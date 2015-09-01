package cloudone.cumulonimbus.model;

/**
 * One REST resource.
 *
 * @author Martin Mares (martin.mares at oracle.com)
 */
public class RestResourceDescription {

    public enum Method {
        POT, POST, PUT, DELETE, GET, OPTIONS;
    }

    private Method method;
    private String path;

    public RestResourceDescription(Method method, String path) {
        this.method = method;
        this.path = path;
    }

    public RestResourceDescription(String method, String path) {
        this.method = Method.valueOf(method);
        this.path = path;
    }

    public Method getMethod() {
        return method;
    }

    public String getPath() {
        return path;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RestResourceDescription)) return false;

        RestResourceDescription that = (RestResourceDescription) o;

        if (method != that.method) return false;
        return !(path != null ? !path.equals(that.path) : that.path != null);

    }

    @Override
    public int hashCode() {
        int result = method != null ? method.hashCode() : 0;
        result = 31 * result + (path != null ? path.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return method + ": " + path;
    }
}
