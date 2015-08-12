package cloudone.cumulonimbus.resources;

import cloudone.C1Services;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import java.time.Duration;

/**
 *  @author Martin Mares (martin.mares at oracle.com)
 */
@Path("/")
public class LifecycleResource {

    private static final Logger LOGGER = LoggerFactory.getLogger(LifecycleResource.class);

    @GET
    @Path("shutdown")
    //@Produces("text/plain")
    public void shutDown() {
        C1Services.getInstance().getLifecycleService().shutdown();
    }

    @GET
    @Path("uptime")
    @Produces("text/plain")
    public Duration uptime() {
        return Duration.ofMillis(System.currentTimeMillis() - C1Services.getInstance().getRuntimeInfo().getCreatedTimestamp());
    }

}
