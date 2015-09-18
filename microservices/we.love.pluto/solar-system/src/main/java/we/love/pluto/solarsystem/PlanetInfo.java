package we.love.pluto.solarsystem;

/**
 * Information about planet.
 *
 * @author Martin Mares (martin.mares at oracle.com)
 */
public class PlanetInfo {

    private final String name;
    //Radius in km
    private final int radius;
    //Sidreal rotation period in hours
    private final int rotationPeriod;

    public PlanetInfo(String name, int radius, int rotationPeriod) {
        this.name = name;
        this.radius = radius;
        this.rotationPeriod = rotationPeriod;
    }

    public String getName() {
        return name;
    }


    public int getRadius() {
        return radius;
    }

    public int getRotationPeriod() {
        return rotationPeriod;
    }

}
