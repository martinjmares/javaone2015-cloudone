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

import cloudone.C1Services;
import twitter4j.Status;
import twitter4j.StatusAdapter;

/**
 * TODO M: Authentication.
 */
class TwitterAggregator extends AbstractAggregator {

    private volatile Twitter4jStatusClient client;
    private volatile String lastMessage;

    @Override
    public TwitterAggregator start(final String... keywords) {
        final BlockingQueue<String> queue = new LinkedBlockingQueue<>(10000);

        final Authentication auth = new OAuth1(
                "TODO",
                "TODO",
                "TODO",
                "TODO");

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

    @Override
    public String lastMessage() {
        return lastMessage;
    }

    private class TwitterMessageListener extends StatusAdapter {

        @Override
        public void onStatus(final Status status) {
            lastMessage = status.getText();

            listeners().forEach(listener -> listener.onNext(lastMessage));
        }
    }
}
