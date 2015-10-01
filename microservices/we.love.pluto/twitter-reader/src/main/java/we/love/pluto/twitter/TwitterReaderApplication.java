package we.love.pluto.twitter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.StreamSupport;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.GenericType;

import javax.annotation.PreDestroy;

import org.glassfish.jersey.gson.GsonFeature;

import org.glassfish.hk2.utilities.binding.AbstractBinder;

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
 */
public class TwitterReaderApplication extends C1Application {

    private static final Logger LOGGER = LoggerFactory.getLogger(TwitterReaderApplication.class);
    private static final String FILE_OPTION = "file";

    private DataAggregator twitter;

    @Override
    public Options getOptions() {
        // TODO M: Options - more possibilities.

        return new Options()
                .addOption(null, FILE_OPTION, true, "File with tweets");
    }

    @Override
    public void init() throws Exception {
        final String fileName = C1Services.getInstance().getRuntimeInfo()
                .getCommandLine()
                .getOptionValue(FILE_OPTION);

        // TODO M: Aggregator.
        if (fileName == null) {
            twitter = new TwitterAggregator();
        } else {
            twitter = new MockedTwitter(fileName);
        }

        // TODO M: Make this more presentable.
        final C1Client client = new C1ClientBuilder().build().register(GsonFeature.class);
        twitter.listener(message -> {
            LOGGER.info("fireUpdate: " + message);
            MultiResponse responses = client.target()
                    .path("/universe")
                    .all()
                    .post(Entity.text(message));
            List<String> found = new ArrayList<>();
            StreamSupport.stream(responses.spliterator(), false)
                    .filter(res -> res.getError() == null && res.getResponse() != null)
                    .map(res -> res.getResponse().readEntity(new GenericType<ArrayList<String>>() {}))
                    .filter(strings -> strings != null)
                    .forEach(strings -> found.addAll(strings));
            LOGGER.info("fireUpdate: FOUND: " + found);
            if (!found.isEmpty()) {
                client.target()
                        .path("/spaceobject/ofthemoment")
                        .all()
                        .post(Entity.text(found.get(found.size() - 1)));
            }
        }).start("oracle"); // TODO M: configurable keywords.
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

    @PreDestroy
    public void release() {
        if (twitter != null) {
            twitter.stop();
        }
    }
}
