package cloudone.cumulonimbus.resources;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;

import cloudone.cumulonimbus.later.LaterService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Supports for later invocation.
 *
 * @author Martin Mares (martin.mares at oracle.com)
 */
@Path("/later")
public class LaterResource {

    private static final Logger LOGGER = LoggerFactory.getLogger(LaterResource.class);

    @Path("all")
    @POST
    public String all(@HeaderParam("Content-Type") String contentType,
                      @QueryParam("method") String methodName,
                      @QueryParam("uri") String uri,
                      @QueryParam("services") String services,
                      @QueryParam("retention-count") @DefaultValue("-1") int retentionCount,
                      InputStream is) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        if (is != null) {
            byte[] buff = new byte[64];
            int count = -1;
            try {
                while ((count = is.read(buff)) > 0) {
                    baos.write(buff,0, count);
                }
            } catch (IOException e) {
                throw new WebApplicationException(500);
            }
        }
        LaterService
                .getInstance()
                .addAllItem(contentType, methodName, uri, services, retentionCount, baos.toByteArray());
        return null;
    }

}
