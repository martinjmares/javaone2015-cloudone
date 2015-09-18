package cloudone.client.internal;

import cloudone.ServiceFullName;
import cloudone.client.C1Client;
import cloudone.client.C1WebTarget;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.JerseyClient;
import org.glassfish.jersey.client.JerseyInvocation;
import org.glassfish.jersey.client.JerseyWebTarget;
import org.glassfish.jersey.uri.internal.JerseyUriBuilder;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.ws.rs.core.Link;
import javax.ws.rs.core.UriBuilder;
import java.net.URI;
import java.util.Map;

/**
 * @author Martin Mares (martin.mares at oracle.com)
 */
public class C1ClientImpl extends JerseyClient implements C1Client {

    private final JerseyClient client;

    public C1ClientImpl(JerseyClient client) {
        this.client = client;
    }

    @Override
    public void close() {
        client.close();
    }

    @Override
    public JerseyWebTarget target(String uri) {
        return client.target(uri);
    }

    @Override
    public JerseyWebTarget target(URI uri) {
        return client.target(uri);
    }

    @Override
    public JerseyWebTarget target(UriBuilder uriBuilder) {
        return client.target(uriBuilder);
    }

    @Override
    public JerseyWebTarget target(Link link) {
        return client.target(link);
    }

    @Override
    public JerseyInvocation.Builder invocation(Link link) {
        return client.invocation(link);
    }

    @Override
    public SSLContext getSslContext() {
        return client.getSslContext();
    }

    @Override
    public HostnameVerifier getHostnameVerifier() {
        return client.getHostnameVerifier();
    }

    @Override
    public ClientConfig getConfiguration() {
        return client.getConfiguration();
    }

    @Override
    public C1ClientImpl property(String name, Object value) {
        return new C1ClientImpl(client.property(name, value));
    }

    @Override
    public C1ClientImpl register(Class<?> componentClass) {
        return new C1ClientImpl(client.register(componentClass));
    }

    @Override
    public C1ClientImpl register(Class<?> componentClass, int priority) {
        return new C1ClientImpl(client.register(componentClass, priority));
    }

    @Override
    public C1ClientImpl register(Class<?> componentClass, Class<?>... contracts) {
        return new C1ClientImpl(client.register(componentClass, contracts));
    }

    @Override
    public C1ClientImpl register(Class<?> componentClass, Map<Class<?>, Integer> contracts) {
        return new C1ClientImpl(client.register(componentClass, contracts));
    }

    @Override
    public C1ClientImpl register(Object component) {
        return new C1ClientImpl(client.register(component));
    }

    @Override
    public C1ClientImpl register(Object component, int priority) {
        return new C1ClientImpl(client.register(component, priority));
    }

    @Override
    public C1ClientImpl register(Object component, Class<?>... contracts) {
        return new C1ClientImpl(client.register(component, contracts));
    }

    @Override
    public C1ClientImpl register(Object component, Map<Class<?>, Integer> contracts) {
        return new C1ClientImpl(client.register(component, contracts));
    }

    @Override
    public C1WebTarget target() {
        return new C1WebTargetImpl(new JerseyUriBuilder(), client, null);
    }

    @Override
    public C1WebTarget target(ServiceFullName... serviceNames) {
        return new C1WebTargetImpl(new JerseyUriBuilder(), client, serviceNames);
    }
}
