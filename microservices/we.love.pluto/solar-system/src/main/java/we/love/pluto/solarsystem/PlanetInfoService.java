package we.love.pluto.solarsystem;

import java.util.HashMap;
import java.util.Map;

/**
 * Static data about Solar system planets.
 *
 * @author Martin Mares (martin.mares at oracle.com)
 */
public class PlanetInfoService {

    private static final Map<String, PlanetInfo> infos = new HashMap<>();

    private static void add(int radius, int rotationPeriod, String... name) {
        PlanetInfo info = new PlanetInfo(name[0], radius, rotationPeriod);
        for (String nm : name) {
            infos.put(nm.toLowerCase(), info);
        }
    }

    static {
        add(2440, 1408, "Mercury");
        add(6052, 244 * 24, "Venus");
        add(6371, 24, "Earth");
        add(3390, 25, "Mars");
        add(69991, 10, "Jupiter");
        add(58232, 11, "Saturn");
        add(25362, 17, "Uranus");
        add(24622, 16, "Neptune");
        add(1186, 6 * 24 + 9, "Pluto");
    }

    public static PlanetInfo getPlanetInfo(String name) {
        if (name == null) {
            return null;
        }
        return infos.get(name.toLowerCase());
    }

}
