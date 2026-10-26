package kz.qazroute.planner;

import java.util.List;

import kz.qazroute.model.DeliveryRequest;
import kz.qazroute.model.Route;
import kz.qazroute.strategy.RouteStrategy;

/**
 * The Context of the Strategy Pattern.
 *
 * <p>It holds a {@link RouteStrategy} and delegates the choice to it. It knows nothing about
 * fastest, cheapest or any other concrete algorithm, so new strategies can be plugged in
 * without changing this class.</p>
 */
public class RoutePlanner {

    private RouteStrategy strategy;

    public RoutePlanner(RouteStrategy strategy) {
        this.strategy = requireStrategy(strategy);
    }

    /** Changes the algorithm at runtime; the same planner object can be reused. */
    public void setStrategy(RouteStrategy strategy) {
        this.strategy = requireStrategy(strategy);
    }

    public RouteStrategy getStrategy() {
        return strategy;
    }

    /** Delegates to the current strategy. */
    public Route planRoute(List<Route> routes, DeliveryRequest request) {
        return strategy.selectRoute(routes, request);
    }

    private static RouteStrategy requireStrategy(RouteStrategy strategy) {
        if (strategy == null) {
            throw new IllegalArgumentException("Route strategy must not be null.");
        }
        return strategy;
    }
}
