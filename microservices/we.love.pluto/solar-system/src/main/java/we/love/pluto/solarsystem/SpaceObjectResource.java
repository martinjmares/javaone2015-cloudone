package we.love.pluto.solarsystem;

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
@Path("universe")
@Produces("application/json")
public class SpaceObjectResource {

    @Path("{planet}")
    @GET
    public PlanetInfo getInfo(@PathParam("planet") String planet) {
        PlanetInfo result = PlanetInfoService.getPlanetInfo(planet);
        if (result == null) {
            throw new WebApplicationException(404);
        } else {
            return result;
        }
    }

}
