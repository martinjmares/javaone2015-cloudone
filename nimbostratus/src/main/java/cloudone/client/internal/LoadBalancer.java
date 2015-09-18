package cloudone.client.internal;

import cloudone.internal.ApplicationFullName;
import cloudone.internal.dto.PortInfo;
import cloudone.internal.nimbostratus.CumulonimbusClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.ws.WebServiceException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Veri primitive demo load balancer for application ports based on reaction time.
 *
 * @author Martin Mares (martin.mares at oracle.com)
 */
// This implementation is allways updating from Cumulonimbus. It should be implemented in much more caching version.
public class LoadBalancer {

    private static class Stats {
        public final int port;
        public long durationSum;

        public Stats(int port, long durationSum) {
            this.durationSum = durationSum;
            this.port = port;
        }
    }

    private static class Ports {
        public volatile long updated = -1;
        public Map<Integer, Stats> ports = new HashMap<>();
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(LoadBalancer.class);

    private static final LoadBalancer INSTANCE = new LoadBalancer();
    private static long UPDATE_DURATION = 4000L;

    private final ConcurrentMap<ApplicationFullName, Ports> registry = new ConcurrentHashMap<>();

    private LoadBalancer() {
    }

    private void update(ApplicationFullName appName, Ports ports) {
        if (appName == null) {
            return;
        }
        try {
            HashMap<Integer, PortInfo> servicePorts = CumulonimbusClient.getInstance().getServicePorts(appName.getServiceName());
            final List<Integer> registeredPorts = new ArrayList<>(servicePorts.size());
            for (PortInfo portInfo : servicePorts.values()) {
                Integer port = portInfo.getApplicationPorts().get(appName.getApplicationName());
                if (port != null) {
                    registeredPorts.add(port);
                }
            }
            //Check for added ports
            final List<Integer> addPorts = new ArrayList<>();
            final Set<Integer> checkedPorts = new HashSet<>(ports.ports.size());
            for (Integer port : registeredPorts) {
                boolean add = true;
                for (Integer key : ports.ports.keySet()) {
                    if (key.equals(port)) {
                        add = false;
                        checkedPorts.add(port);
                        break;
                    }
                }
                if (add) {
                    addPorts.add(port);
                }
            }
            //Check for removed ports
            if (checkedPorts.size() < ports.ports.size()) {
                Iterator<Map.Entry<Integer, Stats>> iterator = ports.ports.entrySet().iterator();
                while (iterator.hasNext()) {
                    Map.Entry<Integer, Stats> entry = iterator.next();
                    if (!checkedPorts.contains(entry.getKey())) {
                        iterator.remove();
                    }
                }
            }
            //Add added ports
            if (!addPorts.isEmpty()) {
                long baseDuration = 0L;
                Stats lowest = getLowest(ports);
                if (lowest != null) {
                    baseDuration = lowest.durationSum;
                }
                for (Integer addPort : addPorts) {
                    ports.ports.put(addPort, new Stats(addPort, baseDuration));
                }
            }
            //Update timestamp
            ports.updated = System.currentTimeMillis();
        } catch (WebServiceException webExc) {
            LOGGER.warn("No more registered instance of service: " + appName.getServiceName() + "! Remove from cache");
            ports.ports.clear();
            ports.updated = System.currentTimeMillis();
        } catch (Exception e) {
            LOGGER.error("Cannot update ports for " + appName + "!", e);
        }
    }

    private Stats getLowest(Ports ports) {
        synchronized (ports) {
            Stats result = null;
            for (Stats stats : ports.ports.values()) {
                if (result == null || result.durationSum > stats.durationSum) {
                    result = stats;
                }
            }
            return result;
        }
    }

    public Ports getPorts(ApplicationFullName name) {
        Ports ports = registry.computeIfAbsent(name, nm -> new Ports());
        if ((ports.updated + UPDATE_DURATION) < System.currentTimeMillis()) {
            synchronized (ports) {
                if ((ports.updated + UPDATE_DURATION) < System.currentTimeMillis()) {
                    update(name, ports);
                }
            }
        }
        return ports;
    }

    /**
     * Finds best candidate for call. Returns negative value id not found.
     */
    public int getPort(ApplicationFullName name) {
        Ports ports = getPorts(name);
        if (ports != null) {
            synchronized (ports) {
                Stats result = getLowest(ports);
                if (result != null) {
                    result.durationSum++;
                    return result.port;
                }
            }
        }
        return -1;
    }

    public void updateStats(ApplicationFullName name, int port, long duration) {
        if (name == null || port < 0 || duration < 0) {
            return;
        }
        Ports ports = registry.get(name);
        if (ports != null) {
            synchronized (ports) {
                Stats stats = ports.ports.get(port);
                if (stats != null) {
                    try {
                        stats.durationSum = Math.addExact(stats.durationSum, duration);
                    } catch (ArithmeticException exc) {
                        LOGGER.warn("Too long duration. Must compact");
                        for (Stats stats1 : ports.ports.values()) {
                            stats1.durationSum -= stats.durationSum;
                            if (stats1.durationSum < 0) {
                                stats1.durationSum = 0;
                            }
                        }
                        stats.durationSum = duration;
                    }
                }
            }
        }
    }

    public static LoadBalancer getInstance() {
        return INSTANCE;
    }

}
