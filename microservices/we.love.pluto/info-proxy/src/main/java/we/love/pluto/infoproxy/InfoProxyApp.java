package we.love.pluto.infoproxy;

import cloudone.C1Application;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Main application for simple proxy service.
 *
 * @author Martin Mares (martin.mares at oracle.com)
 */
public class InfoProxyApp extends C1Application {

    @Override
    public Set<Class<?>> getClasses() {
        return new HashSet<Class<?>>(Arrays.asList(new Class<?>[]{
                InfoResource.class
        }));
    }

}
