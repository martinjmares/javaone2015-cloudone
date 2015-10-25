package cloudone.client.internal;

import java.util.concurrent.TimeUnit;

import javax.ws.rs.HttpMethod;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.UriBuilder;

import cloudone.ServiceFullName;
import cloudone.client.LaterAllInvoker;
import cloudone.client.LaterInvoker;
import cloudone.internal.nimbostratus.CumulonimbusClient;
import org.glassfish.jersey.client.JerseyClient;
import org.glassfish.jersey.client.JerseyWebTarget;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Martin Mares (martin.mares at oracle.com)
 */
public class LaterAllInvokerImpl implements LaterAllInvoker {

    private static final Logger LOGGER = LoggerFactory.getLogger(LaterAllInvokerImpl.class);
    
    private final JerseyClient client;
    private final UriBuilder targetUri;
    private final ServiceFullName[] serviceFullNames;
    private final String responsePath;
    private final ServiceFullName responseService;
    private final int retentionCount;
    private final long retetionPeriod;
    private final TimeUnit retentionTimeUnit;

    public LaterAllInvokerImpl(JerseyClient client,
                               UriBuilder targetUri,
                               ServiceFullName[] serviceFullNames,
                               String responsePath,
                               ServiceFullName responseService,
                               int retentionCount,
                               long retentionPeriod,
                               TimeUnit retentionTimeUnit) {
        this.client = client;
        this.targetUri = targetUri;
        this.serviceFullNames = serviceFullNames;
        this.responsePath = responsePath;
        this.responseService = responseService;
        this.retentionCount = retentionCount;
        this.retetionPeriod = retentionPeriod;
        if (retentionPeriod >= 0 && retentionTimeUnit == null) {
            retentionTimeUnit = TimeUnit.MILLISECONDS;
        }
        this.retentionTimeUnit = retentionTimeUnit;
    }

    LaterAllInvokerImpl(JerseyClient client,
                          UriBuilder targetUri,
                          ServiceFullName[] serviceFullNames) {
        this(client, targetUri, serviceFullNames,null, null, -1, -1, null);
    }
    
    @Override
    public LaterInvoker responseTo(String path) {
        return new LaterAllInvokerImpl(client,
                                       targetUri,
                                       serviceFullNames,
                                       path, //responsePath
                                       responseService,
                                       retentionCount,
                                       retetionPeriod,
                                       retentionTimeUnit);
    }

    @Override
    public LaterInvoker responseTo(ServiceFullName service, String path) {
        return new LaterAllInvokerImpl(client,
                                       targetUri,
                                       serviceFullNames,
                                       path, //responsePath
                                       service, //responseService,
                                       retentionCount,
                                       retetionPeriod,
                                       retentionTimeUnit);
    }

    @Override
    public LaterInvoker retentionCount(int count) {
        return new LaterAllInvokerImpl(client,
                                       targetUri,
                                       serviceFullNames,
                                       responsePath,
                                       responseService,
                                       count, //retentionCount,
                                       retetionPeriod,
                                       retentionTimeUnit);
    }

    @Override
    public LaterInvoker retentionTime(long period, TimeUnit timeUnit) {
        return new LaterAllInvokerImpl(client,
                                       targetUri,
                                       serviceFullNames,
                                       responsePath,
                                       responseService,
                                       retentionCount,
                                       period, //retetionPeriod,
                                       timeUnit); //retentionTimeUnit);
    }

    @Override
    public String method(String name, Entity<?> entity) {
        JerseyWebTarget target = client.target(CumulonimbusClient.getInstance().getTargetUrl())
                                        .path("/later/all");
        return doMethod(target, name, entity);
    }

    @Override
    public String method(String name) {
        return method(name, null);
    }

    @Override
    public String get() {
        return method(HttpMethod.GET);
    }

    @Override
    public String put(Entity<?> entity) {
        return method(HttpMethod.PUT, entity);
    }

    @Override
    public String post(Entity<?> entity) {
        return method(HttpMethod.POST, entity);
    }

    @Override
    public String delete() {
        return method(HttpMethod.DELETE);
    }

    @Override
    public String head() {
        return method(HttpMethod.HEAD);
    }

    @Override
    public String options() {
        return method(HttpMethod.OPTIONS);
    }

    private String targetServices2String() {
        StringBuilder result = new StringBuilder();
        if (serviceFullNames != null) {
            for (ServiceFullName serviceFullName : serviceFullNames) {
                if (result.length() > 0) {
                    result.append(";");
                }
                result.append(serviceFullName.toString());
            }
        }
        return result.toString();
    }

    private String doMethod(WebTarget t, String name, Entity<?> entity) {
        Invocation.Builder request = t.queryParam("method", name)
                .queryParam("uri", targetUri.build().toString())
                .queryParam("services", targetServices2String())
                .queryParam("retention-count", retentionCount)
                .request();
        if (entity == null) {
            return request.build(name).invoke(String.class);
        } else {
            return request.build(name, entity).invoke(String.class);
        }
    }
}
