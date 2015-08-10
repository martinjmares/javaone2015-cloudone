package cloudone;

import cloudone.internal.RuntimeInfoImpl;

/** Provides access to all cloudOne services.
 *
 * @author Martin Mares (martin.mares at oracle.com)
 */
public class C1Services {

    private static final C1Services INSTANCE = new C1Services();

    /** Returns basic initialisation information for this cloudOne server instance.
     */
    public RuntimeInfo getRuntimeInfo() {
        return RuntimeInfoImpl.getInstance();
    }

    public static C1Services getInstance() {
        return INSTANCE;
    }
}
