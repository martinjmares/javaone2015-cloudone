package cloudone.client.internal;

import cloudone.ServiceFullName;
import cloudone.client.AllInvoker;
import cloudone.client.C1WebTarget;
import jersey.repackaged.com.google.common.base.Preconditions;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.JerseyClient;
import org.glassfish.jersey.client.JerseyInvocation;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;
import java.net.URI;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * @author Martin Mares (martin.mares at oracle.com)
 */
public class C1WebTargetImpl implements C1WebTarget {

    private final JerseyClient client;
    private final UriBuilder targetUri;
    private final ServiceFullName[] serviceFullNames;

    /**
     * Create new web target instance.
     *
     * @param uriBuilder builder for the target URI.
     * @param that       original target to copy the internal data from.
     */
    protected C1WebTargetImpl(UriBuilder uriBuilder, C1WebTargetImpl that) {
        this(uriBuilder, that.client, that.serviceFullNames);
    }

    /**
     * Create new web target instance.
     *
     * @param uriBuilder   builder for the target URI.
     */
    protected C1WebTargetImpl(UriBuilder uriBuilder, JerseyClient client, ServiceFullName... serviceFullNames) {
        this.targetUri = uriBuilder;
        this.client = client;
        this.serviceFullNames = serviceFullNames;
    }

    @Override
    public URI getUri() {
        checkNotClosed();
        try {
            return targetUri.build();
        } catch (IllegalArgumentException ex) {
            throw new IllegalStateException(ex.getMessage(), ex);
        }
    }

    private void checkNotClosed() {
        if (client.isClosed()) {
            throw new IllegalStateException("Already closed!");
        }
    }

    @Override
    public UriBuilder getUriBuilder() {
        checkNotClosed();
        return targetUri.clone();
    }

    @Override
    public C1WebTarget path(String path) throws NullPointerException {
        checkNotClosed();
        Preconditions.checkNotNull(path, "path is 'null'.");
        return new C1WebTargetImpl(getUriBuilder().path(path), this);
    }

    @Override
    public C1WebTarget matrixParam(String name, Object... values) throws NullPointerException {
        checkNotClosed();
        Preconditions.checkNotNull(name, "Matrix parameter name must not be 'null'.");

        if (values == null || values.length == 0 || (values.length == 1 && values[0] == null)) {
            return new C1WebTargetImpl(getUriBuilder().replaceMatrixParam(name, (Object[]) null), this);
        }

        checkForNullValues(name, values);
        return new C1WebTargetImpl(getUriBuilder().matrixParam(name, values), this);
    }

    @Override
    public C1WebTarget queryParam(String name, Object... values) throws NullPointerException {
        checkNotClosed();
        return new C1WebTargetImpl(C1WebTargetImpl.setQueryParam(getUriBuilder(), name, values), this);
    }

    @Override
    public AnyInvokerImpl anyOK() {
        return any(CodeInterval.SUCCESSFUL);
    }

    @Override
    public AnyInvokerImpl any(CodeInterval... accepts) {
        return new AnyInvokerImpl(client, getUriBuilder(), serviceFullNames, accepts);
    }

    @Override
    public AllInvoker all() {
        return new AllInvokerImpl(client, getUriBuilder(), serviceFullNames);
    }

    private static UriBuilder setQueryParam(UriBuilder uriBuilder, String name, Object[] values) {
        if (values == null || values.length == 0 || (values.length == 1 && values[0] == null)) {
            return uriBuilder.replaceQueryParam(name, (Object[]) null);
        }
        checkForNullValues(name, values);
        return uriBuilder.queryParam(name, values);
    }

    private static void checkForNullValues(String name, Object[] values) {
        Preconditions.checkNotNull(name, "name is 'null'.");

        List<Integer> indexes = new LinkedList<Integer>();
        for (int i = 0; i < values.length; i++) {
            if (values[i] == null) {
                indexes.add(i);
            }
        }
        final int failedIndexCount = indexes.size();
        if (failedIndexCount > 0) {
            final String valueTxt;
            final String indexTxt;
            if (failedIndexCount == 1) {
                valueTxt = "value";
                indexTxt = "index";
            } else {
                valueTxt = "values";
                indexTxt = "indexes";
            }

            throw new NullPointerException(
                    String.format("'null' %s detected for parameter '%s' on %s : %s",
                            valueTxt, name, indexTxt, indexes.toString()));
        }
    }

    @Override
    public JerseyInvocation.Builder request() {
        checkNotClosed();
        //TODO
        //return new JerseyInvocation.Builder(getUri(), config.snapshot());
        throw new NotImplementedException();
    }

    @Override
    public JerseyInvocation.Builder request(String... acceptedResponseTypes) {
        checkNotClosed();
        //TODO
        //JerseyInvocation.Builder b = new JerseyInvocation.Builder(getUri(), config.snapshot());
        //b.request().accept(acceptedResponseTypes);
        //return b;
        throw new NotImplementedException();
    }

    @Override
    public JerseyInvocation.Builder request(MediaType... acceptedResponseTypes) {
        checkNotClosed();
        //TODO
        //JerseyInvocation.Builder b = new JerseyInvocation.Builder(getUri(), config.snapshot());
        //b.request().accept(acceptedResponseTypes);
        //return b;
        throw new NotImplementedException();
    }

    @Override
    public C1WebTarget resolveTemplate(String name, Object value) throws NullPointerException {
        return resolveTemplate(name, value, true);
    }

    @Override
    public C1WebTarget resolveTemplate(String name, Object value, boolean encodeSlashInPath) throws NullPointerException {
        checkNotClosed();
        Preconditions.checkNotNull(name, "name is 'null'.");
        Preconditions.checkNotNull(value, "value is 'null'.");
        return new C1WebTargetImpl(getUriBuilder().resolveTemplate(name, value, encodeSlashInPath), this);
    }

    @Override
    public C1WebTarget resolveTemplateFromEncoded(String name, Object value)
            throws NullPointerException {
        checkNotClosed();
        Preconditions.checkNotNull(name, "name is 'null'.");
        Preconditions.checkNotNull(value, "value is 'null'.");

        return new C1WebTargetImpl(getUriBuilder().resolveTemplateFromEncoded(name, value), this);
    }

    @Override
    public C1WebTarget resolveTemplates(Map<String, Object> templateValues) throws NullPointerException {
        return resolveTemplates(templateValues, true);
    }

    @Override
    public C1WebTarget resolveTemplates(Map<String, Object> templateValues, boolean encodeSlashInPath)
            throws NullPointerException {
        checkNotClosed();
        checkTemplateValues(templateValues);

        if (templateValues.isEmpty()) {
            return this;
        } else {
            return new C1WebTargetImpl(getUriBuilder().resolveTemplates(templateValues, encodeSlashInPath), this);
        }
    }

    @Override
    public C1WebTarget resolveTemplatesFromEncoded(Map<String, Object> templateValues)
            throws NullPointerException {
        checkNotClosed();
        checkTemplateValues(templateValues);

        if (templateValues.isEmpty()) {
            return this;
        } else {
            return new C1WebTargetImpl(getUriBuilder().resolveTemplatesFromEncoded(templateValues), this);
        }
    }

    /**
     * Check template values for {@code null} values. Throws {@code NullPointerException} if the name-value map or any of the
     * names or encoded values in the map is {@code null}.
     *
     * @param templateValues map to check.
     * @throws NullPointerException if the name-value map or any of the names or encoded values in the map
     * is {@code null}.
     */
    private void checkTemplateValues(final Map<String, Object> templateValues) throws NullPointerException {
        Preconditions.checkNotNull(templateValues, "templateValues is 'null'.");

        for (final Map.Entry entry : templateValues.entrySet()) {
            Preconditions.checkNotNull(entry.getKey(), "name is 'null'.");
            Preconditions.checkNotNull(entry.getValue(), "value is 'null'.");
        }
    }

    @Override
    public C1WebTarget register(Class<?> providerClass) {
        checkNotClosed();
        client.register(providerClass);
        return this;
    }

    @Override
    public C1WebTarget register(Object provider) {
        checkNotClosed();
        client.register(provider);
        return this;
    }

    @Override
    public C1WebTarget register(Class<?> providerClass, int bindingPriority) {
        checkNotClosed();
        client.register(providerClass, bindingPriority);
        return this;
    }

    @Override
    public C1WebTarget register(Class<?> providerClass, Class<?>... contracts) {
        checkNotClosed();
        client.register(providerClass, contracts);
        return this;
    }

    @Override
    public C1WebTarget register(Class<?> providerClass, Map<Class<?>, Integer> contracts) {
        checkNotClosed();
        client.register(providerClass, contracts);
        return this;
    }

    @Override
    public C1WebTarget register(Object provider, int bindingPriority) {
        checkNotClosed();
        client.register(provider, bindingPriority);
        return this;
    }

    @Override
    public C1WebTarget register(Object provider, Class<?>... contracts) {
        checkNotClosed();
        client.register(provider, contracts);
        return this;
    }

    @Override
    public C1WebTarget register(Object provider, Map<Class<?>, Integer> contracts) {
        checkNotClosed();
        client.register(provider, contracts);
        return this;
    }

    @Override
    public C1WebTarget property(String name, Object value) {
        checkNotClosed();
        client.property(name, value);
        return this;
    }

    @Override
    public ClientConfig getConfiguration() {
        checkNotClosed();
        return client.getConfiguration();
    }

    @Override
    public String toString() {
        return "C1WebTarget { " + targetUri.toTemplate() + " }";
    }

}
