package cloudone.internal;

import cloudone.LifecycleService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;

/**
 * @author Martin Mares (martin.mares at oracle.com)
 */
public class LifecycleServiceImpl implements LifecycleService {

    private enum Status {
        IDLE, RUNNING, STOPPING;
    }

    private static final LifecycleServiceImpl INSTANCE = new LifecycleServiceImpl();

    private static final Logger LOGGER = LoggerFactory.getLogger(LifecycleServiceImpl.class);

    private volatile Status status = Status.IDLE;
    private volatile CountDownLatch latch;
    private volatile List<LifecycleListener> lifecycleListeners = Collections.synchronizedList(new ArrayList<>());
    private boolean hooked = false;

    private void fireStarted() {
        for (LifecycleListener lifecycleListener : lifecycleListeners) {
            try {
                lifecycleListener.onStart();
            } catch (Exception exc) {
                LOGGER.warn("Exception from one of lifecycleListeners.", exc);
            }
        }
    }

    private void fireShutdown() {
        for (LifecycleListener lifecycleListener : lifecycleListeners) {
            try {
                lifecycleListener.onShutdown();
            } catch (Exception exc) {
                LOGGER.warn("Exception from one of lifecycleListeners.", exc);
            }
        }
    }

    public synchronized void start() {
        switch (status) {
            case IDLE:
                latch = new CountDownLatch(1);
                setStatus(Status.RUNNING);
                if (!hooked) {
                    Runtime.getRuntime().addShutdownHook(new Thread() {
                            @Override
                            public void run() {
                                if (status == Status.RUNNING) {
                                    shutdown();
                                }
                            }
                        });
                }
                fireStarted();
                break;
            case STOPPING:
                throw new RuntimeException("Server is yet in shutdown mode. Can not start");
            case RUNNING:
                return;
        }
    }

    @Override
    public synchronized void shutdown() {
        if (status == Status.RUNNING) {
            setStatus(Status.STOPPING);
            fireShutdown();
            if (latch != null) {
                latch.countDown();
            }
            setStatus(Status.IDLE);
        }
    }

    @Override
    public void registerListener(LifecycleListener listener) {
        lifecycleListeners.add(listener);
    }

    public void awaitForShutdown() throws InterruptedException {
        if (status == Status.RUNNING && latch != null) {
            latch.await();
        }
    }

    public Status getStatus() {
        return status;
    }

    private synchronized void setStatus(Status status) {
        if (this.status != status) {
            this.status = status;
            LOGGER.info("*** Server is " + status);
        }
    }

    public static LifecycleServiceImpl getInstance() {
        return INSTANCE;
    }
}
