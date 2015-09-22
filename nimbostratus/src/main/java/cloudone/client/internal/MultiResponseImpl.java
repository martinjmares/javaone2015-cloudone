package cloudone.client.internal;

import cloudone.C1Services;
import cloudone.client.MultiResponse;
import cloudone.internal.ApplicationFullName;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;

/**
 * Implementation of MultiResponse which contruct is fulfilled when all callables are add. It means that no new item
 * cannot be add after this item is returned.
 *
 * @author Martin Mares (martin.mares at oracle.com)
 */
public class MultiResponseImpl implements MultiResponse {

    public static class IdentifiedResponseImpl implements IdentifiedResponse {

        private final ApplicationFullName applicationFullName;
        private final Future<Response> responseFuture;
        private volatile Exception exception;

        public IdentifiedResponseImpl(ApplicationFullName applicationFullName, Future<Response> responseFuture) {
            this.applicationFullName = applicationFullName;
            this.responseFuture = responseFuture;
        }

        @Override
        public ApplicationFullName getApplicationFullName() {
            return applicationFullName;
        }

        @Override
        public Response getResponse() {
            if (exception != null) {
                if (exception instanceof WebApplicationException) {
                    return ((WebApplicationException) exception).getResponse();
                } else {
                    return null;
                }
            }
            try {
                return responseFuture.get();
            } catch (InterruptedException e) {
                this.exception = e;
                return null;
            } catch (ExecutionException e) {
                if (e.getCause() instanceof Exception) {
                    exception = (Exception) e.getCause();
                    if (exception instanceof WebApplicationException) {
                        return ((WebApplicationException) exception).getResponse();
                    } else {
                        return null;
                    }
                } else {
                    //This is very unexpected
                    throw new RuntimeException("Cannot reach " + applicationFullName, e.getCause());
                }
            }
        }

        public Exception getError() {
            getResponse();
            return exception;
        }

        boolean isDone() {
            return responseFuture.isDone();
        }

    }

    private final List<IdentifiedResponseImpl> responses = new ArrayList<>();
    private final Map<ApplicationFullName, IdentifiedResponse> map = new HashMap<>();
    private volatile CountDownLatch nextDoneLatch = new CountDownLatch(1);

    /**
     * Add new futer task based on provided callable. It MUST be called only as a part of construction process and
     * task cannot be used based on it's contract before it is fully constructed.
     */
    void add(ApplicationFullName applicationFullName, Callable<Response> responseCallable) {
        ExecutorService executorService = C1Services.getInstance().getExecutorService();
        FutureTask<Response> responseFuture = new FutureTask<>(responseCallable);
        final IdentifiedResponseImpl identifiedResponse = new IdentifiedResponseImpl(applicationFullName, responseFuture);
        executorService.execute(new FutureTask<Response>(responseFuture, null) {
            @Override
            protected void done() {
                super.done();
                synchronized (responses) {
                    responses.add(identifiedResponse);
                    nextDoneLatch.countDown();
                    nextDoneLatch = new CountDownLatch(1);
                }
            }
        });
    }

    @Override
    public Iterator<IdentifiedResponse> iterator() {
        return new Iterator<IdentifiedResponse>() {

            private int index = 0;

            @Override
            public boolean hasNext() {
                if (index < responses.size()) {
                    return true;
                }
                CountDownLatch latch;
                synchronized (responses) {
                    if (index < responses.size()) {
                        return true;
                    }
                    if (responses.size() == map.size()) {
                        return false;
                    }
                    latch = nextDoneLatch;
                }
                try {
                    latch.await();
                    return true;
                } catch (InterruptedException e) {
                    throw new RuntimeException("Interuption during wait for next item!", e);
                }
            }

            @Override
            public IdentifiedResponse next() {
                if (hasNext()) {
                    synchronized (responses) {
                        return responses.get(index++);
                    }
                } else {
                    throw new NoSuchElementException();
                }
            }
        };
    }

    public Set<ApplicationFullName> getApplicationNames() {
        return map.keySet();
    }

    public IdentifiedResponse getResult(ApplicationFullName name) {
        return map.get(name);
    }

}
