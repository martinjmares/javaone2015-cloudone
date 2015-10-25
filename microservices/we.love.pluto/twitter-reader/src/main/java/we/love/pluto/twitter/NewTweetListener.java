package we.love.pluto.twitter;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.GenericType;

import cloudone.client.C1Client;
import cloudone.client.C1ClientBuilder;
import cloudone.client.MultiResponse;
import org.glassfish.jersey.gson.GsonFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Martin Mares (martin.mares at oracle.com)
 */
class NewTweetListener implements DataListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(NewTweetListener.class);

    private static final C1Client client = new C1ClientBuilder().build()
            .register(GsonFeature.class);

    @Override
    @SuppressWarnings("ThrowableResultOfMethodCallIgnored")
    public void onNext(String message, String username) {
        LOGGER.info("TWEET: " + message);

        // Ping everyone in the universe and see if they want to hear news.
        final MultiResponse responses = client.target()
                .path("/universe")
                .all()
                .post(Entity.text(message));

        final List<String> found = StreamSupport.stream(responses.spliterator(), false)
                // Not interested in error states ...
                .filter(res -> res.getError() == null)
                        // ... or response-less answers.
                .filter(response -> response.getResponse() != null)
                        // Read response as list of strings.
                .flatMap(response -> response.getResponse()
                        .readEntity(new GenericType<List<String>>() { }).stream())
                        // Make a list from the stream.
                .collect(Collectors.toList());

        LOGGER.info("onNext: Found Responses in the cloud - " + found);

        if (!found.isEmpty()) {
            client.target()
                    .path("/space-object/of-the-moment")
                    .queryParam("user", username)
                    .all()
                    .later()
                    .retentionCount(1)
                    .post(Entity.json(found));
        }
    }
}
