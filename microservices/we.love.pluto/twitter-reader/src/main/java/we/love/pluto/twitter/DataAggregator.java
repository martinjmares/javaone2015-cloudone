package we.love.pluto.twitter;

import java.util.List;

import org.glassfish.jersey.spi.Contract;

/**
 */
@Contract
interface DataAggregator {

    String lastMessage();

    List<DataListener> listeners();

    DataAggregator start(String... keywords);

    DataAggregator listener(DataListener listener);

    DataAggregator message(String message);

    void stop();

}
