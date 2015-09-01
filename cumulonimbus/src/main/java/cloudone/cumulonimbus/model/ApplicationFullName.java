package cloudone.cumulonimbus.model;

import cloudone.ServiceFullName;

/**
 * Represents application in the service.
 *
 * @author Martin Mares (martin.mares at oracle.com)
 */
public class ApplicationFullName {

    private ServiceFullName serviceName;
    private String applicationName;

    public ApplicationFullName(ServiceFullName serviceName, String applicationName) {
        this.serviceName = serviceName;
        this.applicationName = applicationName;
    }

    public ServiceFullName getServiceName() {
        return serviceName;
    }

    public String getApplicationName() {
        return applicationName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ApplicationFullName)) return false;

        ApplicationFullName that = (ApplicationFullName) o;

        if (serviceName != null ? !serviceName.equals(that.serviceName) : that.serviceName != null) return false;
        return !(applicationName != null ? !applicationName.equals(that.applicationName) : that.applicationName != null);

    }

    @Override
    public int hashCode() {
        int result = serviceName != null ? serviceName.hashCode() : 0;
        result = 31 * result + (applicationName != null ? applicationName.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return serviceName + ";" + applicationName;
    }
}
