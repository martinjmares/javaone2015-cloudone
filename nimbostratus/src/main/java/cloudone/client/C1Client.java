package cloudone.client;

import cloudone.ServiceFullName;

import javax.ws.rs.client.Client;

/**
 * Extended JAX-RS client.
 *
 * @author Martin Mares (martin.mares at oracle.com)
 */
public interface C1Client extends Client {

    /**
     * Target to any accessible cloud application which supports rest resource based on the path.
     */
    C1WebTarget target();

    /**
     * Resolves uri based on the service name.
     *
     * @param serviceNames of the local cloud service.
     * @return
     */
    C1WebTarget target(ServiceFullName... serviceNames);
}
