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

    private final File storageFile;
    private final long interval;
    private final Gson gson;
    private final ServiceRegistryService serviceRegistryService;
    private volatile boolean dirty = false;
    private volatile long nextMustStore = -1;

    ServiceRegistryPersistence(final File storageFile,
                               final long interval,
                               final ServiceRegistryService serviceRegistryService,
                               final boolean manual) {
        this.storageFile = storageFile;
        this.serviceRegistryService = serviceRegistryService;
        this.gson = (new GsonBuilder())
                .setPrettyPrinting()
                .create();
        if (interval <= 0) {
            this.interval = 1;
        } else {
            this.interval = interval;
        }
        if (!manual) {
            scheduleNext();
        }
    }

    public ServiceRegistryPersistence(final File storageFile,
                                      final ServiceRegistryService serviceRegistryService,
                                      final long interval) {
        this(storageFile, interval, serviceRegistryService, false);
    }

    private void scheduleNext() {
        C1Services.getInstance().getScheduledExecutorService()
                .schedule(new Runnable() {
                    @Override
                    public void run() {
                        storeAndScheduleNext();
                    }
                }, interval, TimeUnit.MILLISECONDS);
    }

    void store() throws IOException {
        boolean act = false;
        synchronized (this) {
            if (dirty || nextMustStore > System.currentTimeMillis()) {
                act = true;
                dirty = false;
                nextMustStore = -1;
            }
        }
        if (act) {
            LOGGER.info("Storing service registry.");
            try (FileWriter writer = new FileWriter(storageFile)) {
                gson.toJson(serviceRegistryService.getClusters(), writer);
            }
        }
    }

    private synchronized void storeAndScheduleNext() {
        try {
            store();
        } catch (Throwable thr) {
            LOGGER.warn("Can not store service registry!", thr);
        } finally {
            scheduleNext();
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
    public synchronized void register(RegisteredRuntime runtime) throws Exception {
        dirty = true;
    }

    @Override
    public synchronized void unregister(RegisteredRuntime runtime) {
        if (!dirty && nextMustStore < 0) {
            nextMustStore = System.currentTimeMillis() + (60 * 1000L); //After 1 minute
        }
    }
}
