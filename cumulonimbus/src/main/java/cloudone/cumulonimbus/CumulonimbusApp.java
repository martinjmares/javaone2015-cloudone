package cloudone.cumulonimbus;

import cloudone.C1Application;
import cloudone.cumulonimbus.resources.LifecycleResource;
import cloudone.internal.provider.DurationMessageBodyWriter;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Cumulonimbus represents central cloudOne service. Main goals are to provide configuration and navigation services.
 *
 * @author Martin Mares (martin.mares at oracle.com)
 */
public class CumulonimbusApp extends C1Application {

    public Set<Class<?>> getClasses() {
        return new HashSet<Class<?>>(Arrays.asList(new Class<?>[] {
                LifecycleResource.class,
                DurationMessageBodyWriter.class
        }));
    }
}
