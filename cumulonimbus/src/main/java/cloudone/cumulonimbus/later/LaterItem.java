package cloudone.cumulonimbus.later;

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import cloudone.ServiceFullName;
import cloudone.internal.ApplicationFullName;

/**
 * @author Martin Mares (martin.mares at oracle.com)
 */
public class LaterItem {

    private final String id;
    private final String contentType;
    private final String method;
    private final URI uri;
    private final ServiceFullName[] serviceFullNames;
    private final int retentionCount;
    private final byte[] payload;
    private final Set<ApplicationFullName> deliveredToApps = new HashSet<>();

    LaterItem(String id,
              String contentType,
              String method,
              URI uri,
              ServiceFullName[] serviceFullNames,
              int retentionCount,
              byte[] payload) {
        this.id = id;
        this.method = method;
        this.contentType = contentType;
        this.uri = uri;
        this.serviceFullNames = serviceFullNames;
        this.retentionCount = retentionCount;
        this.payload = payload;
    }

    public String getId() {
        return id;
    }

    public String getMethod() {
        return method;
    }

    public URI getUri() {
        return uri;
    }

    public ServiceFullName[] getServiceFullNames() {
        return serviceFullNames;
    }

    public int getRetentionCount() {
        return retentionCount;
    }

    public byte[] getPayload() {
        return payload;
    }

    public String getPath() {
        return uri.getPath();
    }

    public String getContentType() {
        return contentType;
    }

    public Collection<ApplicationFullName> filterApps(Collection<ApplicationFullName> apps) {
        if (serviceFullNames == null || serviceFullNames.length == 0) {
            return apps;
        }
        Collection<ApplicationFullName> result = new ArrayList<>();
        for (ApplicationFullName app : apps) {
            if (!deliveredToApps.contains(app)) {
                for (ServiceFullName serviceFullName : serviceFullNames) {
                    if (app.getServiceName().accepts(serviceFullName)) {
                        result.add(app);
                        break;
                    }
                }
            }
        }
        return result;
    }

    @Override
    public String toString() {
        return "LaterItem{" +
                "id='" + id + '\'' +
                ", contentType='" + contentType + '\'' +
                ", method='" + method + '\'' +
                ", uri=" + uri +
                ", serviceFullNames=" + Arrays.toString(serviceFullNames) +
                ", retentionCount=" + retentionCount +
                ", payload.size=" + (payload == null ? "null" : String.valueOf(payload.length)) +
                '}';
    }

    public void deliveredTo(ApplicationFullName application) {
        deliveredToApps.add(application);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof LaterItem)) {
            return false;
        }

        LaterItem item = (LaterItem) o;

        return id.equals(item.id);

    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
