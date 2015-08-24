package cloudone.cumulonimbus.resources;

import cloudone.cumulonimbus.PortService;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;

/**
 * Provides configuration for microservices. Curretnly it supports ONLY ports.
 *
 * @author Martin Mares (martin.mares at oracle.com)
 */
@Path("/configuration")
public class ConfigurationResource {

    private enum Scope {
        ADMIN, APPLICATION;

        public static Scope getEnum(String value) {
            if (value == null) {
                return APPLICATION;
            }
            for (Scope v : values()) {
                if (v.toString().equalsIgnoreCase(value)) {
                    return v;
                }
            }
            throw new IllegalArgumentException();
        }
    }

    @GET
    @Path("reserve-port")
    @Produces("text/plain")
    public int reserve(@QueryParam("scope") String strScope) throws Exception {
        Scope scope = Scope.getEnum(strScope);
        switch (scope) {
            case APPLICATION:
                return PortService.getInstance().reserveApplicationPort();
            case ADMIN:
                return PortService.getInstance().reserveAdminPort();
        }
        throw new WebApplicationException("Unknown scope parameter!", 400);
    }

}
