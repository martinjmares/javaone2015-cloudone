package cloudone.client;

import cloudone.internal.ApplicationFullName;

import javax.ws.rs.core.Response;
import java.util.Set;

/**
 * Represents results of AllInvoker.
 *
 * @author Martin Mares (martin.mares at oracle.com)
 */
public interface MultiResponse extends Iterable<MultiResponse.IdentifiedResponse> {

    /**
     * Response with identification of the endpoint. Endpoint is represented by {@link ApplicationFullName}.
     */
    interface IdentifiedResponse {

        ApplicationFullName getApplicationFullName();
        Response getResponse();
        Exception getError();

    }

    Set<ApplicationFullName> getApplicationNames();

    IdentifiedResponse getResult(ApplicationFullName name);

}
