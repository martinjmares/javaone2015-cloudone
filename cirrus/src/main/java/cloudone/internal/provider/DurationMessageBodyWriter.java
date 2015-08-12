package cloudone.internal.provider;

import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.time.Duration;
import java.time.temporal.ChronoUnit;

/**
 * @author Martin Mares (martin.mares at oracle.com)
 */
@Produces("text/plain")
public class DurationMessageBodyWriter  implements MessageBodyWriter<Duration> {
    @Override
    public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        return Duration.class.isAssignableFrom(type);
    }

    @Override
    public long getSize(Duration duration, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        return 0;
    }

    @Override
    public void writeTo(Duration duration, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType, MultivaluedMap<String, Object> httpHeaders, OutputStream entityStream) throws IOException, WebApplicationException {
        if (duration == null) {
            entityStream.write("00:00:00,000".getBytes());
            return;
        }
        StringBuilder result = new StringBuilder(20);
        long dur = duration.toMillis();
        //Days
        long l = dur / ChronoUnit.DAYS.getDuration().toMillis();
        if (l > 0) {
            result.append(l).append("D ");
            dur -= l * ChronoUnit.DAYS.getDuration().toMillis();
        }
        //Hours
        l = dur / ChronoUnit.HOURS.getDuration().toMillis();
        if (l < 10) {
            result.append('0');
        }
        result.append(l).append(':');
        dur -= l * ChronoUnit.HOURS.getDuration().toMillis();
        //Minutes
        l = dur / ChronoUnit.MINUTES.getDuration().toMillis();
        if (l < 10) {
            result.append('0');
        }
        result.append(l).append(':');
        dur -= l * ChronoUnit.MINUTES.getDuration().toMillis();
        //Seconds
        l = dur / ChronoUnit.SECONDS.getDuration().toMillis();
        if (l < 10) {
            result.append('0');
        }
        result.append(l).append(',');
        dur -= l * ChronoUnit.SECONDS.getDuration().toMillis();
        //Millis
        if (dur < 100) {
            result.append('0');
            if (dur < 10) {
                result.append('0');
            }
        }
        result.append(dur);
        //WRITE IT
        entityStream.write(result.toString().getBytes());
    }
}
