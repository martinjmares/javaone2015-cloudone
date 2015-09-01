package cloudone.cumulonimbus.resources;

import cloudone.ServiceFullName;
import cloudone.cumulonimbus.ResourceRegistryService;
import cloudone.cumulonimbus.ServiceRegistryService;
import cloudone.cumulonimbus.model.ApplicationFullName;
import cloudone.cumulonimbus.model.Cluster;
import cloudone.cumulonimbus.model.RestResourceDescription;
import cloudone.internal.dto.ApplicationCluster;
import org.slf4j.LoggerFactory;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * Search for registered resources.
 *
 * @author Martin Mares (martin.mares at oracle.com)
 */
@Path("resource-discovery")
public class ResourceDiscoveryResource {

    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(ResourceDiscoveryResource.class);

    @GET
    @Path("port")
    @Produces("application/json")
    public Collection<ApplicationCluster> getPortsForResource(@QueryParam("path") String path, @QueryParam("method") String method) {
        if (path == null || path.length() == 0 || method == null || method.length() == 0) {
            throw new WebApplicationException("Query parameters path and method must be specified!", 400);
        }
        RestResourceDescription resourceDescription = new RestResourceDescription(method, path);
        Set<ApplicationFullName> applications = ResourceRegistryService.getInstance().getApplicationsForResource(resourceDescription);
        ServiceRegistryService serviceRegistry = ServiceRegistryService.getInstance();
        Collection<ApplicationCluster> result = new ArrayList<>();
        for (ApplicationFullName application : applications) {
            Cluster cluster = serviceRegistry.getCluster(application.getServiceName());
            ApplicationCluster applicationCluster = cluster.toApplicationCluster(application.getApplicationName());
            result.add(applicationCluster);
        }
        return result;
    }

    @GET
    @Path("port")
    @Produces("text/plain")
    public String getPortsForResourceAsString(@QueryParam("path") String path, @QueryParam("method") String method) {
        Collection<ApplicationCluster> appClusters = getPortsForResource(path, method);
        StringBuilder result = new StringBuilder();
        for (ApplicationCluster appCluster : appClusters) {
            result.append(appCluster).append('\n');
        }
        return result.toString();
    }

    @GET
    @Path("describe/{group}/{application}/{version}/{application}")
    @Produces("application/json")
    public Collection<RestResourceDescription> describeService(@PathParam("group") String group,
                                                               @PathParam("application") String app,
                                                               @PathParam("version") String version,
                                                               @PathParam("application") String application) {
        ServiceFullName serviceName = new ServiceFullName(group, app, version);
        List<RestResourceDescription> result
                = ResourceRegistryService.getInstance().getResourcesForApplication(new ApplicationFullName(serviceName, application));
        return result;
    }

    @GET
    @Path("describe/{group}/{application}/{version}/{application}")
    @Produces("text/plain")
    public String describeServiceAsString(@PathParam("group") String group,
                                                 @PathParam("application") String app,
                                                 @PathParam("version") String version,
                                                 @PathParam("application") String application) {
        Collection<RestResourceDescription> data = describeService(group, app, version, application);
        StringBuilder result = new StringBuilder();
        if (data != null) {
            for (RestResourceDescription description : data) {
                result.append(description).append('\n');
            }
        }
        return result.toString();
    }
}
