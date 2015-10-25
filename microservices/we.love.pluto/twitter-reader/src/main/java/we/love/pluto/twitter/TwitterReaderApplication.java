package we.love.pluto.twitter;

import java.io.File;
import java.io.FileInputStream;
import java.util.Collections;
import java.util.Properties;
import java.util.Set;

import org.glassfish.hk2.utilities.binding.AbstractBinder;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;

import cloudone.C1Application;
import cloudone.C1Services;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Martin Mares (martin.mares at oracle.com)
 * @author Michal Gajdos
 */
public class TwitterReaderApplication extends C1Application {

    private static final Logger LOGGER = LoggerFactory.getLogger(TwitterReaderApplication.class);

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
            if (properties.getProperty("twitter.consumer.secret") == null || properties.getProperty("twitter.consumer.secret").isEmpty()
                    || properties.getProperty("twitter.consumer.key") == null || properties.getProperty("twitter.consumer.key").isEmpty()
                    || properties.getProperty("twitter.token.secret") == null || properties.getProperty("twitter.token.secret").isEmpty()
                    || properties.getProperty("twitter.token.key") == null || properties.getProperty("twitter.token.key").isEmpty()) {
                LOGGER.warn("CAN not START twitter reader because provided properties file does not contain mandatory keys!");
            } else {
                realTwitter = new TwitterAggregator(
                        properties.getProperty("twitter.consumer.secret"),
                        properties.getProperty("twitter.consumer.key"),
                        properties.getProperty("twitter.token.secret"),
                        properties.getProperty("twitter.token.key"));
            }
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

        twitter.listener(new NewTweetListener());
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

}
