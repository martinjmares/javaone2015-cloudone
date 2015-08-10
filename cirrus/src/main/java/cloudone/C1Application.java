package cloudone;

import org.apache.commons.cli.Options;

/**
 * Represents main class of the cloud application.
 *
 * @author Martin Mares (martin.mares at oracle.com)
 */
public class C1Application extends javax.ws.rs.core.Application {

    /** Command line arguments options for configuration of the current application.
     */
    public Options getOptions() {
        return new Options();
    }

    /** Lifecycle method - It is called after cloud instance is prepares and configured but before this
     * particular application is executed.
     *
     * @throws Exception because of any initialisation or validation error.
     */
    public void init() throws Exception {
    }

    /** Lifecycle method - It is called after this application was started.
     */
    public void started() {
    }

    /** Lifecycle method - It is called after this application is no more served.
     */
    public void shutDown() {
    }

}
