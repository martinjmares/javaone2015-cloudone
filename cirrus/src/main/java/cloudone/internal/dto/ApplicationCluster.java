package cloudone.internal.dto;

import cloudone.ServiceFullName;
import cloudone.internal.ApplicationFullName;

import java.util.Set;
import java.util.TreeSet;

/**
 * Access ports of all instances of one application cluster.
 *
 * @author Martin Mares (martin.mares at oracle.com)
 */
public class ApplicationCluster {

    private final ApplicationFullName fullName;
    private final Set<Integer> ports = new TreeSet<>();

    public ApplicationCluster(ServiceFullName serviceFullName, String applicationName) {
        this(new ApplicationFullName(serviceFullName, applicationName));
    }

    public ApplicationCluster(ApplicationFullName fullName) {
        if (fullName == null) {
            throw new IllegalArgumentException("ApplicationFullName must be defined!");
        }
        this.fullName = fullName;
    }

    public ApplicationFullName getFullName() {
        return fullName;
    }

    public Set<Integer> getPorts() {
        return ports;
    }

    public void addPort(int port) {
        ports.add(port);
    }

    public String toString() {
        StringBuilder result = new StringBuilder();
        result.append(fullName).append(" - ");
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

        if (!fullName.equals(that.fullName)) return false;
        return ports.equals(that.ports);

    }

    @Override
    public int hashCode() {
        int result = fullName.hashCode();
        result = 31 * result + ports.hashCode();
        return result;
    }
}
