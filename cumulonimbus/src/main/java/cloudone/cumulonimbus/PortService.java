package cloudone.cumulonimbus;

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
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
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

    private static final long RESERVATION_PARIOD = 5 * 60 * 1000L; //5 minutes
    private static final String FILE_NAME = "appregistry.xml";

    public static final String KEY_PORT_RANGE = "port.range";
    public static final String KEY_PORT_RANGE_ADMIN = "port.range.admin";

    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(PortService.class);

    private static PortService INSTANCE;

    private final Gson gson;

    private final File registryFile;
    private final PortRange appRange;
    private final PortRange adminRange;

    private final Map<Integer, RegisteredRuntime> ports = new HashMap<>();
    private final Map<String, RegisteredRuntime> runtimeIds = new HashMap<>();
    private final Map<Integer, Long> reservations = new HashMap<>();

    private PortService(Properties properties, File dir) throws Exception {
        adminRange = new PortRange(properties.getProperty(KEY_PORT_RANGE_ADMIN, "4300-4399"));
        appRange = new PortRange(properties.getProperty(KEY_PORT_RANGE, "4400-4499"));
        registryFile = new File(dir, FILE_NAME);
        this.gson = new GsonBuilder()
                .setPrettyPrinting()
                .create();
        if (registryFile.exists()) {
            try (Reader reader = new FileReader(registryFile);) {
                List<RegisteredRuntime> rtms= gson.fromJson(reader, new TypeToken<ArrayList<RegisteredRuntime>>() {}.getType());
                for (RegisteredRuntime rtm : rtms) {
                    doRegistration(rtm);
                }
            }
        }
    }

    private synchronized void doRegistration(RegisteredRuntime runtime) {
        boolean modifReservations = false;
        if (reservations.remove(runtime.getAdminPort()) != null) {
            modifReservations = true;
        }
        ports.put(runtime.getAdminPort(), runtime);
        for (Integer port : runtime.getApplicationPorts().values()) {
            if (reservations.remove(port) != null) {
                modifReservations = true;
            }
            ports.put(port, runtime);
        }
        runtimeIds.put(runtime.getInstanceId(), runtime);
    }

    /**
     * Registers new runtime with all its ports.
     *
     * @param runtime Rutime for registration
     * @return application registretion id
     * @throws Exception in any case of the conflict
     */
    public synchronized String registerRuntime(RegisteredRuntime runtime) throws Exception {
        if (runtime == null) {
            throw new InvalidParameterException("RegisteredRuntime parameter cannot be null!");
        }
        RegisteredRuntime r = runtimeIds.get(runtime.getInstanceId());
        if (r != null) {
            if (r.equals(runtime)) {
                return r.getInstanceId();
            } else {
                throw new Exception("Instance ID conflict!");
            }
        }
        if (ports.containsKey(runtime.getAdminPort())) {
            throw new Exception("Admin port conflict!");
        }
        for (Integer port : runtime.getApplicationPorts().values()) {
            if (ports.containsKey(port)) {
                throw new Exception("Application port conflict!");
            }
        }
        //ALL is ok. Do registration
        doRegistration(runtime);
        store();
        return runtime.getInstanceId();
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

    public synchronized RegisteredRuntime getRegisteredRuntime(String instanceId) {
        return runtimeIds.get(instanceId);
    }

    public synchronized RegisteredRuntime getRegisteredRuntime(int port) {
        return ports.get(port);
    }

    public synchronized Collection<RegisteredRuntime> getRegisteredRuntimes() {
        return Collections.unmodifiableCollection(runtimeIds.values());
    }

    private synchronized void store() {
        if (runtimeIds.size() == 0) {
            if (registryFile.exists()) {
                registryFile.delete();
            }
        } else {
            try (FileWriter writer = new FileWriter(registryFile);) {
                gson.toJson(runtimeIds.values(), writer);
            } catch (IOException ioe) {
                LOGGER.warn("Can not store port reservations to " + registryFile.getPath() + "!", ioe);
            }
        }
    }

    static PortService init(Properties properties, File dir) throws Exception {
        LOGGER.info("Starting PortService");
        INSTANCE = new PortService(properties, dir);
        return INSTANCE;
    }

    public static PortService getInstance() {
        return INSTANCE;
    }

}
