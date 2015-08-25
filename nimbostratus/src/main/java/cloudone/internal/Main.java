package cloudone.internal;

import cloudone.internal.nimbostratus.NimbostratusApp;
import org.apache.commons.cli.HelpFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Main class for all CloudOne application.
 *
 * @author Martin Mares (martin.mares at oracle.com)
 */
public class Main {
    private static final Logger LOGGER = LoggerFactory.getLogger(Main.class);

    public void main(String[] args) {
        try {
            (new RuntimeInfoImpl.Builder())
                    .addApplication(new NimbostratusApp())
                    .findApplications()
                    .processCommandLineArgs(args)
                    .build();
            if (RuntimeInfoImpl.getInstance().getCommandLine().hasOption('h')) {
                HelpFormatter formatter = new HelpFormatter();
                formatter.printHelp("CloudOne Service", RuntimeInfoImpl.getInstance().getCmdlOptions());
            }
            //TODO: Update port numbers
        } catch (Exception exc) {
            LOGGER.error("General exception in main thread!", exc);
        }
    }
}
