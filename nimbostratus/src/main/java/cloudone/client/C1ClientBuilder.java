package cloudone.client;

import cloudone.client.internal.C1ClientImpl;
import org.glassfish.jersey.client.JerseyClientBuilder;

/**
 * Main entry point to the client API used to bootstrap {@link cloudone.client.C1Client}
 * instances.
 *
 * @author Martin Mares (martin.mares at oracle.com)
 */
public class C1ClientBuilder extends JerseyClientBuilder {

    public C1ClientImpl build() {
        return new C1ClientImpl(super.build());
    }
}
