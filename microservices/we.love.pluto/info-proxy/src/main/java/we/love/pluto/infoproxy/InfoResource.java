package we.love.pluto.infoproxy;

import cloudone.ServiceFullName;
import cloudone.client.C1Client;
import cloudone.client.C1ClientBuilder;
import org.glassfish.jersey.gson.GsonFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

/**
 * @author Martin Mares (martin.mares at oracle.com)
 */
@Path("/")
public class InfoResource {

    private static final Logger LOGGER = LoggerFactory.getLogger(InfoResource.class);
    private static C1Client client = new C1ClientBuilder()
                                            .build()
                                            .register(GsonFeature.class);

    @Path("planet/{name}")
    @GET
    @Produces("text/plain")
    public String getPlanetInfo(@PathParam("name") String name) {
        LOGGER.info("getPlanetInfo(" + name + ")");
        Info info = client.target(new ServiceFullName("we.love.pluto", "solar-system", null))
                                .path("universe/{name}")
                                .resolveTemplate("name", name)
                                .anyOK()
                                .get(Info.class);
        return info.toString();
    }

    @Path("star/{name}")
    @GET
    @Produces("text/plain")
    public String getStarInfo(@PathParam("name") String name) {
        LOGGER.info("getStarInfo(" + name + ")");
        Info info = client.target(new ServiceFullName("we.love.pluto", "milky-way", null))
                .path("universe/{name}")
                .resolveTemplate("name", name)
                .anyOK()
                .get(Info.class);
        return info.toString();
    }

    @Path("object/{name}")
    @GET
    @Produces("text/plain")
    public String getObjectInfo(@PathParam("name") String name) {
        LOGGER.info("getObjectInfo(" + name + ")");
        Info info = client.target()
                .path("universe/{name}")
                .resolveTemplate("name", name)
                .anyOK()
                .get(Info.class);
        return info.toString();
    }
}
