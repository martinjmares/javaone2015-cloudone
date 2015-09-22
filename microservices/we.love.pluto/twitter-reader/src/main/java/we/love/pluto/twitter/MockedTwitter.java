package we.love.pluto.twitter;

import cloudone.client.C1Client;
import cloudone.client.C1ClientBuilder;
import cloudone.client.MultiResponse;
import org.glassfish.jersey.gson.GsonFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.GenericType;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.StreamSupport;

/**
 * Mocked implementation of twitter client. Reads from file. :-)
 *
 * @author Martin Mares (martin.mares at oracle.com)
 */
public class MockedTwitter {

    private static final Logger LOGGER = LoggerFactory.getLogger(MockedTwitter.class);
    private static final C1Client CLIENT = (new C1ClientBuilder()).build().register(GsonFeature.class);

    private final File file;

    private volatile long timestamp = -1;
    private volatile String message = null;

    public MockedTwitter(File file) {
        LOGGER.info("Read tweets from file " + file.getPath());
        this.file = file;
    }

    public MockedTwitter(String fileName) {
        if (fileName == null) {
            throw new IllegalArgumentException("Argument fileName cannot be null!");
        }
        file = new File(fileName);
        LOGGER.info("Read tweets from file " + file.getPath());
    }

    public String getLastMessage() {
        return message;
    }

    private String readFile() {
        //file.lastModified();
        if (!file.exists()) {
            return null;
        }
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (FileInputStream is = new FileInputStream(file)) {
            byte[] buff = new byte[64];
            int ind = -1;
            while ((ind = is.read(buff)) > 0) {
                baos.write(buff, 0, ind);
            }
            baos.close();
            return baos.toString();
        } catch (IOException e) {
            LOGGER.error("Cannot read file " + file.getPath(), e);
            return null;
        }
    }

    synchronized void update() {
        if (file.exists() && timestamp < file.lastModified()) {
            LOGGER.info("Reading file.");
            timestamp = file.lastModified();
            message = readFile();
            if (message != null) {
                fireUpdate();
            }
        }
    }

    public synchronized void setMessage(String message) {
        LOGGER.info("setMessage(): " + message);
        timestamp = System.currentTimeMillis();
        this.message = message;
        if (message != null) {
            fireUpdate();
        }
    }

    private synchronized void fireUpdate() {
        try {
            LOGGER.info("fireUpdate: " + message);
            MultiResponse responses = CLIENT.target()
                                            .path("/universe")
                                            .all()
                                            .post(Entity.text(message));
            List<String> finded = new ArrayList<>();
            StreamSupport.stream(responses.spliterator(), false)
                    .filter(res -> res.getError() == null && res.getResponse() != null)
                    .map(res -> res.getResponse().readEntity(new GenericType<ArrayList<String>>() {}))
                    .filter(strings -> strings != null)
                    .forEach(strings -> finded.addAll(strings));
            LOGGER.info("fireUpdate: FOUND: " + finded);
            if (!finded.isEmpty()) {
                CLIENT.target()
                        .path("/spaceobject/ofthemoment")
                        .all()
                        .post(Entity.text(finded.get(finded.size() - 1)));
            }
        } catch (Exception exc) {
            LOGGER.error("fireUpdate() exception: ", exc);
        }
    }
}
