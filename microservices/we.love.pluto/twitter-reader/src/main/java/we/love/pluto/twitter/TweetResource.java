package we.love.pluto.twitter;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

/**
 * @author Martin Mares (martin.mares at oracle.com)
 */
@Path("/tweet")
public class TweetResource {

    private final TwitterReaderApp app;

    public TweetResource(TwitterReaderApp app) {
        this.app = app;
    }

    @GET
    @Produces("text/plain")
    public String getTweet() {
        return app.getTwitter().getLastMessage();
    }

    @POST
    @Consumes("text/plain")
    public void setTweet(String message) {
        app.getTwitter().setMessage(message);
    }

}
