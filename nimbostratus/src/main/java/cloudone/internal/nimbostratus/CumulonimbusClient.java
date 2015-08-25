package cloudone.internal.nimbostratus;

import cloudone.C1Services;
import org.glassfish.jersey.gson.GsonFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Java API for rest cumulonimbus services.
 *
 * @author Martin Mares (martin.mares at oracle.com)
 */
public class CumulonimbusClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(CumulonimbusClient.class);

    private static class InstanceHolder {
        private static CumulonimbusClient INSTANCE = new CumulonimbusClient();
    }

    private volatile int port;

    private final Client client = ClientBuilder.newClient()
                                    .register(GsonFeature.class);

    private CumulonimbusClient() {
        reloadPort();
    }

    private synchronized void reloadPort() {
        //Currently is cumulonimbus just singleton
        File dir = new File(C1Services.getInstance().getRuntimeInfo().getHomeDirectory(), "cumulonimbus");
        if (dir.exists()) {
            File f = new File(dir, "port.txt");
            if (f.exists()) {
                try (InputStream is = new FileInputStream(f)) {
                    byte[] buff = new byte[8];
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    int len;
                    while ((len = is.read(buff)) > 0) {
                        baos.write(buff, 0, len);
                    }
                    this.port = Integer.parseInt(baos.toString().trim());
                } catch (IOException e) {
                    LOGGER.warn("Can not open file with cumulonimbus port number. Set default 4242.");
                    this.port = 4242;
                } catch (NumberFormatException nfe) {
                    throw new RuntimeException("Can NOT parse cumulonimbus port number!");
                }
            } else {
                LOGGER.info("Can not locate cumulonimbus port file. Use default 4242 port.");
                this.port = 4242;
            }
        } else {
            LOGGER.info("Can not locate cumulonimbus configuration directory. Use default 4242.");
            this.port = 4242;
        }
    }

    private WebTarget getWebTarget() {
        return client.target("http://localhost:" + this.port);
    }

    public int reservePort(boolean admin) throws RuntimeException {
        WebTarget target = getWebTarget()
                                .path("/configuration/reserve-port");
        if (admin) {
            target = target.queryParam("scope", "admin");
        }
        return target
                .request(MediaType.TEXT_PLAIN)
                .get(Integer.class);
    }

    private static CumulonimbusClient getInstance() {
        return InstanceHolder.INSTANCE;
    }
}
