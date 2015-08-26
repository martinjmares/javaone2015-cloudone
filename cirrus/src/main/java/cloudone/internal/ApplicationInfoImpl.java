package cloudone.internal;

import cloudone.ApplicationInfo;
import cloudone.C1Application;

/**
 *  @author Martin Mares (martin.mares at oracle.com)
 */
public class ApplicationInfoImpl implements ApplicationInfo {

    private static final String[] COMMON_NAME_SUFIXES = new String[] {"c1application", "application", "app"};

    private final C1Application application;
    private final String name;
    private int port;

    public ApplicationInfoImpl(C1Application application, int port) {
        this.application = application;
        this.name = constructApplicationName(application);
        this.port = port;
    }

    public ApplicationInfoImpl(C1Application application) {
        this(application, -1);
    }

    public ApplicationInfoImpl(Class<C1Application> appClass) throws IllegalAccessException, InstantiationException {
        this(appClass.newInstance());
    }

    private static String constructApplicationName(C1Application application) {
        if (application == null) {
            return "";
        }
        String result = application.getClass().getSimpleName();
        String sResult = result.toLowerCase();
        for (String commonNameSufix : COMMON_NAME_SUFIXES) {
            if (result.toLowerCase().endsWith(commonNameSufix) && result.length() > commonNameSufix.length()) {
                result = result.substring(0, result.length() - commonNameSufix.length());
                break;
            }
        }
        return result;
    }

    public C1Application getApplication() {
        return application;
    }

    public String getName() {
        return name;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }
}
