package cloudone.cumulonimbus.provider;

import cloudone.cumulonimbus.model.RestResourceDescription;
import cloudone.cumulonimbus.model.ServiceRestResources;
import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
import java.util.regex.Pattern;

/**
 * Process WADL file - just parts which are important for this project.
 *
 * @author Martin Mares (martin.mares at oracle.com)
 */
public class WadlSaxHandler extends DefaultHandler {

    private class MultiMethodResource {

        private final String path;
        private final Collection<RestResourceDescription.Method> methods = new ArrayList<>();

        public MultiMethodResource(String path) {
            this.path = normalizePath(path);
        }

        public MultiMethodResource(MultiMethodResource mmr, String path) {
            if (mmr == null) {
                this.path = normalizePath(path);
            } else {
                this.path = normalizePath(mmr.path + normalizePath(path));
            }
        }

        public void addMethod(RestResourceDescription.Method method) {
            if (method != null) {
                methods.add(method);
            }
        }

        public void addMethod(String method) {
            if (method != null) {
                addMethod(RestResourceDescription.Method.valueOf(method));
            }
        }

    }

    private static final Pattern MULTISLASHES_PATTERN = Pattern.compile("/{2,}");

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
            for (RestResourceDescription.Method method : mmr.methods) {
                processed.add(new RestResourceDescription(method, mmr.path));
            }
        }
    }

    public ServiceRestResources toServiceRestResource() {
        return new ServiceRestResources(null, processed);
    }

    private static String normalizePath(String path) {
        if (path == null || path.length() == 0) {
            path = "/";
        } else if (!path.startsWith("/")) {
            path = "/" + path.trim();
        } else {
            path = path.trim();
        }
        if (path.length() > 1 && path.endsWith("/")) {
            path = path.substring(0, path.length() - 1);
        }
        path = MULTISLASHES_PATTERN.matcher(path).replaceAll("/");
        return path;
    }
}
