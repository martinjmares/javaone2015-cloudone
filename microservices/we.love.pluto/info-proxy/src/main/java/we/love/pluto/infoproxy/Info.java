package we.love.pluto.infoproxy;

/**
 * General information about space object.
 *
 * @author Martin Mares (martin.mares at oracle.com)
 */
public class Info {

    private String name;
    //Radius in km
    private int radius;
    //Sidreal rotation period in hours
    private int rotationPeriod;

    public String getName() {
        return name;
    }

    public int getRadius() {
        return radius;
    }

    public int getRotationPeriod() {
        return rotationPeriod;
    }

    public String toString() {
        StringBuilder result = new StringBuilder();
        result.append(name).append(':').append('\n');
        result.append("    ").append("Radius:   ").append(radius).append(" km").append('\n');
        result.append("    ").append("Rotation: ").append(radius).append(" hrs").append('\n');
        return result.toString();
    }
}
