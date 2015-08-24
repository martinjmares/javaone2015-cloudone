package cloudone.internal.dto;

/**
 * @author Martin Mares (martin.mares at oracle.com)
 */
public class RuntimeIdAndSecCode {

    private int runtimeId;
    private String securityCode;

    public RuntimeIdAndSecCode(int runtimeId, String securityCode) {
        this.runtimeId = runtimeId;
        this.securityCode = securityCode;
    }

    public RuntimeIdAndSecCode() {
    }

    public int getRuntimeId() {
        return runtimeId;
    }

    public void setRuntimeId(int runtimeId) {
        this.runtimeId = runtimeId;
    }

    public String getSecurityCode() {
        return securityCode;
    }

    public void setSecurityCode(String securityCode) {
        this.securityCode = securityCode;
    }
}
