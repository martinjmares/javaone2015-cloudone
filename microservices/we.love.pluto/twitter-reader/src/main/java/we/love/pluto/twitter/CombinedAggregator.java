package we.love.pluto.twitter;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Michal Gajdos
 */
final class CombinedAggregator extends AbstractAggregator {

    private final List<DataAggregator> aggregators;

    CombinedAggregator(final DataAggregator... aggregators) {
        this.aggregators = Arrays.asList(aggregators);
        this.aggregators.forEach(aggregator -> aggregator.listener(this::message));
    }

    @Override
    public List<DataListener> listeners() {
        return aggregators.stream()
                .flatMap(aggregator -> aggregator.listeners().stream())
                .collect(Collectors.toList());
    }

    @Override
    public DataAggregator start(final String... keywords) {
        aggregators.forEach(aggregator -> aggregator.start(keywords));

        return this;
    }

    @Override
    public void stop() {
        aggregators.forEach(DataAggregator::stop);
    }
}
