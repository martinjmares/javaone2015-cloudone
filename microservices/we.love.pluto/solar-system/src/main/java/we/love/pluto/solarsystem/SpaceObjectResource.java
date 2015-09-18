package we.love.pluto.solarsystem;

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
public class SpaceObjectResource {

    private static final Logger LOGGER = LoggerFactory.getLogger(SpaceObjectResource.class);

    @Path("{planet}")
    @GET
    public PlanetInfo getInfo(@PathParam("planet") String planet) {
        LOGGER.info("getInfo(" + planet + ")");
        PlanetInfo result = PlanetInfoService.getPlanetInfo(planet);
        if (result == null) {
            throw new WebApplicationException(404);
        } else {
            return result;
        }
    }

}
