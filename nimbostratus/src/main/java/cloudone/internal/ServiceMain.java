package cloudone.internal;

import cloudone.ApplicationInfo;
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

    private void run() throws Exception{
        final RuntimeInfoImpl runtimeInfo = (RuntimeInfoImpl) C1Services.getInstance().getRuntimeInfo();
        updatePortNumbers(runtimeInfo);
        //Start all applications
        LOGGER.info("STARTING: " + runtimeInfo.getServiceFullName());
        LifecycleServiceImpl lifecycleService = (LifecycleServiceImpl) C1Services.getInstance().getLifecycleService();
        Stream<ApplicationInfo> apps = Stream.concat(runtimeInfo.getApplicationInfos().stream(),
                Stream.of(new ApplicationInfoImpl(new NimbostratusApp(), runtimeInfo.getAdminPort())));
        apps.forEach(info -> {
            try {
                info.getApplication().init();
                LOGGER.info("STARTING Application: " + info.getName());
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
        apps.forEach(info -> {
            info.getApplication().started();
            LOGGER.info("--------------- " + info.getName() + " is RUNNING on port " + info.getPort() + " ---------------");
        });
        lifecycleService.start();
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

    public void main(String[] args) {
        try {
            (new RuntimeInfoImpl.Builder())
                    .findApplications()
                    .processCommandLineArgs(args)
                    .build();
            //Help?
            if (RuntimeInfoImpl.getInstance().getCommandLine().hasOption('h')) {
                HelpFormatter formatter = new HelpFormatter();
                formatter.printHelp("CloudOne Service", RuntimeInfoImpl.getInstance().getCmdlOptions());
                return;
            }
        } catch (Exception exc) {
            LOGGER.error("General exception in main thread!", exc);
        }
    }
}
