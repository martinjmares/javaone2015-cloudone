package cloudone.client.internal;

import cloudone.ServiceFullName;
import cloudone.client.AnyInvoker;
import cloudone.client.C1WebTarget;
import cloudone.client.LaterAnyInvoker;
import cloudone.internal.ApplicationFullName;
import org.glassfish.jersey.client.JerseyClient;
import org.glassfish.jersey.client.JerseyInvocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import javax.ws.rs.HttpMethod;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import java.util.List;

/**
 * @author Martin Mares (martin.mares at oracle.com)
 */
public class AnyInvokerImpl extends InvokerBase implements AnyInvoker {

    private static final Logger LOGGER = LoggerFactory.getLogger(AnyInvokerImpl.class);

    private final C1WebTarget.CodeInterval[] accepts;

    public AnyInvokerImpl(JerseyClient client,
                          UriBuilder targetUri,
                          ServiceFullName[] serviceFullNames,
                          C1WebTarget.CodeInterval... accepts) {
        super(client, targetUri, serviceFullNames);
        this.accepts = accepts;
    }

    @Override
    public LaterAnyInvoker later() {
        throw new NotImplementedException();
    }

    @Override
    public Response get() {
        return method(HttpMethod.GET);
    }

    @Override
    public <T> T get(Class<T> responseType) {
        return method(HttpMethod.GET, responseType);
    }

    @Override
    public <T> T get(GenericType<T> responseType) {
        return method(HttpMethod.GET, responseType);
    }

    @Override
    public Response put(Entity<?> entity) {
        return method(HttpMethod.PUT, entity);
    }

    @Override
    public <T> T put(Entity<?> entity, Class<T> responseType) {
        return method(HttpMethod.PUT, responseType);
    }

    @Override
    public <T> T put(Entity<?> entity, GenericType<T> responseType) {
        return method(HttpMethod.PUT, responseType);
    }

    @Override
    public Response post(Entity<?> entity) {
        return method(HttpMethod.POST, entity);
    }

    @Override
    public <T> T post(Entity<?> entity, Class<T> responseType) {
        return method(HttpMethod.POST, responseType);
    }

    @Override
    public <T> T post(Entity<?> entity, GenericType<T> responseType) {
        return method(HttpMethod.POST, responseType);
    }

    @Override
    public Response delete() {
        return method(HttpMethod.DELETE);
    }

    @Override
    public <T> T delete(Class<T> responseType) {
        return method(HttpMethod.DELETE, responseType);
    }

    @Override
    public <T> T delete(GenericType<T> responseType) {
        return method(HttpMethod.DELETE, responseType);
    }

    @Override
    public Response head() {
        return method(HttpMethod.HEAD);
    }

    @Override
    public Response options() {
        return method(HttpMethod.OPTIONS);
    }

    @Override
    public <T> T options(Class<T> responseType) {
        return method(HttpMethod.OPTIONS, responseType);
    }

    @Override
    public <T> T options(GenericType<T> responseType) {
        return method(HttpMethod.OPTIONS, responseType);
    }

    @Override
    public Response trace() {
        throw new NotImplementedException();
    }

    @Override
    public <T> T trace(Class<T> responseType) {
        throw new NotImplementedException();
    }

    @Override
    public <T> T trace(GenericType<T> responseType) {
        throw new NotImplementedException();
    }

    @Override
    public Response method(String name) {
        return method(name, (Entity) null);
    }

    @Override
    public <T> T method(String name, Class<T> responseType) {
        return method(name, null, responseType);
    }

    @Override
    public <T> T method(String name, GenericType<T> responseType) {
        return method(name, null, responseType);
    }

    private boolean isResponseCodeAcceptable(int code) {
        if (accepts != null) {
            for (C1WebTarget.CodeInterval accept : accepts) {
                if (accept.isInInterval(code)) {
                    return true;
                }
            }
            return false;
        } else {
            return true;
        }
    }

    @Override
    public Response method(String name, Entity<?> entity) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("method(" + name + ")");
        }
        List<ApplicationFullName> targetApplications = super.findTargetApplications(name);
        LoadBalancer loadBalancer = LoadBalancer.getInstance();
        for (ApplicationFullName targetApplication : targetApplications) {
            int port = loadBalancer.getPort(targetApplication);
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("method(" + name + "): targetApplication: " + targetApplication + ", port: " + port);
            }
            long startAt = -1;
            if (port > 0) {
                UriBuilder uriBuilder = targetUri
                        .clone()
                        .scheme("http")
                        .host("localhost")
                        .port(port);
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("method(" + name + "): URI: " + uriBuilder.build());
                }
                try {
                    JerseyInvocation.Builder request = client.target(uriBuilder.build()).request();
                    for (MediaType accept : super.acceptMediaTypes) {
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
                    //TODO:
                    LOGGER.warn("Cannot reach application " + targetApplication + " on port " + port, e);
                } finally {
                    if (startAt > 0) {
                        loadBalancer.updateStats(targetApplication, port, System.currentTimeMillis() - startAt);
                    }
                }
            }
        }
        return null;
    }

    @Override
    public <T> T method(String name, Entity<?> entity, Class<T> responseType) {
        Response response = method(name, entity);
        return response.readEntity(responseType);
    }

    @Override
    public <T> T method(String name, Entity<?> entity, GenericType<T> responseType) {
        Response response = method(name, entity);
        return response.readEntity(responseType);
    }

    @Override
    public AnyInvokerImpl accept(String... responseMediaTypes) {
        super.accept(responseMediaTypes);
        return this;
    }

    @Override
    public AnyInvokerImpl accept(MediaType... mediaTypes) {
        super.accept(mediaTypes);
        return this;
    }
}
