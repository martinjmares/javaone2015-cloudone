package cloudone.client.internal;

import java.util.List;

import javax.ws.rs.HttpMethod;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;

import cloudone.ServiceFullName;
import cloudone.client.AllInvoker;
import cloudone.client.LaterAllInvoker;
import cloudone.internal.ApplicationFullName;
import org.glassfish.jersey.client.JerseyClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

/**
 * @author Martin Mares (martin.mares at oracle.com)
 */
public class AllInvokerImpl extends InvokerBase implements AllInvoker {

    private static final Logger LOGGER = LoggerFactory.getLogger(AllInvokerImpl.class);

    public AllInvokerImpl(JerseyClient client,
                          UriBuilder targetUri,
                          ServiceFullName[] serviceFullNames) {
        super(client, targetUri, serviceFullNames);
    }

    @Override
    protected boolean isResponseCodeAcceptable(int code) {
        return true;
    }

    @Override
    public LaterAllInvoker later() {
        return new LaterAllInvokerImpl(client, targetUri, serviceFullNames);
    }

    @Override
    public MultiResponseImpl method(String name, Entity<?> entity) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("method(" + name + ")");
        }
        List<ApplicationFullName> targetApplications = super.findTargetApplications(name);
        MultiResponseImpl response = new MultiResponseImpl();
        for (ApplicationFullName targetApplication : targetApplications) {
            response.add(targetApplication, () -> method(targetApplication, name, entity));
        }
        return response;
    }

    @Override
    public MultiResponseImpl method(String name) {
        return method(name, null);
    }

    @Override
    public AllInvokerImpl accept(String... responseMediaTypes) {
        super.accept(responseMediaTypes);
        return this;
    }

    @Override
    public AllInvokerImpl accept(MediaType... mediaTypes) {
        super.accept(mediaTypes);
        return this;
    }

    @Override
    public MultiResponseImpl get() {
        return method(HttpMethod.GET);
    }

    @Override
    public MultiResponseImpl put(Entity<?> entity) {
        return method(HttpMethod.PUT, entity);
    }

    @Override
    public MultiResponseImpl post(Entity<?> entity) {
        return method(HttpMethod.POST, entity);
    }

    @Override
    public MultiResponseImpl delete() {
        return method(HttpMethod.DELETE);
    }

    @Override
    public MultiResponseImpl head() {
        return method(HttpMethod.HEAD);
    }

    @Override
    public MultiResponseImpl options() {
        return method(HttpMethod.OPTIONS);
    }

    @Override
    public MultiResponseImpl trace() {
        throw new NotImplementedException();
    }
}
