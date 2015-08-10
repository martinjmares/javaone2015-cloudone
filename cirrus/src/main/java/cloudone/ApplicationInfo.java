package cloudone;

/**
 * General information about one included application.
 * <p>
 *     Any cloudOne runtime can execute more then one application.
 * </p>
 *
 * @see C1Application
 * @see RuntimeInfo
 * @author Martin Mares (martin.mares at oracle.com)
 */
public interface ApplicationInfo {

    public C1Application getApplication();

    /** Returns String identifier of the application
     */
    public String getName();

    /** Each {@code C1Application} is executed on it's own port.
     */
    public int getPort();

}
