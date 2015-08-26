package cloudone.internal;

import cloudone.ApplicationInfo;
import cloudone.C1Application;
import cloudone.C1Services;
import cloudone.LifecycleService;
import cloudone.internal.nimbostratus.CumulonimbusClient;
import cloudone.internal.nimbostratus.NimbostratusApp;
import org.apache.commons.cli.HelpFormatter;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.stream.Stream;

/**
 * Main class for all CloudOne application.
 *
 * @author Martin Mares (martin.mares at oracle.com)
 */
public class ServiceMain {
    private static final Logger LOGGER = LoggerFactory.getLogger(ServiceMain.class);

    private final ApplicationInfo nimbostratusAppInfo;

    private ServiceMain() {
        nimbostratusAppInfo = new ApplicationInfo() {
                    private final NimbostratusApp app = new NimbostratusApp();
                    @Override
                    public C1Application getApplication() {
                        return app;
                    }
                    @Override
                    public String getName() {
                        return "Nimbostratus";
                    }
                    @Override
                    public int getPort() {
                        return C1Services.getInstance().getRuntimeInfo().getAdminPort();
                    }
                };
    }

    private Stream<ApplicationInfo> getAllAppInfoStream() {
        return Stream.concat(C1Services.getInstance().getRuntimeInfo().getApplicationInfos().stream(),
                Stream.of(this.nimbostratusAppInfo));
    }

    private void run() throws Exception{
        final RuntimeInfoImpl runtimeInfo = (RuntimeInfoImpl) C1Services.getInstance().getRuntimeInfo();
        //Help?
        if (runtimeInfo.getCommandLine().hasOption('h')) {
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp(runtimeInfo.getServiceFullName().getArtifactId() + " Service",
                    RuntimeInfoImpl.getInstance().getCmdlOptions());
            return;
        }
        //Port update
        updatePortNumbers(runtimeInfo);
        //Start all applications
        LOGGER.info("STARTING: " + runtimeInfo.getServiceFullName());
        LifecycleServiceImpl lifecycleService = (LifecycleServiceImpl) C1Services.getInstance().getLifecycleService();
        getAllAppInfoStream().forEach(info -> {
            try {
                info.getApplication().init();
                LOGGER.info("STARTING Application: " + info.getName() + " on " + info.getPort());
                final ResourceConfig resourceConfig = ResourceConfig.forApplication(info.getApplication());
                final URI uri = URI.create("http://localhost:" + info.getPort() + "/");
                final HttpServer server = GrizzlyHttpServerFactory.createHttpServer(uri, resourceConfig);
                lifecycleService.registerListener(new LifecycleService.LifecycleListener() {
                    @Override
                    public void onStart() {
                    }

                    @Override
                    public void onShutdown() {
                        LOGGER.info("--------------- SHUTDOWN: " + info.getName() + " ---------------");
                        server.shutdown();
                        info.getApplication().shutDown();
                    }
                });

            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
        //Register service
        CumulonimbusClient.getInstance().register();
        lifecycleService.registerListener(new LifecycleService.LifecycleListener() {
            @Override
            public void onStart() {
            }

            @Override
            public void onShutdown() {
                try {
                    CumulonimbusClient.getInstance().unregister();
                } catch (Exception e) {
                    LOGGER.warn("Cannot uregister service from cumulonimbus!", e);
                }
            }
        });
        //Start
        lifecycleService.start();
        getAllAppInfoStream().forEach(info -> {
            info.getApplication().started();
            LOGGER.info("--------------- " + info.getName() + " is RUNNING on port " + info.getPort() + " ---------------");
        });
        //Wait for stop signal
        lifecycleService.awaitForShutdown();
    }

    private void updatePortNumbers(final RuntimeInfoImpl runtimeInfo) {
        if (runtimeInfo.getAdminPort() < 0) {
            runtimeInfo.setAdminPort(CumulonimbusClient.getInstance().reservePort(true));
        }
        runtimeInfo
                .getApplicationInfos()
                .stream()
                .filter(ai -> ai.getPort() < 0)
                .forEach(info -> ((ApplicationInfoImpl) info).setPort(CumulonimbusClient.getInstance().reservePort(false)));
    }

    public static void main(String[] args) {
        try {
            (new RuntimeInfoImpl.Builder())
                    .findApplications()
                    .processCommandLineArgs(args)
                    .build();
        } catch (Exception exc) {
            LOGGER.error("Initialisation issue!", exc);
        }
        try {
            ServiceMain serviceMain = new ServiceMain();
            serviceMain.run();
        } catch (Exception exc) {
            LOGGER.error("Unexpected exception in main thread", exc);
            System.exit(1);
        }
    }
}
