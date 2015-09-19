package we.love.pluto.milkyway;

/**
 * Information about a star.
 *
 * @author Martin Mares (martin.mares at oracle.com)
 */
public class StarInfo {

    public static final int SOLAR_RADIUS = 695500;

    private final String name;

    private final String constelation;
    //Radius in km
    private final int radius;
    //Sidreal rotation period in hours
    private final int rotationPeriod;

    public StarInfo(String name, String constelation, int radius, int rotationPeriod) {
        this.name = name;
        this.constelation = constelation;
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
