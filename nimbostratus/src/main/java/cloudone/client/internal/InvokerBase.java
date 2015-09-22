package cloudone.client.internal;

import cloudone.ServiceFullName;
import cloudone.internal.ApplicationFullName;
import cloudone.internal.nimbostratus.CumulonimbusClient;
import org.glassfish.jersey.client.JerseyClient;
import org.glassfish.jersey.client.JerseyInvocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author Martin Mares (martin.mares at oracle.com)
 */
public abstract class InvokerBase {

    private static final Logger LOGGER = LoggerFactory.getLogger(InvokerBase.class);

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

    protected abstract boolean isResponseCodeAcceptable(int code);

    protected Response method(ApplicationFullName targetApplication, String name, Entity<?> entity) {
        int port = LoadBalancer.getInstance().getPort(targetApplication);
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("method(" + targetApplication + ", " + name + "): port: " + port);
        }
        long startAt = -1;
        if (port > 0) {
            UriBuilder uriBuilder = targetUri
                    .clone()
                    .scheme("http")
                    .host("localhost")
                    .port(port);
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("method(" + targetApplication + ", " + name + "): URI: " + uriBuilder.build());
            }
            try {
                JerseyInvocation.Builder request = client.target(uriBuilder.build()).request();
                for (MediaType accept : acceptMediaTypes) {
                    request = request.accept(accept);
                }
                JerseyInvocation invocation;
                if (entity == null) {
                    invocation = request.build(name);
                } else {
                    invocation = request.build(name, entity);
                }
                startAt = System.currentTimeMillis();
                Response response = invocation.invoke();
                if (isResponseCodeAcceptable(response.getStatus())) {
                    return response;
                }
            } catch (WebApplicationException wExc) {
                if (isResponseCodeAcceptable(wExc.getResponse().getStatus())) {
                    throw wExc;
                }
            } catch (Exception e) {
                //TODO: Provide support for balancing to another endpoint (another endpoint of the same cluster).
                LOGGER.warn("Cannot reach application " + targetApplication + " on port " + port, e);
            } finally {
                if (startAt > 0) {
                    LoadBalancer.getInstance().updateStats(targetApplication, port, System.currentTimeMillis() - startAt);
                }
            }
        }
        return null;
    }
}
