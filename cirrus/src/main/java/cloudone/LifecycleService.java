package cloudone;

/**
 * Instamce lifecycle operations.
 *
 * @author Martin Mares (martin.mares at oracle.com)
 */
public interface LifecycleService {

    /** Implementation if this listener can be registered into the {@code LifecycleService.registerListener()}.
     */
    public interface LifecycleListener {

        /**
         * Service is started and ready to serve. It means that all included {@code C1Application}s are started
         */
        public void onStart();

        /**
         * Service is stoped. It means that shutdown operation was executed.
         */
        public void onShutdown();

    }

    /**
     * Stop this instance
     */
    public void shutdown();

    /**
     * Register new {@code LifecycleListener}
     */
    public void registerListener(LifecycleListener listener);
}
