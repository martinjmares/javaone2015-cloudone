package cloudone.internal.dto;

import cloudone.ApplicationInfo;
import cloudone.RuntimeInfo;

import java.util.HashMap;
import java.util.Map;

/**
 * Information about ports of service runtime instance.
 *
 * @author Martin Mares (martin.mares at oracle.com)
 */
public class PortInfo {

    public int adminPort;
    public Map<String, Integer> applicationPorts = new HashMap<>();

    public PortInfo(int adminPort, Map<String, Integer> applicationPorts) {
        this.adminPort = adminPort;
        if (applicationPorts != null) {
            this.applicationPorts.putAll(applicationPorts);
        }
    }

    public PortInfo(RuntimeInfo runtimeInfo) {
        adminPort = runtimeInfo.getAdminPort();
        for (ApplicationInfo aInfo : runtimeInfo.getApplicationInfos()) {
            applicationPorts.put(aInfo.getName(), aInfo.getPort());
        }
    }

    public int getAdminPort() {
        return adminPort;
    }

    public void setAdminPort(int adminPort) {
        this.adminPort = adminPort;
    }

    public Map<String, Integer> getApplicationPorts() {
        return applicationPorts;
    }

    public void putApplicationPort(String name, int port) {
        applicationPorts.put(name, port);
    }
}
