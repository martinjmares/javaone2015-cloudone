package cloudone.client.internal;

import cloudone.ServiceFullName;
import cloudone.internal.ApplicationFullName;
import cloudone.internal.nimbostratus.CumulonimbusClient;
import org.glassfish.jersey.client.JerseyClient;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author Martin Mares (martin.mares at oracle.com)
 */
public abstract class InvokerBase {

    protected final JerseyClient client;
    protected final UriBuilder targetUri;
    protected final ServiceFullName[] serviceFullNames;
    protected final List<MediaType> acceptMediaTypes = new ArrayList<>();

    public InvokerBase(JerseyClient client, UriBuilder targetUri, ServiceFullName[] serviceFullNames) {
        this.client = client;
        this.targetUri = targetUri;
        this.serviceFullNames = serviceFullNames;
    }

    protected List<ApplicationFullName> findTargetApplications(String method) {
        String path = targetUri.build().getPath();
        List<ApplicationFullName> apps = CumulonimbusClient.getInstance().getApplicationsForPath(path, method);
        List<ApplicationFullName> result;
        if (serviceFullNames == null || serviceFullNames.length == 0) {
            result = apps;
        } else {
            result = new ArrayList<>();
            for (ApplicationFullName app : apps) {
                for (ServiceFullName serviceFullName : serviceFullNames) {
                    if (serviceFullName.accepts(app.getServiceName())) {
                        result.add(app);
                        break;
                    }
                }
            }
        }
        return result;
    }

    /**
     * Add the accepted response media types.
     *
     * @param responseMediaTypes accepted response media types.
     * @return the updated invoker.
     */
    public InvokerBase accept(String... responseMediaTypes) {
        if (responseMediaTypes != null) {
            MediaType[] types = new MediaType[responseMediaTypes.length];
            for (int i = 0; i < responseMediaTypes.length; i++) {
                types[i] = MediaType.valueOf(responseMediaTypes[i]);
            }
            accept(types);
        }
        return this;
    }

    /**
     * Add the accepted response media types.
     *
     * @param mediaTypes accepted response media types.
     * @return the updated invoker.
     */
    public InvokerBase accept(MediaType... mediaTypes) {
        if (mediaTypes != null) {
            acceptMediaTypes.addAll(Arrays.asList(mediaTypes));
        }
        return this;
    }
}
