package cloudone.cumulonimbus.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Utility methods for the path.
 *
 * @author Martin Mares (martin.mares at oracle.com)
 */
public class PathUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(PathUtil.class);
    private static final Pattern MULTISLASHES_PATTERN = Pattern.compile("/{2,}");

    public static String normalizePath(String path) {
        if (path == null || path.length() == 0) {
            path = "/";
        } else if (!path.startsWith("/")) {
            path = "/" + path.trim();
        } else {
            path = path.trim();
        }
        if (path.length() > 1 && path.endsWith("/")) {
            path = path.substring(0, path.length() - 1);
        }
        return path;
    }

    /**
     * Devides path into ints segments.
     *
     * @param path to be parsed.
     * @param toRegexp if {@code true} then it will parse path parameter definition into regexp.
     */
    public static List<String> parsePath(String path, final boolean toRegexp) {
        if (path == null) {
            return Collections.emptyList();
        }
        path = path.trim();
        if (path.length() == 0 || "/".equals(path)) {
            return Collections.emptyList();
        }
        List<String> result = new ArrayList<>();
        boolean first = true;
        StringBuilder standard = new StringBuilder();
        StringBuilder parameter = null;
        boolean inParameter = false;
        for (char c : path.toCharArray()) {
            if (first) {
                first = false;
                if (c == '/') {
                    continue; //Must skip first slash
                }
            }
            switch (c) {
                case '/':
                    if (inParameter) {
                        parameter.append(c);
                    } else {
                        result.add(standard.toString());
                        standard.setLength(0);
                    }
                    break;
                case '{':
                    if (inParameter) {
                        parameter.append(c);
                    } else if (toRegexp) {
                        inParameter = true;
                        parameter = new StringBuilder();
                    } else {
                        standard.append(c);
                    }
                    break;
                case '}':
                    if (inParameter) {
                        standard.append(deriveRegexpFromPathParam(parameter.toString()));
                        parameter = null;
                        inParameter = false;
                    } else if (toRegexp) {
                        if (escapeInRegexp(c)) {
                            standard.append('\\');
                        }
                        standard.append(c);
                    } else {
                        standard.append(c);
                    }
                    break;
                default:
                    if (inParameter) {
                        parameter.append(c);
                    } else {
                        if (toRegexp && escapeInRegexp(c)) {
                            standard.append('\\');
                        }
                        standard.append(c);
                    }
            }
        }
        if (standard.length() > 0) {
            result.add(standard.toString());
        }
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("parsePath(" + path + ", " + toRegexp + ") -> " + result);
        }
        return result;
    }

    private static boolean escapeInRegexp(char c) {
        return ".^$*+?()[{\\|}".indexOf(c) > -1;
    }

    private static String deriveRegexpFromPathParam(String pathParam) {
        if (pathParam == null) {
            return "";
        }
        if (pathParam.endsWith("}")) {
            pathParam = pathParam.substring(pathParam.length() - 1);
        }
        int ind = pathParam.indexOf(':');
        if (ind >= 0) {
            return "(" + pathParam.substring(ind + 1) + ")";
        } else {
            return "(.*)";
        }
    }
}
