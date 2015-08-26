package cloudone.cumulonimbus.model;

import cloudone.ServiceFullName;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Represents cluster - several instancef of the same service.
 *
 * @author Martin Mares (martin.mares at oracle.com)
 */
public class Cluster {

    private ServiceFullName fullName;
    private List<RegisteredRuntime> runtimes = new ArrayList<>(1);
    private volatile transient int counter = -1;

    public Cluster(ServiceFullName fullName) {
        this.fullName = fullName;
    }

    private synchronized int getNextId() {
        if (counter < 0) {
            for (RegisteredRuntime runtime : runtimes) {
                if (runtime.getInstanceId() > counter) {
                    counter = runtime.getInstanceId();
                }
            }
        }
        return ++counter;
    }

    public synchronized RegisteredRuntime register(int adminPort,
                                                   Map<String, Integer> applicationPorts) {
        RegisteredRuntime result = new RegisteredRuntime(fullName,
                                                        UUID.randomUUID().toString(),
                                                        getNextId(),
                                                        adminPort,
                                                        applicationPorts);
        result.touch();
        runtimes.add(result);
        return result;
    }

    public synchronized boolean unRegister(RegisteredRuntime runtime) {
        if (runtime == null) {
            return false;
        }
        for (int i = 0; i < runtimes.size(); i++) {
            RegisteredRuntime rr = runtimes.get(i);
            if (runtime.getInstanceSecCode().equals(rr.getInstanceSecCode())
                    && runtime.getInstanceId() == rr.getInstanceId()) {
                runtimes.remove(i);
                return true;
            }
        }
        return false;
    }

    public synchronized RegisteredRuntime getRuntime(int instamceId) {
        for (RegisteredRuntime runtime : runtimes) {
            if (instamceId == runtime.getInstanceId()) {
                return runtime;
            }
        }
        return null;
    }

    public ServiceFullName getFullName() {
        return fullName;
    }

    public List<RegisteredRuntime> getRuntimes() {
        return Collections.unmodifiableList(runtimes);
    }

    public String toString() {
        return "Cluster{" + fullName + " - " + runtimes.size() + "}";
    }
}
