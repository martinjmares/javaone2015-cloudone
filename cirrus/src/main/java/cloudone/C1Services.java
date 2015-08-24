package cloudone;

import cloudone.internal.LifecycleServiceImpl;
import cloudone.internal.RuntimeInfoImpl;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicLong;

/** Provides access to all cloudOne services.
 *
 * @author Martin Mares (martin.mares at oracle.com)
 */
public class C1Services {

    private static class C1ThreadFactory implements ThreadFactory {
        private final ThreadGroup group;
        private final AtomicLong threadNumber = new AtomicLong(1);
        private final String namePrefix;

        C1ThreadFactory(String namePrefix) {
            SecurityManager s = System.getSecurityManager();
            group = (s != null) ? s.getThreadGroup() :
                    Thread.currentThread().getThreadGroup();
            if (namePrefix == null) {
                throw new NullPointerException("Name prefix cannot be null.");
            }
            this.namePrefix = namePrefix;
        }

        public Thread newThread(Runnable r) {
            Thread t = new Thread(group, r,
                    namePrefix + threadNumber.getAndIncrement(),
                    0);
            if (!t.isDaemon()) {
                t.setDaemon(true);
            }
            if (t.getPriority() != Thread.NORM_PRIORITY) {
                t.setPriority(Thread.NORM_PRIORITY);
            }
            return t;
        }
    }

    private static final C1Services INSTANCE = new C1Services();

    private final ScheduledExecutorService schedulerService = Executors.newScheduledThreadPool(2, new C1ThreadFactory("C1-Scheduler-"));
    private final ExecutorService executorService = Executors.newCachedThreadPool(new C1ThreadFactory("C1-Executor-"));

    /** Returns basic initialisation information for this cloudOne server instance.
     */
    public RuntimeInfo getRuntimeInfo() {
        return RuntimeInfoImpl.getInstance();
    }

    public LifecycleService getLifecycleService() {
        return LifecycleServiceImpl.getInstance();
    }

    public ScheduledExecutorService getScheduledExecutorService() {
        return schedulerService;
    }

    public ExecutorService getExecutorService() {
        return executorService;
    }

    public static C1Services getInstance() {
        return INSTANCE;
    }

}
