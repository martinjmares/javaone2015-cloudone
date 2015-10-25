package we.love.pluto.infoproxy;

import java.util.Arrays;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;

/**
 * Just print all calls.
 *
 * @author Martin Mares (martin.mares at oracle.com)
 */
@Path("/")
public class PrintingResource {

    @Path("space-object/of-the-moment")
    @POST
    public void ofTheMoment(@QueryParam("user") String user, String[] objects) {
        System.out.println("SPACE OBJECTS ARE: " + Arrays.toString(objects) + " set by " + user);
    }

}
