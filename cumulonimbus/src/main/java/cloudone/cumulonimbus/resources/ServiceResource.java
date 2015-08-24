package cloudone.cumulonimbus.resources;

import cloudone.ServiceFullName;
import cloudone.cumulonimbus.ServiceRegistryService;
import cloudone.cumulonimbus.model.Cluster;
import cloudone.cumulonimbus.model.RegisteredRuntime;
import cloudone.internal.dto.PortInfo;
import cloudone.internal.dto.RuntimeIdAndSecCode;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Management of service runtimes.
 *
 * @author Martin Mares (martin.mares at oracle.com)
 */
@Path("/service")
public class ServiceResource {

    @GET
    @Produces("application/json")
    public Collection<ServiceFullName> list() {
        Collection<Cluster> clusters = ServiceRegistryService.getInstance().getClusters();
        List<ServiceFullName> result = new ArrayList<>(clusters.size());
        for (Cluster cluster : clusters) {
            result.add(cluster.getFullName());
        }
        return result;
    }

    @GET
    @Path("{group}/{application}/{version}")
    @Produces("application/json")
    public Collection<Integer> listRuntimes(@PathParam("group") String group,
                              @PathParam("application") String app,
                              @PathParam("version") String version) {
        ServiceFullName name = new ServiceFullName(group, app, version);
        Cluster cluster = ServiceRegistryService.getInstance().getCluster(name);
        if (cluster == null) {
            throw new WebApplicationException("No registered " + name + " service!", 404);
        }
        List<RegisteredRuntime> runtimes = cluster.getRuntimes();
        List<Integer> result = new ArrayList<>(runtimes.size());
        for (RegisteredRuntime runtime : runtimes) {
            result.add(runtime.getInstanceId());
        }
        return result;
    }

    @PUT
    @Path("{group}/{application}/{version}")
    @Produces("application/json")
    public RuntimeIdAndSecCode register(@PathParam("group") String group,
                                            @PathParam("application") String app,
                                            @PathParam("version") String version,
                                            PortInfo portInfo) throws Exception {
        ServiceFullName name = new ServiceFullName(group, app, version);
        RegisteredRuntime runtime = ServiceRegistryService
                .getInstance()
                .register(name, portInfo.getAdminPort(), portInfo.getApplicationPorts());
        RuntimeIdAndSecCode result = new RuntimeIdAndSecCode(runtime.getInstanceId(), runtime.getInstanceSecCode());
        return result;
    }

    @DELETE
    @Path("{group}/{application}/{version}/{instance}")
    @Produces("application/json")
    public void unregister(@PathParam("group") String group,
                                        @PathParam("application") String app,
                                        @PathParam("version") String version,
                                        @PathParam("instance") int instance,
                                        @QueryParam("seccode") String secCode) {
        if (secCode == null || secCode.length() == 0) {
            throw new WebApplicationException("Parameter seccode must be specified", 400);
        }
        ServiceFullName name = new ServiceFullName(group, app, version);
        RegisteredRuntime runtime = ServiceRegistryService.getInstance().getRuntime(secCode);
        if (runtime == null) {
            throw new WebApplicationException("No registered runtime!", 404);
        }
        if (!name.equals(runtime.getServiceName()) || instance != runtime.getInstanceId()) {
            throw new WebApplicationException("Invalid sec code!", 400);
        }
        ServiceRegistryService.getInstance().unregister(runtime);
    }

    @GET
    @Path("{group}/{application}/{version}/{instance}")
    @Produces("application/json")
    public PortInfo getPortInfo(@PathParam("group") String group,
                           @PathParam("application") String app,
                           @PathParam("version") String version,
                           @PathParam("instance") int instance) {
        ServiceFullName name = new ServiceFullName(group, app, version);
        Cluster cluster = ServiceRegistryService.getInstance().getCluster(name);
        if (cluster == null) {
            throw new WebApplicationException("Service not found!", 404);
        }
        RegisteredRuntime runtime = cluster.getRuntime(instance);
        if (runtime == null) {
            throw new WebApplicationException("Service runtime not found!", 404);
        }
        return new PortInfo(runtime.getAdminPort(), runtime.getApplicationPorts());
    }

}
