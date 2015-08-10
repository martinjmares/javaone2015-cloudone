package cloudone.internal;

import cloudone.ApplicationInfo;
import cloudone.C1Application;
import cloudone.RuntimeInfo;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 *  @author Martin Mares (martin.mares at oracle.com)
 */
public class RuntimeInfoImpl implements RuntimeInfo {

    public static class Builder {

        private final RuntimeInfoImpl instance = new RuntimeInfoImpl();

        public Builder addApplication(C1Application c1Application) {
            instance.applicationInfos.add(new ApplicationInfoImpl(c1Application));
            return this;
        }

        private Options createInternalOptions() {
            Options result = new Options();
            result.addOption("h", "help", false, "Prints this help information.");
            result.addOption(null, "group", true, "Group id");
            result.addOption(null, "artifact", true, "Artifact id");
            result.addOption("v", "version", true, "Version");
            result.addOption(null, "c1home", true, "Cloud one home directory");
            return result;
        }

        public Builder processCommandLineArgs(String[] args) throws ParseException {
            Options opts = createInternalOptions();
            for (ApplicationInfo appInfo : instance.applicationInfos) {
                Options op = appInfo.getApplication().getOptions();
                if (op != null) {
                    for (Option option : op.getOptions()) {
                        opts.addOption(option);
                    }
                }
                opts.addOption(null, appInfo.getName() + ".port", true, "Port for " + appInfo.getName());
            }
            if (instance.applicationInfos.size() == 1) {
                opts.addOption("p", "port", true, "Port for the application");
            }
            instance.cmdlOptions = opts;
            //Parse cmd line args
            DefaultParser parser = new DefaultParser();
            instance.commandLine = parser.parse(opts, args);
            return this;
        }

        public void build() throws Exception {
            instance.applicationInfos = Collections.unmodifiableList(instance.applicationInfos);
            //Find C1_HOME
            if (instance.commandLine.getOptionValue("c1home") != null) {
                instance.homeDirectory = new File(instance.commandLine.getOptionValue("c1home"));
            } else if (System.getenv("C1_HOME") != null) {
                instance.homeDirectory = new File(System.getenv("C1_HOME"));
            } else {
                instance.homeDirectory = new File("/var/cloud1");
            }
            //Ports
            int defaultPort = -1;
            try {
                String val = instance.commandLine.getOptionValue('p');
                if (val != null) {
                    defaultPort = Integer.parseInt(val);
                }
            } catch (Exception exc) {
                throw new Exception("Cannot parse -p / --port parameter value.");
            }
            for (ApplicationInfo appInfo : instance.applicationInfos) {
                ((ApplicationInfoImpl) appInfo).setPort(defaultPort);
                try {
                    String val = instance.commandLine.getOptionValue('p');
                    if (val != null) {
                        ((ApplicationInfoImpl) appInfo).setPort(Integer.parseInt(val));
                    }
                } catch (Exception exc) {
                    throw new Exception("Cannot parse --" + appInfo.getName() + ".port parameter value.");
                }
            }
            //Build
            RuntimeInfoImpl.instance = this.instance;
        }

    }

    private static RuntimeInfoImpl instance;

    private List<ApplicationInfo> applicationInfos = new ArrayList<>();
    private Options cmdlOptions;
    private CommandLine commandLine;
    private File homeDirectory;
    private final long createdTimestamp = System.currentTimeMillis();

    public List<ApplicationInfo> getApplicationInfos() {
        return Collections.unmodifiableList(applicationInfos);
    }

    public Options getCmdlOptions() {
        return cmdlOptions;
    }

    public CommandLine getCommandLine() {
        return commandLine;
    }

    public static RuntimeInfoImpl getInstance() {
        return instance;
    }

    public File getHomeDirectory() {
        return homeDirectory;
    }

    public long getCreatedTimestamp() {
        return createdTimestamp;
    }
}
