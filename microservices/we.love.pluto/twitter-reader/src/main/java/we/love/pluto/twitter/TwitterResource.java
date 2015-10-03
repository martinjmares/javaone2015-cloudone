package we.love.pluto.twitter;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import javax.inject.Inject;

/**
 * @author Martin Mares (martin.mares at oracle.com)
 * @author Michal Gajdos
 */
@Path("tweet")
public class TwitterResource {

    @Inject
    private DataAggregator twitter;

    @GET
    @Produces("text/plain")
    public String lastTweet() {
        return twitter.lastMessage();
    }

    @POST
    @Consumes("text/plain")
    public void postTweet(String message) {
        twitter.message(message);
    }

}
