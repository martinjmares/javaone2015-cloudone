package cloudone.internal.resources;

import cloudone.C1Services;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 *  @author Martin Mares (martin.mares at oracle.com)
 */
@Path("/lifecycle")
public class LifecycleResource {

    private static final Logger LOGGER = LoggerFactory.getLogger(LifecycleResource.class);

    private C1Services getC1Services() {
        return C1Services.getInstance();
    }

    @GET
    @Path("shutdown")
    @Produces("text/plain")
    public String shutDown() {
        getC1Services().getScheduledExecutorService().schedule(new Runnable() {
            @Override
            public void run() {
                getC1Services().getLifecycleService().shutdown();
            }
        }, 1, TimeUnit.SECONDS);
        return "OK";
    }

    @GET
    @Path("uptime")
    @Produces("text/plain")
    public Duration uptime() {
        return Duration.ofMillis(System.currentTimeMillis() - getC1Services().getRuntimeInfo().getCreatedTimestamp());
    }

}
