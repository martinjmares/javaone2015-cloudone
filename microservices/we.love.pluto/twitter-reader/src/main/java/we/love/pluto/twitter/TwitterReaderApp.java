package we.love.pluto.twitter;

import cloudone.C1Application;
import cloudone.C1Services;
import org.apache.commons.cli.Options;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * @author Martin Mares (martin.mares at oracle.com)
 */
public class TwitterReaderApp extends C1Application {

    private static final Logger LOGGER = LoggerFactory.getLogger(TwitterReaderApp.class);
    private static final String FILE_OPTION = "file";

    private MockedTwitter twitter;
    private final TweetResource tweetResource;

    public TwitterReaderApp() {
        tweetResource = new TweetResource(this);
    }

    @Override
    public Options getOptions() {
        Options result = new Options();
        result.addOption(null, FILE_OPTION, true, "File with tweets");
        return result;
    }

    @Override
    public void init() throws Exception {
        String fileName = C1Services.getInstance().getRuntimeInfo().getCommandLine().getOptionValue(FILE_OPTION);
        if (fileName == null) {
            throw new Exception("Missing command line parameter " + FILE_OPTION + "!");
        }
        twitter = new MockedTwitter(fileName);
        C1Services.getInstance()
                .getScheduledExecutorService()
                .schedule(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            getTwitter().update();
                        } finally {
                            C1Services.getInstance()
                                    .getScheduledExecutorService()
                                    .schedule(this, 5, TimeUnit.SECONDS);
                        }
                    }
                }, 5, TimeUnit.SECONDS);
    }

    @Override
    public Set<Object> getSingletons() {
        return new HashSet<>(Arrays.asList(tweetResource));
    }

    public MockedTwitter getTwitter() {
        return twitter;
    }
}
