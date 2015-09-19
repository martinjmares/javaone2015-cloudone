package we.love.pluto.milkyway;

import java.util.HashMap;
import java.util.Map;

/**
 * Static data about stars in Milky Way.
 *
 * @author Martin Mares (martin.mares at oracle.com)
 */
public class StarInfoService {

    private static final Map<String, StarInfo> infos = new HashMap<>();

    private static void add(double radius, int rotationPeriod, String constellation, String name) {
        StarInfo info = new StarInfo(name, constellation, (int) (StarInfo.SOLAR_RADIUS * radius), rotationPeriod);
        infos.put(name.toLowerCase(), info);
    }

    static {
        add(1, 25 * 24, null, "Sun");
        add(0.141, (int) (83.5 * 24), "Centaurus", "Proxima Centauri");
        add(1.227, 22 * 24, "Centaurus", "Alpha Centauri A");
        add(0.865, 41 * 24, "Centaurus", "Alpha Centauri B");
    }

    public static StarInfo getStarInfo(String name) {
        if (name == null) {
            return null;
        }
        return infos.get(name.replace('_', ' ').toLowerCase());
    }

}
