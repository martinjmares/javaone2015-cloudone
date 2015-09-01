package cloudone.internal.dto;

import cloudone.ServiceFullName;

import java.util.Set;
import java.util.TreeSet;

/**
 * Access ports of all instances of one application cluster.
 *
 * @author Martin Mares (martin.mares at oracle.com)
 */
public class ApplicationCluster {

    private final ServiceFullName serviceFullName;
    private final String applicationName;
    private final Set<Integer> ports = new TreeSet<>();

    public ApplicationCluster(ServiceFullName serviceFullName, String applicationName) {
        this.serviceFullName = serviceFullName;
        this.applicationName = applicationName;
    }

    public ServiceFullName getServiceFullName() {
        return serviceFullName;
    }

    public String getApplicationName() {
        return applicationName;
    }

    public Set<Integer> getPorts() {
        return ports;
    }

    public void addPort(int port) {
        ports.add(port);
    }

    public String toString() {
        StringBuilder result = new StringBuilder();
        result.append(serviceFullName).append("::").append(applicationName);
        result.append(" - ");
        boolean first = true;
        for (Integer port : ports) {
            if (first) {
                first = false;
            } else {
                result.append(',');
            }
            result.append(port);
        }
        return result.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ApplicationCluster)) return false;

        ApplicationCluster that = (ApplicationCluster) o;

        if (serviceFullName != null ? !serviceFullName.equals(that.serviceFullName) : that.serviceFullName != null)
            return false;
        if (applicationName != null ? !applicationName.equals(that.applicationName) : that.applicationName != null)
            return false;
        return !(ports != null ? !ports.equals(that.ports) : that.ports != null);

    }

    @Override
    public int hashCode() {
        int result = serviceFullName != null ? serviceFullName.hashCode() : 0;
        result = 31 * result + (applicationName != null ? applicationName.hashCode() : 0);
        result = 31 * result + (ports != null ? ports.hashCode() : 0);
        return result;
    }
}
