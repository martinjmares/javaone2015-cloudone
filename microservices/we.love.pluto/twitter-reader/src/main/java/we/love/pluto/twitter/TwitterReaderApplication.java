package we.love.pluto.twitter;

import java.io.File;
import java.io.FileInputStream;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.GenericType;

import org.glassfish.jersey.gson.GsonFeature;

import org.glassfish.hk2.utilities.binding.AbstractBinder;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cloudone.C1Application;
import cloudone.C1Services;
import cloudone.client.C1Client;
import cloudone.client.C1ClientBuilder;
import cloudone.client.MultiResponse;

/**
 * @author Martin Mares (martin.mares at oracle.com)
 * @author Michal Gajdos
 */
public class TwitterReaderApplication extends C1Application {

    private static final String MOCK_FILE_OPTION = "mockFile";
    private static final String TWITTER_FILE_OPTION = "twitterApiFile";
    private static final String TWITTER_KEYWORDS = "keywords";

    private DataAggregator twitter;

    @Override
    public Options getOptions() {
        return new Options()
                .addOption(null, MOCK_FILE_OPTION, true, "(Mock) Twitter file with tweets.")
                .addOption(null, TWITTER_FILE_OPTION, true, "Twitter API properties (Consumer and Access tokens).")
                .addOption(null, TWITTER_KEYWORDS, true, "Keywords to look for in the twitter stream.");
    }

    @Override
    public void init() throws Exception {
        final CommandLine cmd = C1Services.getInstance().getRuntimeInfo().getCommandLine();

        // Mock Twitter.
        final String mockFile = cmd.getOptionValue(MOCK_FILE_OPTION);
        final DataAggregator mockTwitter = mockFile == null ? null : new MockedTwitter(mockFile);

        // Real Twitter.
        final String apiFileName = cmd.getOptionValue(TWITTER_FILE_OPTION, "twitter-api.properties");
        final File apiFile = new File(apiFileName);
        DataAggregator realTwitter = null;
        if (apiFile.exists()) {
            final Properties properties = new Properties();
            properties.load(new FileInputStream(apiFile));

            realTwitter = new TwitterAggregator(
                    properties.getProperty("twitter.consumer.secret"),
                    properties.getProperty("twitter.consumer.key"),
                    properties.getProperty("twitter.token.secret"),
                    properties.getProperty("twitter.token.key"));
        }

        if (realTwitter != null && mockTwitter != null) {
            twitter = new CombinedAggregator(realTwitter, mockTwitter);
        } else if (realTwitter != null) {
            twitter = realTwitter;
        } else if (mockTwitter != null) {
            twitter = mockTwitter;
        } else {
            throw new IllegalStateException("No twitter client is configured.");
        }

        twitter.listener(new CloudOneListener());
    }

    @Override
    public void started() {
        if (twitter != null) {
            twitter.start(C1Services.getInstance()
                    .getRuntimeInfo()
                    .getCommandLine()
                    .getOptionValue(TWITTER_KEYWORDS, "javaone").split(","));
        }
    }

    @Override
    public void shutDown() {
        if (twitter != null) {
            twitter.stop();
        }
    }

    @Override
    public Set<Class<?>> getClasses() {
        return Collections.singleton(TwitterResource.class);
    }

    @Override
    public Set<Object> getSingletons() {
        return Collections.singleton(new AbstractBinder() {
            @Override
            protected void configure() {
                bind(twitter).to(DataAggregator.class);
            }
        });
    }

    private static class CloudOneListener implements DataListener {

        private static final Logger LOGGER = LoggerFactory.getLogger(CloudOneListener.class);

        private static final C1Client client = new C1ClientBuilder().build().register(GsonFeature.class);

        @Override
        @SuppressWarnings("ThrowableResultOfMethodCallIgnored")
        public void onNext(final String message) {
            LOGGER.info("onNext: " + message);

            // Ping everyone in the universe and see if they want to hear news.
            final MultiResponse responses = client.target()
                    .path("/universe")
                    .all()
                    .post(Entity.text(message));

            final List<String> found = StreamSupport.stream(responses.spliterator(), false)
                    // Not interested in error states ...
                    .filter(res -> res.getError() == null)
                    // ... or response-less answers. TODO ???
                    .filter(response -> response.getResponse() != null)
                    // Read response as list of strings.
                    .flatMap(response -> response.getResponse().readEntity(new GenericType<List<String>>() {}).stream())
                    // Make a list from the stream.
                    .collect(Collectors.toList());

            LOGGER.info("onNext: Found Responses in the cloud - " + found);

            if (!found.isEmpty()) {
                client.target()
                        .path("/spaceobject/ofthemoment")
                        .all()
                        .post(Entity.text(found.get(found.size() - 1)));
            }
        }
    }
}
