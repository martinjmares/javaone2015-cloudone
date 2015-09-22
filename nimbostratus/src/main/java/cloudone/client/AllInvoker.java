package cloudone.client;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;

/**
 * @author Martin Mares (martin.mares at oracle.com)
 */
public interface AllInvoker {

    LaterAllInvoker later();

    AllInvoker accept(String... responseMediaTypes);

    AllInvoker accept(MediaType... mediaTypes);

    MultiResponse method(String name, Entity<?> entity);

    MultiResponse method(String name);

    MultiResponse get();

    MultiResponse put(Entity<?> entity);

    MultiResponse post(Entity<?> entity);

    MultiResponse delete();

    MultiResponse head();

    MultiResponse options();

    MultiResponse trace();

}
