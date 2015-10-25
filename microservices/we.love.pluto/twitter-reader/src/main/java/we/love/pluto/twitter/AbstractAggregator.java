package we.love.pluto.twitter;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Michal Gajdos
 */
abstract class AbstractAggregator implements DataAggregator {

    private final List<DataListener> listeners = new ArrayList<>();

    private volatile String lastMessage;

    @Override
    public List<DataListener> listeners() {
        return listeners;
    }

    @Override
    public DataAggregator listener(final DataListener listener) {
        listeners.add(listener);
        return this;
    }

    @Override
    public String lastMessage() {
        return lastMessage;
    }

    @Override
    public AbstractAggregator message(String message, String username) {
        lastMessage = message;
        listeners.forEach(listener -> listener.onNext(message, username));

        return this;
    }
}
