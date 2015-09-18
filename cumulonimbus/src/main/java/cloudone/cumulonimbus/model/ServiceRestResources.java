package cloudone.cumulonimbus.model;

import cloudone.internal.ApplicationFullName;

import java.util.ArrayList;
import java.util.List;

/**
 * All rest resources of one service.
 *
 * @author Martin Mares (martin.mares at oracle.com)
 */
public class ServiceRestResources {

    private ApplicationFullName name;
    private List<RestResourceDescription> resources = new ArrayList<>();

    public ServiceRestResources(ApplicationFullName name, List<RestResourceDescription> resources) {
        this.name = name;
        this.resources.addAll(resources);
    }

    public ApplicationFullName getName() {
        return name;
    }

    public List<RestResourceDescription> getResources() {
        return resources;
    }
}
