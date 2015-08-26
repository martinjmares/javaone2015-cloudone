package cloudone.internal.nimbostratus;

import cloudone.C1Application;
import cloudone.internal.provider.DurationMessageBodyWriter;
import cloudone.internal.resources.LifecycleResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Main administration part microservice application.
 *
 * @author Martin Mares (martin.mares at oracle.com)
 */
public class NimbostratusApp extends C1Application {

    private static final Logger LOGGER = LoggerFactory.getLogger(NimbostratusApp.class);

    public Set<Class<?>> getClasses() {
        return new HashSet<Class<?>>(Arrays.asList(new Class<?>[]{
                LifecycleResource.class,
                DurationMessageBodyWriter.class
        }));
    }
}
