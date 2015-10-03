package we.love.pluto.visualizer;

import java.io.InputStream;

import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

/**
 * @author Michal Gajdos
 */
@Path("/")
public class VisualizerResource {

    @GET
    @Produces("text/html")
    public InputStream index() {
        return getClass().getResourceAsStream("/static/index.html");
    }

    @GET
    @Path("{resource: .*}")
    public InputStream resource(@PathParam("resource") final String path) {
        final InputStream resource = getClass().getResourceAsStream("/static/" + path.replaceAll("(([\\.]+)/)*", ""));

        if (resource == null) {
            throw new NotFoundException();
        }

        return resource;
    }
}
