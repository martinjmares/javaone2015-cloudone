package we.love.pluto.twitter;

import java.util.Collections;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import com.google.common.collect.Lists;

import com.twitter.hbc.ClientBuilder;
import com.twitter.hbc.core.Constants;
import com.twitter.hbc.core.endpoint.StatusesFilterEndpoint;
import com.twitter.hbc.core.processor.StringDelimitedProcessor;
import com.twitter.hbc.httpclient.auth.Authentication;
import com.twitter.hbc.httpclient.auth.OAuth1;
import com.twitter.hbc.twitter4j.Twitter4jStatusClient;
import static com.google.common.base.Preconditions.checkNotNull;

import cloudone.C1Services;
import twitter4j.Status;
import twitter4j.StatusAdapter;

/**
 * @author Michal Gajdos
 */
final class TwitterAggregator extends AbstractAggregator {

    private final String consumerKey;
    private final String consumerSecret;
    private final String token;
    private final String tokenSecret;

    private volatile Twitter4jStatusClient client;

    public TwitterAggregator(final String consumerKey,
                             final String consumerSecret,
                             final String token,
                             final String tokenSecret) {
        checkNotNull(consumerKey);
        checkNotNull(consumerSecret);
        checkNotNull(token);
        checkNotNull(tokenSecret);

        this.consumerKey = consumerKey;
        this.consumerSecret = consumerSecret;
        this.token = token;
        this.tokenSecret = tokenSecret;
    }

    @Override
    public TwitterAggregator start(final String... keywords) {
        final BlockingQueue<String> queue = new LinkedBlockingQueue<>(10000);

        final Authentication auth = new OAuth1(
                consumerKey,
                consumerSecret,
                token,
                tokenSecret);

        // Create a new BasicClient. By default gzip is enabled.
        final ClientBuilder builder = new ClientBuilder()
                .hosts(Constants.STREAM_HOST)
                .endpoint(new StatusesFilterEndpoint().trackTerms(Lists.newArrayList(keywords)))
                .authentication(auth)
                .processor(new StringDelimitedProcessor(queue));

        // Wrap our BasicClient with the twitter4j client
        client = new Twitter4jStatusClient(
                builder.build(),
                queue,
                Collections.singletonList(new TwitterMessageListener()),
                C1Services.getInstance().getExecutorService());

        client.connect();
        client.process();

        return this;
    }

    @Override
    public void stop() {
        if (client != null) {
            client.stop();
        }
    }

    private class TwitterMessageListener extends StatusAdapter {

        @Override
        public void onStatus(final Status status) {
            message(status.getText());
        }
    }
}
