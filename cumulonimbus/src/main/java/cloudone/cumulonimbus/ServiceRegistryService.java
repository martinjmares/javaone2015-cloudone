package cloudone.cumulonimbus;

import cloudone.ServiceFullName;
import cloudone.cumulonimbus.model.Cluster;
import cloudone.cumulonimbus.model.RegisteredRuntime;
import cloudone.cumulonimbus.persistence.ServiceRegistryPersistence;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Catalogue of registered services.
 *
 * @author Martin Mares (martin.mares at oracle.com)
 */
public class ServiceRegistryService {

    public interface RegistrationListener {

        /** Service runtime instance is about to register.
         *
         * @param runtime which is about to register.
         * @param cluster with other registered instances.
         * @throws Exception in case that it is not possible to register such instance.
         */
        public void register(RegisteredRuntime runtime, Cluster cluster) throws Exception;

        /** This service runtime instance was unregistered.
         *
         * @param runtime which is unregistered.
         * @param cluster from which this instance was unregistered.
         */
        public void unregister(RegisteredRuntime runtime, Cluster cluster);
    }

    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(ServiceRegistryService.class);
    private static final String FILE_NAME = "appregistry.json";
    private static ServiceRegistryService INSTANCE;

    private final ConcurrentMap<ServiceFullName, Cluster> registry = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, RegisteredRuntime> secRegistry = new ConcurrentHashMap<>();
    private final List<RegistrationListener> listeners = Collections.synchronizedList(new ArrayList<RegistrationListener>());


    ServiceRegistryService() {
    }

    private void fireRegister(final RegisteredRuntime runtime, final Cluster cluster) throws Exception {
        final List<RegistrationListener> passListeners = new ArrayList<>(listeners.size());
        try {
            for (RegistrationListener listener : listeners) {
                listener.register(runtime, cluster);
                passListeners.add(listener);
            }
        } catch (Exception exc) {
            fireUnRegister(runtime, cluster, passListeners);
            throw exc;
        }
    }

    private void fireUnRegister(final RegisteredRuntime runtime,
                                final Cluster cluster,
                                final List<RegistrationListener> listeners) {
        for (RegistrationListener listener : listeners) {
            try {
                listener.unregister(runtime, cluster);
            } catch (RuntimeException re) {
                LOGGER.warn("Exception during unregistration of " + runtime.toRuntimeName() + " from " + listener, re);
            }
        }
    }

    private void fireUnRegister(final RegisteredRuntime runtime, final Cluster cluster) {
        fireUnRegister(runtime, cluster, this.listeners);
    }

    public void addRegistrationListener(RegistrationListener listener) {
        listeners.add(listener);
    }

    public RegisteredRuntime register(ServiceFullName fullName,
                                      int adminPort,
                                      Map<String, Integer> applicationPorts) throws Exception {
        final Cluster cluster = registry.computeIfAbsent(fullName, fn -> new Cluster(fn));
        final RegisteredRuntime result = cluster.register(adminPort, applicationPorts);
        try {
            fireRegister(result, cluster);
        } catch (Exception e) {
            //Remove from registry
            cluster.unRegister(result);
            if (cluster.getRuntimes().isEmpty()) {
                registry.remove(result.getServiceName());
            }
            LOGGER.warn("Can not accept service registration!", e);
            throw e;
        }
        secRegistry.put(result.getInstanceSecCode(), result);
        LOGGER.info("REGISTERED: " + result.toRuntimeName());
        return result;
    }

    public boolean unregister(RegisteredRuntime runtime) {
        if (runtime == null) {
            return false;
        }
        Cluster cluster = registry.get(runtime.getServiceName());
        if (cluster != null) {
            if (cluster.unRegister(runtime)) {
                if (cluster.getRuntimes().size() == 0) {
                    registry.remove(cluster.getFullName());
                }
                fireUnRegister(runtime, cluster);
                LOGGER.info("UNREGISTERED: " + runtime.toRuntimeName());
                return true;
            }
        }
        return false;
    }

    private void registerClusterInternal(Cluster cluster) throws Exception {
        if (registry.putIfAbsent(cluster.getFullName(), cluster) != null) {
            throw new Exception("Cluster " + cluster.getFullName() + " allready registered");
        }
        for (RegisteredRuntime runtime : cluster.getRuntimes()) {
            fireRegister(runtime, cluster);
        }
    }

    public Collection<Cluster> getClusters() {
        return Collections.unmodifiableCollection(registry.values());
    }

    public Cluster getCluster(ServiceFullName name) {
        if (name == null) {
            return null;
        }
        return registry.get(name);
    }

    public RegisteredRuntime getRuntime(String secCode) {
        return secRegistry.get(secCode);
    }

    public static ServiceRegistryService getInstance() {
        return INSTANCE;
    }

    static ServiceRegistryService init(final File dir, final RegistrationListener... listeners) throws Exception {
        final File storeFile = new File(dir, FILE_NAME);
        INSTANCE = new ServiceRegistryService();
        if (listeners != null) {
            for (RegistrationListener listener : listeners) {
                INSTANCE.addRegistrationListener(listener);
            }
        }
        //Load from file
        ServiceRegistryPersistence serviceRegistryPersistence = new ServiceRegistryPersistence(storeFile, INSTANCE);
        for (Cluster cluster : serviceRegistryPersistence.loadClusters()) {
            INSTANCE.registerClusterInternal(cluster);
        }
        INSTANCE.addRegistrationListener(serviceRegistryPersistence);
        return INSTANCE;
    }

}
