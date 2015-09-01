package cloudone.cumulonimbus.provider;

import cloudone.cumulonimbus.model.ServiceRestResources;
import org.xml.sax.SAXException;

import javax.ws.rs.Consumes;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyReader;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

/**
 * Parse WADL to List of provided resources.
 *
 * @author Martin Mares (martin.mares at oracle.com)
 */
@Consumes("application/vnd.sun.wadl+xml")
public class ServiceRestResourcesProvider implements MessageBodyReader<ServiceRestResources> {

    private static final SAXParserFactory parserFactor = SAXParserFactory.newInstance();

    @Override
    public boolean isReadable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        return ServiceRestResources.class.isAssignableFrom(type);
    }

    @Override
    public ServiceRestResources readFrom(Class<ServiceRestResources> type, Type genericType, Annotation[] annotations, MediaType mediaType, MultivaluedMap<String, String> httpHeaders, InputStream entityStream) throws IOException, WebApplicationException {
        try {
            SAXParser parser = parserFactor.newSAXParser();
            WadlSaxHandler handler = new WadlSaxHandler();
            parser.parse(entityStream, handler);
            return handler.toServiceRestResource();
        } catch (ParserConfigurationException e) {
            throw new RuntimeException(e);
        } catch (SAXException e) {
            throw new WebApplicationException("Cannot parse WADL data.", e, 422);
        }

    }
}
