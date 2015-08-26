package cloudone.internal.nimbostratus;

import cloudone.C1Services;
import cloudone.ServiceFullName;
import cloudone.internal.dto.PortInfo;
import cloudone.internal.dto.RuntimeIdAndSecCode;
import org.glassfish.jersey.gson.GsonFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.function.Function;

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

    private final Client client;
    private volatile WebTarget target;
    private RuntimeIdAndSecCode registration;

    private CumulonimbusClient() {
        client = ClientBuilder.newClient()
                .register(GsonFeature.class);
        reloadWebTarget();
    }

    private synchronized void reloadWebTarget() {
        int port = 4242; //Default value
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
                    port = Integer.parseInt(baos.toString().trim());
                } catch (IOException e) {
                    LOGGER.warn("Can not open file with cumulonimbus port number. Set default 4242.");
                } catch (NumberFormatException nfe) {
                    throw new RuntimeException("Can NOT parse cumulonimbus port number!");
                }
            } else {
                LOGGER.info("Can not locate cumulonimbus port file. Use default 4242 port.");
            }
        } else {
            LOGGER.info("Can not locate cumulonimbus configuration directory. Use default 4242.");
        }
        this.target = client.target("http://localhost:" + port);
    }

    private WebTarget addServiceFullName(WebTarget target, ServiceFullName name) {
        if (name == null) {
            name = C1Services.getInstance().getRuntimeInfo().getServiceFullName();
        }
        return target
                .path(name.getGroupId())
                .path(name.getArtifactId())
                .path(name.getVersion());
    }

    private <T> T cumulonimbusCall(final Function<WebTarget, T> f) {
        return f.apply(target);
    }

    public int reservePort(final boolean admin) throws RuntimeException {
        return cumulonimbusCall(target -> {
            target = target.path("/configuration/reserve-port");
            if (admin) {
                target = target.queryParam("scope", "admin");
            }
            return target
                    .request(MediaType.TEXT_PLAIN)
                    .accept(MediaType.TEXT_PLAIN)
                    .get(Integer.class);
        });
    }

    public synchronized RuntimeIdAndSecCode register() throws RuntimeException {
        final PortInfo portInfo = new PortInfo(C1Services.getInstance().getRuntimeInfo());
        this.registration = cumulonimbusCall(target -> {
                                return addServiceFullName(target.path("service"), null)
                                            .request(MediaType.APPLICATION_JSON)
                                            .accept(MediaType.APPLICATION_JSON)
                                            .put(Entity.json(portInfo), RuntimeIdAndSecCode.class);
                            });
        return this.registration;
    }

    public synchronized void unregister() throws RuntimeException {
        if (registration == null) {
            throw new RuntimeException("Can not unregiter service without registration.");
        }
        cumulonimbusCall(target -> {
            return addServiceFullName(target.path("service"), null)
                    .path(String.valueOf(registration.getRuntimeId()))
                    .queryParam("seccode", registration.getSecurityCode())
                    .request()
                    .accept(MediaType.APPLICATION_JSON)
                    .delete();
        });
        registration = null;
    }

    public static CumulonimbusClient getInstance() {
        return InstanceHolder.INSTANCE;
    }
}
