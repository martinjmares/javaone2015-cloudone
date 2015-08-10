package cloudone.cumulonimbus.resources;

import cloudone.C1Services;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

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
        System.exit(0);
    }

    @GET
    @Path("uptime")
    @Produces("text/plain")
    public String uptime() {
        long period = System.currentTimeMillis() - C1Services.getInstance().getRuntimeInfo().getCreatedTimestamp();
        long days = period / (1000 * 60 * 60 * 24);
        period -= days * 1000 * 60 * 60 * 24;
        long hrs = period / (1000 * 60 * 60);
        period -= hrs * 1000 * 60 * 60;
        long min = period / (1000 * 60);
        period -= min * 1000 * 60;
        long sec = period / (1000);
        period -= sec * 1000;
        //Build result
        StringBuilder result = new StringBuilder();
        if (days > 0) {
            result.append(days).append(" days and ");
        }
        if (hrs > 0 || result.length() > 0) {
            if (hrs < 10) {
                result.append('0');
            }
            result.append(hrs).append(":");
        }
        if (min > 0 || result.length() > 0) {
            if (min < 10) {
                result.append('0');
            }
            result.append(min).append(":");
        }
        if (sec > 0 || result.length() > 0) {
            if (sec < 10) {
                result.append('0');
            }
            result.append(sec).append(",");
        }
        if (period < 100) {
            result.append('0');
            if (period < 10) {
                result.append('0');
            }
        }
        result.append(period);
        return result.toString();
    }

}
