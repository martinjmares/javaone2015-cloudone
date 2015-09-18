package cloudone.cumulonimbus.resources;

import cloudone.ServiceFullName;
import cloudone.cumulonimbus.ResourceRegistryService;
import cloudone.cumulonimbus.model.HttpMethod;
import cloudone.cumulonimbus.model.RestResourceDescription;
import cloudone.internal.ApplicationFullName;
import org.slf4j.LoggerFactory;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
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
    @Produces("application/json")
    public Set<ApplicationFullName> getApplicationsForResource(@QueryParam("path") String path, @QueryParam("method") String method) {
        if (path == null || path.length() == 0 || method == null || method.length() == 0) {
            throw new WebApplicationException("Query parameters path and method must be specified!", 400);
        }
        return ResourceRegistryService.getInstance().getApplicationsForResource(path, HttpMethod.valueOf(method));
    }

    @GET
    @Produces("text/plain")
    public String getPortsForResourceAsString(@QueryParam("path") String path, @QueryParam("method") String method) {
        Set<ApplicationFullName> apps = getApplicationsForResource(path, method);
        final StringBuilder result = new StringBuilder();
        for (ApplicationFullName app : apps) {
            result.append(app).append('\n');
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
        final ServiceFullName serviceName = new ServiceFullName(group, app, version);
        final List<RestResourceDescription> result
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
        final Collection<RestResourceDescription> data = describeService(group, app, version, application);
        final StringBuilder result = new StringBuilder();
        if (data != null) {
            for (RestResourceDescription description : data) {
                result.append(description).append('\n');
            }
        }
        return result.toString();
    }
}
