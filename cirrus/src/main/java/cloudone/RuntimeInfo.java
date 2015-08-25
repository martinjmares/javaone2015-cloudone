package cloudone;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;

import java.io.File;
import java.util.List;

/**
 * Provides basic information about this cloudOne instance.
 *
 * @author Martin Mares (martin.mares at oracle.com)
 */
public interface RuntimeInfo {

    /**
     * Returns full name of this service.
     */
    public ServiceFullName getServiceFullName();

    /**
     * Returns all applications included in this runtimeInstance.
     */
    public List<ApplicationInfo> getApplicationInfos();

    /**
     * Returns admin port of this runtime.
     */
    public int getAdminPort();

    /**
     * Returns all command line options recognised by this runtime instance.
     */
    public Options getCmdlOptions();

    /**
     * Returns parsed command line parameters.
     */
    public CommandLine getCommandLine();

    /**
     * Timestamp in millis from epoche when this instance was started.
     */
    public long getCreatedTimestamp();

    /**
     * Returns home directory for cloud persisted data
     */
    public File getHomeDirectory();

}
