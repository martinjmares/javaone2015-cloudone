package cloudone.cumulonimbus.model;

import cloudone.cumulonimbus.util.PathUtil;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.regex.Pattern;

/**
 * Tree model of registered paths.
 *
 * @author Martin Mares (martin.mares at oracle.com)
 */
public class PathRegistry {


    private static class Node {

        public enum Type {
            EXACT, ANY, REGEXP;
        }

        private final Type type;
        private final String pathElement;
        private final Pattern pattern;

        private EnumMap<HttpMethod, Set<ApplicationFullName>> registry;
        private Map<String, Node> subNodes;

        /**
         * Just to construct root node.
         */
        public Node() {
            type = Type.ANY;
            pathElement = "";
            pattern = null;
        }

        public Node(String pathElement) {
            if (pathElement == null) {
                pathElement = "";
            }
            this.pathElement = pathElement;
            if ("(.*)".equals(pathElement)) {
                type = Type.ANY;
                pattern = null;
            } else if (pathElement.indexOf('\\') > -1 || pathElement.indexOf('(') > -1) {
                type = Type.REGEXP;
                pattern = Pattern.compile(pathElement);
            } else {
                type = Type.EXACT;
                pattern = null;
            }
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Node)) return false;
            Node node = (Node) o;
            return pathElement.equals(node.pathElement);
        }

        @Override
        public int hashCode() {
            return pathElement.hashCode();
        }

        public boolean matches(String element) {
            switch (type) {
                case EXACT:
                    return pathElement.equals(element);
                case ANY:
                    return true;
                case REGEXP:
                    return pattern.matcher(element).matches();
                default:
                    return false;
            }
        }

        public String getPathElement() {
            return pathElement;
        }

        public void register(final HttpMethod method, final ApplicationFullName appName) {
            if (registry == null) {
                registry = new EnumMap<>(HttpMethod.class);
            }
            registry.computeIfAbsent(method, m -> new HashSet<>()).add(appName);
        }

        public Node addNode(final Node node) {
            if (subNodes == null) {
                subNodes = new HashMap<>();
                subNodes.put(node.getPathElement(), node);
                return node;
            } else if (!subNodes.containsKey(node.getPathElement())) {
                return subNodes.get(node.getPathElement());
            } else {
                subNodes.put(node.getPathElement(), node);
                return node;
            }
        }

        public void registerToPath(final List<String> path,
                                   final int index,
                                   final HttpMethod method,
                                   final ApplicationFullName appName) {
            if (index < path.size()) {
                Node node = new Node(path.get(index));
                node = addNode(node);
                node.registerToPath(path, index + 1, method, appName);
            } else {
                register(method, appName);
            }
        }

        public void unregisterFromPath(final ApplicationFullName appName) {
            //First unregister here
            if (registry != null) {
                Set<HttpMethod> toRemove = EnumSet.noneOf(HttpMethod.class);
                for (Map.Entry<HttpMethod, Set<ApplicationFullName>> entry : registry.entrySet()) {
                    if (entry.getValue().remove(appName) && entry.getValue().isEmpty()) {
                        toRemove.add(entry.getKey());
                    }
                }
                for (HttpMethod method : toRemove) {
                    registry.remove(method);
                }
                if (registry.isEmpty()) {
                    registry = null;
                }
            }
            //Now unrwgister subPath
            if (subNodes != null) {
                Set<String> toRemove = new HashSet<>(1);
                for (Map.Entry<String, Node> entry : subNodes.entrySet()) {
                    Node subNode = entry.getValue();
                    subNode.unregisterFromPath(appName);
                    if (subNode.registry == null && subNode.subNodes == null) {
                        toRemove.add(entry.getKey());
                    }
                }
                for (String key : toRemove) {
                    subNodes.remove(key);
                }
                if (subNodes.isEmpty()) {
                    subNodes = null;
                }
            }
        }

        public void collectPossibilities(final List<String> path,
                                         final int index,
                                         final HttpMethod method,
                                         final Set<ApplicationFullName> appNames) {
            if (path.size() > index) {
                String pathElement = path.get(index);
                for (Node subNode : subNodes.values()) {
                    if (subNode.matches(pathElement)) {
                        collectPossibilities(path, index + 1, method, appNames);
                    }
                }
            } else if (registry != null) {
                Set<ApplicationFullName> result = registry.get(method);
                appNames.addAll(result);
            }
        }

    }

    private final ReadWriteLock lock = new ReentrantReadWriteLock();
    private final Node root = new Node();

    public void register(final String path, final HttpMethod method, final ApplicationFullName appName) {
        final List<String> parsedPath = PathUtil.parsePath(path, true);
        lock.writeLock().lock();
        try {
            root.registerToPath(parsedPath, 0, method, appName);
        } finally {
            lock.writeLock().unlock();
        }
    }

    public void unregister(final ApplicationFullName appName) {
        lock.writeLock().lock();
        try {
            root.unregisterFromPath(appName);
        } finally {
            lock.writeLock().unlock();
        }
    }

    public Set<ApplicationFullName> get(final String path, final HttpMethod method) {
        final List<String> parsedPath = PathUtil.parsePath(path, true);
        final Set<ApplicationFullName> result = new HashSet<>();
        lock.readLock().lock();
        try {
            root.collectPossibilities(parsedPath, 0, method, result);
        } finally {
            lock.readLock().unlock();
        }
        return result;
    }


}
