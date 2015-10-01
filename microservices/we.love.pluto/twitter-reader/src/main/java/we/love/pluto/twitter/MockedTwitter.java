package we.love.pluto.twitter;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cloudone.C1Services;

/**
 * Mocked implementation of twitter client. Reads from file. :-)
 *
 * @author Martin Mares (martin.mares at oracle.com)
 */
class MockedTwitter extends AbstractAggregator {

    private static final Logger LOGGER = LoggerFactory.getLogger(MockedTwitter.class);

    private final File file;

    private volatile long timestamp = -1;
    private volatile Future<?> checker;

    MockedTwitter(String fileName) {
        if (fileName == null) {
            throw new IllegalArgumentException("Argument fileName cannot be null!");
        }
        file = new File(fileName);
        LOGGER.info("Read tweets from file " + file.getPath());
    }

    @Override
    public DataAggregator start(final String... keywords) {
        checker = C1Services.getInstance()
                .getScheduledExecutorService()
                .scheduleAtFixedRate(this::update, 0, 5, TimeUnit.SECONDS);

        return this;
    }

    @Override
    public void stop() {
        if (checker != null) {
            checker.cancel(true);
        }
    }

    private String readFile() {
        if (!file.exists()) {
            return null;
        }

        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (FileInputStream is = new FileInputStream(file)) {
            final byte[] buff = new byte[256];

            int read;
            while ((read = is.read(buff)) > 0) {
                baos.write(buff, 0, read);
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
            final String message = readFile();

            if (message != null) {
                message(message);
            }
        }
    }
}
