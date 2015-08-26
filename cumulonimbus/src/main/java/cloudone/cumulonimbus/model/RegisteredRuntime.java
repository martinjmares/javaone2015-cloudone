package cloudone.cumulonimbus.model;

import cloudone.ServiceFullName;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Entity for registered service runtime.
 *
 * @author Martin Mares (martin.mares at oracle.com)
 */
public class RegisteredRuntime {

    private ServiceFullName serviceName;
    private String instanceSecCode;
    private int instanceId;
    private int adminPort;
    private transient long lastTouch = -1;
    private Map<String, Integer> applicationPorts = new HashMap<>();

    public RegisteredRuntime(ServiceFullName serviceName,
                             String instanceSecCode,
                             int instanceId,
                             int adminPort,
                             Map<String, Integer> applicationPorts) {
        this.serviceName = serviceName;
        this.instanceSecCode = instanceSecCode;
        this.instanceId = instanceId;
        this.adminPort = adminPort;
        this.applicationPorts.putAll(applicationPorts);
    }

    public ServiceFullName getServiceName() {
        return serviceName;
    }

    public String getInstanceSecCode() {
        return instanceSecCode;
    }

    public int getInstanceId() {
        return instanceId;
    }

    public long getLastTouch() {
        return lastTouch;
    }

    public void touch() {
        lastTouch = System.currentTimeMillis();
    }

    public int getAdminPort() {
        return adminPort;
    }

    public Map<String, Integer> getApplicationPorts() {
        return Collections.unmodifiableMap(applicationPorts);
    }

    public int getApplicationPort(String appName) {
        Integer result = applicationPorts.get(appName);
        if (result == null) {
            return -1;
        } else {
            return result;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RegisteredRuntime)) return false;
        RegisteredRuntime that = (RegisteredRuntime) o;
        if (adminPort != that.adminPort) return false;
        if (serviceName != null ? !serviceName.equals(that.serviceName) : that.serviceName != null) return false;
        if (instanceSecCode != null ? !instanceSecCode.equals(that.instanceSecCode) : that.instanceSecCode != null) return false;
        return !(applicationPorts != null ? !applicationPorts.equals(that.applicationPorts) : that.applicationPorts != null);

    }

    @Override
    public int hashCode() {
        int result = serviceName != null ? serviceName.hashCode() : 0;
        result = 31 * result + (instanceSecCode != null ? instanceSecCode.hashCode() : 0);
        result = 31 * result + adminPort;
        result = 31 * result + (applicationPorts != null ? applicationPorts.hashCode() : 0);
        return result;
    }

    public String toRuntimeName() {
        return serviceName + "::" + instanceId;
    }

    @Override
    public String toString() {
        return "RegisteredRuntime{" +
                "serviceName=" + serviceName +
                ", instanceId=" + instanceId +
                ", adminPort=" + adminPort +
                ", lastTouch=" + lastTouch +
                ", applicationPorts=" + applicationPorts +
                '}';
    }
}
