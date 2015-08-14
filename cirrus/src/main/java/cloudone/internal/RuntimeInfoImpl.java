package cloudone.internal;

import cloudone.ApplicationInfo;
import cloudone.C1Application;
import cloudone.RuntimeInfo;
import cloudone.ServiceFullName;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

/**
 *  @author Martin Mares (martin.mares at oracle.com)
 */
public class RuntimeInfoImpl implements RuntimeInfo {

    public static class Builder {

        private static final String MANIFEST_SERVICE_NAME = "X-CloudOne-ServiceName";

        private final RuntimeInfoImpl instance = new RuntimeInfoImpl();
        private Manifest manifest;

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

        private Manifest getPreferredManifest() {
            if (manifest == null) {
                Enumeration<URL> resources = null;
                try {
                    resources = getClass().getClassLoader().getResources("META-INF/MANIFEST.MF");
                } catch (IOException e) {
                    throw new RuntimeException("Can not access META-INF/MANIFEST.MF in using ClassLoader.");
                }
                while (resources.hasMoreElements()) {
                    URL url = resources.nextElement();
                    try {
                        Manifest mnf = new Manifest(url.openStream());
                        if (mnf.getMainAttributes().getValue(MANIFEST_SERVICE_NAME) != null) {
                            if (this.manifest == null) {
                                this.manifest = mnf;
                            } else {
                                throw new RuntimeException("More then one manifest file with cloud one servie attribute.");
                            }
                        }
                    } catch (IOException E) {
                        throw new RuntimeException("Can not read/parse manifest file from " + url);
                    }
                }
            }
            if (manifest == null) {
                throw new  RuntimeException("Can not find manifest file with cloud one service related attributes.");
            }
            return manifest;
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
            if (!instance.homeDirectory.exists()) {
                if (!instance.homeDirectory.mkdirs()) {
                    throw new RuntimeException("C1_HOME directory (" + instance.homeDirectory.getPath() + ") does not exists and cannot be created! "
                        + "Plese provide this directory or set C1_HOME environmnet variable or c1home parametter to the existing and accessible directory");
                }
            }
            if (!instance.homeDirectory.isDirectory()) {
                throw new RuntimeException("C1_HOME (" + instance.homeDirectory.getPath() + ") is not directory! "
                        + "Plese provide this directory or set C1_HOME environmnet variable or c1home parametter to the existing and accessible directory");
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
            //Service full name
            String serviceName = getPreferredManifest().getMainAttributes().getValue(MANIFEST_SERVICE_NAME);
            if (serviceName == null) {
                throw new RuntimeException("Can not find " + MANIFEST_SERVICE_NAME + " attribute in MANIFEST.MF file");
            }
            ServiceFullName fullName = new ServiceFullName(serviceName);
            if (instance.getCommandLine().getOptionValue("group") != null) {
                fullName = new ServiceFullName(instance.getCommandLine().getOptionValue("group"), fullName.getArtifactId(), fullName.getVersion());
            }
            if (instance.getCommandLine().getOptionValue("artifact") != null) {
                fullName = new ServiceFullName(fullName.getGroupId(), instance.getCommandLine().getOptionValue("artifact"), fullName.getVersion());
            }
            if (instance.getCommandLine().getOptionValue('v') != null) {
                fullName = new ServiceFullName(fullName.getGroupId(), fullName.getArtifactId(), instance.getCommandLine().getOptionValue('v'));
            }
            instance.serviceFullName = fullName;
            //Build
            RuntimeInfoImpl.instance = this.instance;
        }

    }

    private static RuntimeInfoImpl instance;

    private List<ApplicationInfo> applicationInfos = new ArrayList<>();
    private Options cmdlOptions;
    private CommandLine commandLine;
    private File homeDirectory;
    private ServiceFullName serviceFullName;
    private final long createdTimestamp = System.currentTimeMillis();

    @Override
    public List<ApplicationInfo> getApplicationInfos() {
        return Collections.unmodifiableList(applicationInfos);
    }

    @Override
    public Options getCmdlOptions() {
        return cmdlOptions;
    }

    @Override
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

    @Override
    public ServiceFullName getServiceFullName() {
        return serviceFullName;
    }
}
