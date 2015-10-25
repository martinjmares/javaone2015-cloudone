package we.love.pluto.visualizer;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.ServiceUnavailableException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.media.sse.EventOutput;
import org.glassfish.jersey.media.sse.OutboundEvent;
import org.glassfish.jersey.media.sse.SseBroadcaster;
import org.glassfish.jersey.media.sse.SseFeature;

import com.google.common.collect.Maps;

/**
 * TODO M: Distributed map.
 *
 * @author Michal Gajdos
 */
@Path("space-object")
public class SpaceObjectResource {

    private static final Map<String, AtomicInteger> occurrences = new ConcurrentHashMap<>();
    private static final SseBroadcaster broadcaster = new SseBroadcaster();

    @GET
    @Path("events")
    @Produces(SseFeature.SERVER_SENT_EVENTS)
    public EventOutput events() {
        final EventOutput eventOutput = new EventOutput();

        if (!broadcaster.add(eventOutput)) {
            // 503 -> 5s delayed client reconnect attempt.
            throw new ServiceUnavailableException(5L);
        }

        try {
            eventOutput.write(event());
        } catch (final IOException ioe) {
            // NO-OP.
        }

        return eventOutput;
    }

    @POST
    @Path("of-the-moment")
    public Response post(final String[] objects) {
        // Increment counter.
        for (String object : objects) {
            if (!occurrences.containsKey(object)) {
                occurrences.putIfAbsent(object, new AtomicInteger());
            }
            occurrences.get(object).incrementAndGet();
        }
        // Broadcast.
        broadcaster.broadcast(event());
        return Response.ok().build();
    }

    private OutboundEvent event() {
        final Map<String, AtomicInteger> occurrences = SpaceObjectResource.occurrences;

        return new OutboundEvent.Builder()
                .mediaType(MediaType.APPLICATION_JSON_TYPE)
                .data(Maps.transformValues(occurrences, AtomicInteger::intValue))
                .build();
    }
}
