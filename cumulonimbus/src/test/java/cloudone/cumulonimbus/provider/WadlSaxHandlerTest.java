package cloudone.cumulonimbus.provider;

import cloudone.cumulonimbus.model.RestResourceDescription;
import cloudone.cumulonimbus.model.ServiceRestResources;
import org.junit.Test;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.InputStream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * @author Martin Mares (martin.mares at oracle.com)
 */
public class WadlSaxHandlerTest {

    private static final SAXParserFactory parserFactor = SAXParserFactory.newInstance();

    @Test
    public void testParsing() throws Exception {
        WadlSaxHandler handler = new WadlSaxHandler();
        try (InputStream is = this.getClass().getClassLoader().getResourceAsStream("application1.wadl"); ) {
            SAXParser parser = parserFactor.newSAXParser();
            parser.parse(is, handler);
        }
        ServiceRestResources srr = handler.toServiceRestResource();
        assertNotNull(srr);
        assertEquals(8, srr.getResources().size());
        assertTrue(srr.getResources().contains(new RestResourceDescription(RestResourceDescription.Method.DELETE,
                "/service/{group}/{application}/{version}/{instance}")));
        assertTrue(srr.getResources().contains(new RestResourceDescription(RestResourceDescription.Method.GET,
                "/lifecycle/uptime")));
        assertTrue(srr.getResources().contains(new RestResourceDescription(RestResourceDescription.Method.GET,
                "/service")));
    }
}