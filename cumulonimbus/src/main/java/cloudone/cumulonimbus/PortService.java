package cloudone.cumulonimbus;

import cloudone.cumulonimbus.model.RegisteredRuntime;
import org.slf4j.LoggerFactory;

import java.security.InvalidParameterException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Manages for running services.
 *
 * @author Martin Mares (martin.mares at oracle.com)
 */
public class PortService {

    private static class PortRange {
        private final int from;
        private final int to;
        private int index = -1;

        public PortRange(String range) throws Exception {
            if (range == null) {
                throw new Exception("Invalid port range " + range);
            }
            int ind = range.indexOf('-');
            String sFrom = range;
            String sTo = "";
            if (ind >= 0) {
                sFrom = range.substring(0, ind).trim();
                sTo = range.substring(ind + 1).trim();
                if (sTo.length() == 0) {
                    sTo = String.valueOf(Integer.MAX_VALUE);
                }
            }
            if (sFrom.length() == 0) {
                sFrom = sTo;
            } else if (sTo.length() == 0) {
                sTo = sFrom;
            }
            if (sFrom.length() == 0 && sTo.length() == 0) {
                throw new Exception("Invalid port range " + range);
            }
            from = Integer.parseInt(sFrom);
            to = Integer.parseInt(sTo);
        }

        public PortRange(int from, int to) {
            this.from = from;
            this.to = to;
        }

        public synchronized int getNext() {
            if (index < 0) {
                index = from;
                return index;
            }
            index++;
            if (index > to) {
                index = from;
            }
            return index;
        }
    }

    public class RegistrationListener implements ServiceRegistryService.RegistrationListener {

        private RegistrationListener() {}

        @Override
        public void register(RegisteredRuntime runtime) throws Exception {
            registerRuntime(runtime);
        }

        @Override
        public void unregister(RegisteredRuntime runtime) {
            unregisterRuntime(runtime);
        }
    }

    private static final long RESERVATION_PARIOD = 5 * 60 * 1000L; //5 minutes

    public static final String KEY_PORT_RANGE = "port.range";
    public static final String KEY_PORT_RANGE_ADMIN = "port.range.admin";

    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(PortService.class);

    private static PortService INSTANCE;

    private final PortRange appRange;
    private final PortRange adminRange;

    private final Map<Integer, RegisteredRuntime> ports = new HashMap<>();
    private final Map<Integer, Long> reservations = new HashMap<>();

    private PortService(Properties properties) throws Exception {
        adminRange = new PortRange(properties.getProperty(KEY_PORT_RANGE_ADMIN, "4300-4399"));
        appRange = new PortRange(properties.getProperty(KEY_PORT_RANGE, "4400-4499"));
    }

    /**
     * Registers new runtime with all its ports.
     *
     * @param runtime Rutime for registration
     * @return application registretion id
     * @throws Exception in any case of the conflict
     */
    private synchronized void registerRuntime(RegisteredRuntime runtime) throws Exception {
        if (runtime == null) {
            throw new InvalidParameterException("RegisteredRuntime parameter cannot be null!");
        }
        RegisteredRuntime rr2 = ports.get(runtime.getAdminPort());
        if (rr2 != null) {
            if (rr2.equals(runtime)) {
                return; //This is the same => OK
            } else {
                throw new Exception("Admin port conflict!");
            }
        }
        for (Integer port : runtime.getApplicationPorts().values()) {
            if (ports.containsKey(port)) {
                throw new Exception("Application port conflict!");
            }
        }
        //ALL is ok. Do registration
        ports.put(runtime.getAdminPort(), runtime);
        reservations.remove(runtime.getAdminPort());
        for (Integer port : runtime.getApplicationPorts().values()) {
            ports.put(port, runtime);
            reservations.remove(port);
        }
    }

    private synchronized void unregisterRuntime(RegisteredRuntime runtime) {
        ports.remove(runtime.getAdminPort());
        for (Integer port : runtime.getApplicationPorts().values()) {
            ports.remove(port);
        }
    }

    private synchronized int reserve(PortRange range) throws Exception {
        int firstPort = range.getNext();
        int result = firstPort;
        while (true) {
            Long validTo = reservations.get(result);
            if (validTo != null && validTo < System.currentTimeMillis()) {
                reservations.remove(result);
                validTo = null;
            }
            if (validTo == null && !ports.containsKey(result)) {
                reservations.put(result, System.currentTimeMillis() + RESERVATION_PARIOD);
                return result;
            } else {
                result = range.getNext();
                if (result == firstPort) {
                    throw new Exception("No avialble port!");
                }
            }
        }
    }

    public synchronized int reserveAdminPort() throws Exception {
        return reserve(adminRange);
    }

    public synchronized int reserveApplicationPort() throws Exception {
        return reserve(appRange);
    }

    public synchronized RegisteredRuntime getRegisteredRuntime(int port) {
        return ports.get(port);
    }

    RegistrationListener getNewListener() {
        return new RegistrationListener();
    }

    static PortService init(Properties properties) throws Exception {
        LOGGER.info("Starting PortService");
        INSTANCE = new PortService(properties);
        return INSTANCE;
    }

    public static PortService getInstance() {
        return INSTANCE;
    }

}
