package cloudone.cumulonimbus;

import sun.jvm.hotspot.utilities.Interval;

import java.util.Properties;

/**
 *
 *
 * @author Martin Mares (martin.mares at oracle.com)
 */
public class PortService {

    private static class PortRange {
        private final int from;
        private final int to;
        private int index = -1;

        public PortRange(String range) throws Exception {
            if (range == null) {
                throw new Exception("Invalid port range " + range);
            }
            int ind = range.indexOf('-');
            String sFrom = range;
            String sTo = "";
            if (ind >= 0) {
                sFrom = range.substring(0, ind).trim();
                sTo = range.substring(ind + 1).trim();
                if (sTo.length() == 0) {
                    sTo = String.valueOf(Integer.MAX_VALUE);
                }
            }
            if (sFrom.length() == 0) {
                sFrom = sTo;
            } else if (sTo.length() == 0) {
                sTo = sFrom;
            }
            if (sFrom.length() == 0 && sTo.length() == 0) {
                throw new Exception("Invalid port range " + range);
            }
            from = Integer.parseInt(sFrom);
            to = Integer.parseInt(sTo);
        }

        public PortRange(int from, int to) {
            this.from = from;
            this.to = to;
        }

        public synchronized int getNext() {
            if (index < 0) {
                index = from;
                return index;
            }
            index++;
            if (index > to) {
                index = from;
            }
            return index;
        }
    }

    private static PortService INSTANCE;

    private final PortRange appRange;
    private final PortRange adminRange;

    private PortService(Properties properties) throws Exception {
        appRange = new PortRange(properties.getProperty("port.range.admin", "4300-4399"));
        adminRange = new PortRange(properties.getProperty("port.range", "4400-4499"));
    }

    static PortService init(Properties properties) throws Exception {
        INSTANCE = new PortService(properties);
        return INSTANCE;
    }

    public static PortService getInstance() {
        return INSTANCE;
    }

}
