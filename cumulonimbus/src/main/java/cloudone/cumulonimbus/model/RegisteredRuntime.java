package cloudone.cumulonimbus.model;

import cloudone.ServiceFullName;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Entity for registered service runtime.
 *
 * @author Martin Mares (martin.mares at oracle.com)
 */
public class RegisteredRuntime {

    private ServiceFullName serviceName;
    private String instanceId;
    private int adminPort;
    private Map<String, Integer> applicationPorts = new HashMap<>();

    public RegisteredRuntime(ServiceFullName serviceName, int adminPort) {
        this.serviceName = serviceName;
        this.adminPort = adminPort;
        this.instanceId = UUID.randomUUID().toString();
    }

    public ServiceFullName getServiceName() {
        return serviceName;
    }

    public String getInstanceId() {
        return instanceId;
    }

    public int getAdminPort() {
        return adminPort;
    }

    public Map<String, Integer> getApplicationPorts() {
        return Collections.unmodifiableMap(applicationPorts);
    }

    public void registerApplication(String appName, int port) {
        applicationPorts.put(appName, port);
    }

    public int getApplicationPort(String appName) {
        return applicationPorts.get(appName);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RegisteredRuntime)) return false;
        RegisteredRuntime that = (RegisteredRuntime) o;
        if (adminPort != that.adminPort) return false;
        if (serviceName != null ? !serviceName.equals(that.serviceName) : that.serviceName != null) return false;
        if (instanceId != null ? !instanceId.equals(that.instanceId) : that.instanceId != null) return false;
        return !(applicationPorts != null ? !applicationPorts.equals(that.applicationPorts) : that.applicationPorts != null);

    }

    @Override
    public int hashCode() {
        int result = serviceName != null ? serviceName.hashCode() : 0;
        result = 31 * result + (instanceId != null ? instanceId.hashCode() : 0);
        result = 31 * result + adminPort;
        result = 31 * result + (applicationPorts != null ? applicationPorts.hashCode() : 0);
        return result;
    }
}
