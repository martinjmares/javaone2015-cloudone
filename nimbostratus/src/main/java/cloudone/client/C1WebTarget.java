package cloudone.client;

import javax.ws.rs.client.WebTarget;
import java.util.Map;

/**
 * @author Martin Mares (martin.mares at oracle.com)
 */
public interface C1WebTarget extends WebTarget {

    public class CodeInterval {

        public static final CodeInterval SUCCESSFUL = new CodeInterval(200, 299);
        public static final CodeInterval REDIRECTION = new CodeInterval(300, 399);
        public static final CodeInterval CLIENT_ERROR = new CodeInterval(400, 499);
        public static final CodeInterval SERVER_ERROR = new CodeInterval(500, 599);

        final int from;
        final int to;

        public CodeInterval(int from, int to) {
            this.from = from;
            this.to = to;
        }

        public CodeInterval(int only) {
            this(only, only);
        }

        public int getFrom() {
            return from;
        }

        public int getTo() {
            return to;
        }

        public boolean isInInterval(int code) {
            return code >= from && code <= to;
        }

    }
    public C1WebTarget path(String path);

    public C1WebTarget resolveTemplate(String name, Object value);

    public C1WebTarget resolveTemplate(String name, Object value, boolean encodeSlashInPath);

    public C1WebTarget resolveTemplateFromEncoded(String name, Object value);

    public C1WebTarget resolveTemplates(Map<String, Object> templateValues);

    public C1WebTarget resolveTemplates(Map<String, Object> templateValues, boolean encodeSlashInPath);

    public C1WebTarget resolveTemplatesFromEncoded(Map<String, Object> templateValues);

    public C1WebTarget matrixParam(String name, Object... values);

    public C1WebTarget queryParam(String name, Object... values);

    public AnyInvoker anyOK();

    public AnyInvoker any(CodeInterval... accepts);

    public AllInvoker all();

}
