package we.love.pluto.milkyway;

import cloudone.C1Application;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Martin Mares (martin.mares at oracle.com)
 */
public class MilkyWayApp extends C1Application {

    @Override
    public Set<Class<?>> getClasses() {
        return new HashSet<Class<?>>(Arrays.asList(new Class<?>[]{
                UniverseResource.class
        }));
    }

}
