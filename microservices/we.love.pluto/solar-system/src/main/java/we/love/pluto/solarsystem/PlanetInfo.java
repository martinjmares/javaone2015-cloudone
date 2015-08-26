package we.love.pluto.solarsystem;

/**
 * Information about planet.
 *
 * @author Martin Mares (martin.mares at oracle.com)
 */
public class PlanetInfo {

    private String name;
    //Radius in km
    private int radius;
    //Sidreal rotation period in hours
    private int rotationPeriod;

    public PlanetInfo(String name, int radius, int rotationPeriod) {
        this.name = name;
        this.radius = radius;
        this.rotationPeriod = rotationPeriod;
    }

    public PlanetInfo() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getRadius() {
        return radius;
    }

    public void setRadius(int radius) {
        this.radius = radius;
    }

    public int getRotationPeriod() {
        return rotationPeriod;
    }

    public void setRotationPeriod(int rotationPeriod) {
        this.rotationPeriod = rotationPeriod;
    }
}
