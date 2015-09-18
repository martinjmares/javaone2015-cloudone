package cloudone.client;

import javax.ws.rs.client.SyncInvoker;
import javax.ws.rs.core.MediaType;

/**
 * @author Martin Mares (martin.mares at oracle.com)
 */
public interface AnyInvoker extends SyncInvoker {

    public LaterAnyInvoker later();

    public AnyInvoker accept(String... mediaTypes);

    public AnyInvoker accept(MediaType... mediaTypes);
}
