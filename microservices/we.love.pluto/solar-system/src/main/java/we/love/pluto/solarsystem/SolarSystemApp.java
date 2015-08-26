package we.love.pluto.solarsystem;

import cloudone.C1Application;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * C1Application for solar system information services.
 *
 * @author Martin Mares (martin.mares at oracle.com)
 */
public class SolarSystemApp extends C1Application {

    @Override
    public Set<Class<?>> getClasses() {
        return new HashSet<Class<?>>(Arrays.asList(new Class<?>[]{
                SpaceObjectResource.class
        }));
    }

}
