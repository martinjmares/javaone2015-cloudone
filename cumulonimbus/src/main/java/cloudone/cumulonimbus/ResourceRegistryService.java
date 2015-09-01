package cloudone.cumulonimbus;

import cloudone.cumulonimbus.model.ApplicationFullName;
import cloudone.cumulonimbus.model.Cluster;
import cloudone.cumulonimbus.model.RegisteredRuntime;
import cloudone.cumulonimbus.model.RestResourceDescription;
import cloudone.cumulonimbus.model.ServiceRestResources;
import cloudone.cumulonimbus.provider.ServiceRestResourcesProvider;
import org.slf4j.LoggerFactory;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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
    private final ConcurrentMap<RestResourceDescription, Set<ApplicationFullName>> revRegister = new ConcurrentHashMap<>();
    //private final ConcurrentMap<RestResourceDescription, Set<String>> requested = new ConcurrentHashMap<>();

    public ResourceRegistryService() {
        client = ClientBuilder.newClient()
                //.register(GsonFeature.class)
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
                executor.submit(() -> doUnloadServiceContract(runtime));
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
                    revRegister
                            .computeIfAbsent(restResourceDescription,
                                    rrd -> Collections.synchronizedSet(new HashSet<>()))
                            .add(appFullName);
                }
                updateClients(serviceRestResources);
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
            if (serviceRestResources != null) {
                for (RestResourceDescription restResourceDescription : serviceRestResources.getResources()) {
                    Set<ApplicationFullName> applicationFullNames = revRegister.get(restResourceDescription);
                    applicationFullNames.remove(appFullName);
                    if (applicationFullNames.size() == 0) {
                        revRegister.remove(restResourceDescription);
                    }
                }
            }
        }
    }

    private void updateClients(ServiceRestResources resources) {
        //TODO
    }

    public Set<ApplicationFullName> getApplicationsForResource(RestResourceDescription resource) {
        //TODO: Update this
        return Collections.unmodifiableSet(revRegister.get(resource));
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
