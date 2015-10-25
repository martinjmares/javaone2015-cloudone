package cloudone.cumulonimbus.later;

import java.io.ByteArrayInputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.concurrent.atomic.AtomicInteger;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;

import cloudone.C1Services;
import cloudone.ServiceFullName;
import cloudone.cumulonimbus.ResourceRegistryService;
import cloudone.cumulonimbus.ServiceRegistryService;
import cloudone.cumulonimbus.model.Cluster;
import cloudone.cumulonimbus.model.HttpMethod;
import cloudone.cumulonimbus.model.RegisteredRuntime;
import cloudone.internal.ApplicationFullName;
import org.slf4j.LoggerFactory;

/**
 * Service for queued invocation.
 *
 * @author Martin Mares (martin.mares at oracle.com)
 */
public class LaterService {

    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(LaterService.class);
    private static final LaterService INSTANCE = new LaterService();

    private final long baseId = System.currentTimeMillis();
    private final AtomicInteger seqId = new AtomicInteger(0);

    private final Client client = ClientBuilder.newClient();

    private final Map<String, List<LaterItem>> callAllItems = new HashMap<>();


    public String addAllItem(String contentType,
                                String methodName,
                                String uri,
                                String services,
                                int retentionCount,
                                byte[] payload) {
        List<ServiceFullName> servicesList = new ArrayList<>();
        if (services != null) {
            StringTokenizer stok = new StringTokenizer(services, ";");
            while (stok.hasMoreTokens()) {
                servicesList.add(new ServiceFullName(stok.nextToken()));
            }
        }
        LaterItem item = new LaterItem(baseId + "-" + seqId.incrementAndGet(),
                                       contentType,
                                       methodName,
                                       URI.create(uri),
                                       servicesList.toArray(new ServiceFullName[servicesList.size()]),
                                       retentionCount,
                                       payload);
        LOGGER.info("add: " + item);
        synchronized (callAllItems) {
            String path = item.getPath();
            List<LaterItem> items = callAllItems.get(path);
            if (items == null) {
                items = new ArrayList<LaterItem>();
                callAllItems.put(path, items);
            }
            items.add(item);
            List<LaterItem> toRemove = new ArrayList<>();
            for (int i = 0; i < items.size(); i++) {
                int rc = items.get(i).getRetentionCount();
                if (rc >= 0 && (items.size() - i - 1) >= rc) {
                    toRemove.add(items.get(i));
                }
            }
            items.removeAll(toRemove);
        }
        C1Services.getInstance().getExecutorService().submit(() -> processAllItem(item));
        return item.getId();
    }

    private void processAllItem(LaterItem item) {
        HttpMethod httpMethod = HttpMethod.valueOf(item.getMethod());
        Collection<ApplicationFullName> apps;
        synchronized (callAllItems) {
            apps = ResourceRegistryService.getInstance()
                                .getApplicationsForResource(item.getPath(), httpMethod);
        }
        apps = item.filterApps(apps);
        for (ApplicationFullName app : apps) {
            call(app, item);
        }
    }

    private void call(ApplicationFullName app, LaterItem item) {
        LOGGER.info("call(" + app + ", " + item.getId() + ")");
        Cluster cluster = ServiceRegistryService.getInstance().getCluster(app.getServiceName());
        UriBuilder uriBuilder = UriBuilder.fromUri(item.getUri());
        for (RegisteredRuntime runtime : cluster.getRuntimes()) {
            int port = runtime.getApplicationPort(app.getApplicationName());
            Response response = client
                    .target(uriBuilder.host("localhost").port(port).scheme("http"))
                    .request()
                    .method(item.getMethod(),
                            Entity.entity(new ByteArrayInputStream(item.getPayload()),
                                          item.getContentType()));
            if (response.getStatus() == 200) {
                //TODO: process entity
                item.deliveredTo(app);
                break;
            }
        }
    }

    public void processQueuesForNewApp(ApplicationFullName appFullName) {
        Collection<LaterItem> toProcess = new ArrayList<>();
        //Process all
        synchronized (callAllItems) {
            for (List<LaterItem> laterItems : callAllItems.values()) {
                for (LaterItem item : laterItems) {
                    HttpMethod httpMethod = HttpMethod.valueOf(item.getMethod());
                    Collection<ApplicationFullName> apps;
                    synchronized (callAllItems) {
                        apps = ResourceRegistryService.getInstance()
                                .getApplicationsForResource(item.getPath(), httpMethod);
                    }
                    apps = item.filterApps(apps);
                    for (ApplicationFullName app : apps) {
                        if (app.equals(appFullName)) {
                            toProcess.add(item);
                        }
                    }
                }
            }
        }
        //Execute
        for (LaterItem item : toProcess) {
            call(appFullName, item);
        }
    }

    public static LaterService getInstance() {
        return INSTANCE;
    }
}
