package cloudone.cumulonimbus;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;

import cloudone.cumulonimbus.later.LaterService;
import cloudone.cumulonimbus.model.Cluster;
import cloudone.cumulonimbus.model.HttpMethod;
import cloudone.cumulonimbus.model.PathRegistry;
import cloudone.cumulonimbus.model.RegisteredRuntime;
import cloudone.cumulonimbus.model.RestResourceDescription;
import cloudone.cumulonimbus.model.ServiceRestResources;
import cloudone.cumulonimbus.provider.ServiceRestResourcesProvider;
import cloudone.internal.ApplicationFullName;
import org.slf4j.LoggerFactory;

/**
 * Manages catalogue of REST resources from various services.
 *
 * @author Martin Mares (martin.mares at oracle.com)
 */
public class ResourceRegistryService {

    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(ResourceRegistryService.class);
    private static final ResourceRegistryService INSTANCE = new ResourceRegistryService();

    private final Client client;
    private final ExecutorService executor;

    private final ConcurrentMap<ApplicationFullName, ServiceRestResources> register = new ConcurrentHashMap<>();
    private final PathRegistry pathRegistry = new PathRegistry();

    public ResourceRegistryService() {
        client = ClientBuilder.newClient()
                .register(ServiceRestResourcesProvider.class);
        executor = Executors.newSingleThreadExecutor();
    }

    ServiceRegistryService.RegistrationListener getNewListener() {
        return new ServiceRegistryService.RegistrationListener() {
            @Override
            public void register(final RegisteredRuntime runtime, final Cluster cluster) throws Exception {
                for (String appName : runtime.getApplicationPorts().keySet()) {
                    final ApplicationFullName appFullName = new ApplicationFullName(runtime.getServiceName(), appName);
                    if (!register.containsKey(appFullName)) {
                        executor.submit(
                                () -> doLoadServiceContract(appFullName,
                                                            runtime.getApplicationPort(appFullName.getApplicationName())));
                    }
                }
            }
            @Override
            public void unregister(final RegisteredRuntime runtime, final Cluster cluster) {
                executor.submit(() ->
                {if (cluster.getRuntimes().isEmpty()
                        || cluster.getRuntimes().size() == 1 && cluster.getRuntimes().get(0).equals(runtime)) {
                    doUnloadServiceContract(runtime);
                }});
            }
        };
    }

    private void doLoadServiceContract(final ApplicationFullName appFullName, final int port) {
        LOGGER.info("doLoadServiceContract(" + appFullName + ", " + port + ")");
        if (!register.containsKey(appFullName)) {
            try {
                WebTarget target = client.target("http://localhost:" + port);
                ServiceRestResources serviceRestResources = target.path("application.wadl")
                        .request() //TODO: Define media type of WADL
                        .get(ServiceRestResources.class);
                serviceRestResources = new ServiceRestResources(appFullName, serviceRestResources.getResources());
                register.put(appFullName, serviceRestResources);
                for (RestResourceDescription restResourceDescription : serviceRestResources.getResources()) {
                    pathRegistry.register(restResourceDescription.getPath(), restResourceDescription.getMethod(), appFullName);
                }
                //This direct call is very ugly - MUST BE REDESIGNED
                LaterService.getInstance().processQueuesForNewApp(appFullName);
            } catch (Exception e) {
                LOGGER.warn("doLoadServiceContract(" + appFullName + "): Cannot load wadl!", e);
            }
        }
    }

    private void doUnloadServiceContract(RegisteredRuntime registeredRuntime) {
        LOGGER.info("doUnloadServiceContract(" + registeredRuntime + ")");
        for (String app : registeredRuntime.getApplicationPorts().keySet()) {
            final ApplicationFullName appFullName = new ApplicationFullName(registeredRuntime.getServiceName(), app);
            ServiceRestResources serviceRestResources = register.remove(appFullName);
            pathRegistry.unregister(appFullName);
        }
    }

    public Set<ApplicationFullName> getApplicationsForResource(final String path, final HttpMethod method) {
        return pathRegistry.get(path, method);
    }

    public List<RestResourceDescription> getResourcesForApplication(ApplicationFullName appName) {
        ServiceRestResources result = register.get(appName);
        if (result == null) {
            return Collections.emptyList();
        } else {
            return Collections.unmodifiableList(result.getResources());
        }
    }

    public static ResourceRegistryService getInstance() {
        return INSTANCE;
    }
}
