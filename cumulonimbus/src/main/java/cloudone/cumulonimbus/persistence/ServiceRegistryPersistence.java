package cloudone.cumulonimbus.persistence;

import cloudone.C1Services;
import cloudone.cumulonimbus.ServiceRegistryService;
import cloudone.cumulonimbus.model.Cluster;
import cloudone.cumulonimbus.model.RegisteredRuntime;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Store and load service registry data in periodical manner.
 *
 * @author Martin Mares (martin.mares at oracle.com)
 */
public class ServiceRegistryPersistence implements ServiceRegistryService.RegistrationListener {

    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(ServiceRegistryPersistence.class);
    private static final long INTERVAL = 3L; //seconds

    private final File storageFile;
    private final boolean manual;
    private final Gson gson;
    private final ServiceRegistryService serviceRegistryService;
    private volatile boolean scheduled = false;

    ServiceRegistryPersistence(final File storageFile,
                               final ServiceRegistryService serviceRegistryService,
                               final boolean manual) {
        this.storageFile = storageFile;
        this.manual = manual;
        this.serviceRegistryService = serviceRegistryService;
        this.gson = (new GsonBuilder())
                .setPrettyPrinting()
                .create();
    }

    public ServiceRegistryPersistence(final File storageFile,
                                      final ServiceRegistryService serviceRegistryService) {
        this(storageFile, serviceRegistryService, false);
    }

    private void scheduleNext() {
        C1Services
                .getInstance()
                .getScheduledExecutorService()
                .schedule(() -> {
                                try {
                                    store();
                                } catch (IOException e) {
                                    LOGGER.warn("Cannot store cluster data!", e);
                                } finally {
                                    scheduled = false;
                                }
                            }, INTERVAL, TimeUnit.SECONDS);
    }

    synchronized void store() throws IOException {
        LOGGER.info("Storing service registry.");
        Collection<Cluster> clusters = new ArrayList<>(serviceRegistryService.getClusters());
        if (clusters.isEmpty()) {
            if (storageFile.exists()) {
                storageFile.delete();
            }
        } else {
            try (FileWriter writer = new FileWriter(storageFile)) {
                gson.toJson(clusters, writer);
            }
        }
    }

    public synchronized Collection<Cluster> loadClusters() throws Exception {
        List<Cluster> loaded = Collections.emptyList();
        if (storageFile.exists()) {
            LOGGER.info("Application registry storage found - loading");
            try (Reader reader = new FileReader(storageFile)) {
                loaded = gson.fromJson(reader, (new TypeToken<ArrayList<Cluster>>() {}).getType());
            } catch (IOException e) {
                throw new Exception("Cannot read persisted registry!", e);
            }
            if (loaded == null) {
                loaded = Collections.emptyList();
            }
        }
        return loaded;
    }


    @Override
    public synchronized void register(RegisteredRuntime runtime, Cluster cluster) {
        if (!scheduled && !manual) {
            scheduleNext();
            scheduled = true;
        }
    }

    @Override
    public synchronized void unregister(RegisteredRuntime runtime, Cluster cluster) {
        register(runtime, cluster); //The same behavior
    }
}
