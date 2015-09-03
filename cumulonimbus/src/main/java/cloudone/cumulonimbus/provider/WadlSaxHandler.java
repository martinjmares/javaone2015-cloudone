package cloudone.cumulonimbus.provider;

import cloudone.cumulonimbus.model.HttpMethod;
import cloudone.cumulonimbus.model.RestResourceDescription;
import cloudone.cumulonimbus.model.ServiceRestResources;
import cloudone.cumulonimbus.util.PathUtil;
import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;

/**
 * Process WADL file - just parts which are important for this project.
 *
 * @author Martin Mares (martin.mares at oracle.com)
 */
public class WadlSaxHandler extends DefaultHandler {

    private class MultiMethodResource {

        private final String path;
        private final Collection<HttpMethod> methods = new ArrayList<>();

        public MultiMethodResource(String path) {
            this.path = PathUtil.normalizePath(path);
        }

        public MultiMethodResource(MultiMethodResource mmr, String path) {
            if (mmr == null) {
                this.path = PathUtil.normalizePath(path);
            } else {
                this.path = PathUtil.normalizePath(mmr.path + PathUtil.normalizePath(path));
            }
        }

        public void addMethod(HttpMethod method) {
            if (method != null) {
                methods.add(method);
            }
        }

        public void addMethod(String method) {
            if (method != null) {
                addMethod(HttpMethod.valueOf(method));
            }
        }

    }

    private final Deque<MultiMethodResource> stack = new ArrayDeque<>();
    private final ArrayList<RestResourceDescription> processed = new ArrayList<>();

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) {
        switch (qName) {
            case "resource":
                stack.push(new MultiMethodResource(stack.peek(), attributes.getValue("path")));
                break;
            case "method":
                stack.peek().addMethod(attributes.getValue("name"));
                break;
        }
    }

    @Override
    public void endElement(String uri, String localName, String qName) {
        if ("resource".equals(qName)) {
            MultiMethodResource mmr = stack.pop();
            for (HttpMethod method : mmr.methods) {
                processed.add(new RestResourceDescription(method, mmr.path));
            }
        }
    }

    public ServiceRestResources toServiceRestResource() {
        return new ServiceRestResources(null, processed);
    }

}
