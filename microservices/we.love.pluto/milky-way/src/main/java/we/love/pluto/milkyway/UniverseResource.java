package we.love.pluto.milkyway;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;

/**
 * JAX-RS resource for information about space objects.
 *
 * @author Martin Mares (martin.mares at oracle.com)
 */
@Path("/universe")
@Produces("application/json")
public class UniverseResource {

    private static final Logger LOGGER = LoggerFactory.getLogger(UniverseResource.class);

    @Path("{star}")
    @GET
    public StarInfo getInfo(@PathParam("star") String star) {
        LOGGER.info("getInfo(" + star + ")");
        StarInfo result = StarInfoService.getStarInfo(star);
        if (result == null) {
            throw new WebApplicationException(404);
        } else {
            return result;
        }
    }

}
