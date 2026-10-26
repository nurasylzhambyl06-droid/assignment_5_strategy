package kz.qazroute.planner;

import kz.qazroute.model.DeliveryRequest;
import kz.qazroute.strategy.BalancedRouteStrategy;
import kz.qazroute.strategy.CheapestRouteStrategy;
import kz.qazroute.strategy.EcoFriendlyRouteStrategy;
import kz.qazroute.strategy.FastestRouteStrategy;
import kz.qazroute.strategy.RouteStrategy;

/**
 * Maps a delivery request to the strategy that fits it. It only chooses WHICH strategy to use;
 * the algorithms themselves stay inside the strategy classes.
 *
 * <pre>
 * URGENT   -> FastestRouteStrategy
 * ECONOMY  -> CheapestRouteStrategy
 * ECO      -> EcoFriendlyRouteStrategy
 * STANDARD -> BalancedRouteStrategy
 * </pre>
 *
 * Small extra rule: for STANDARD requests with {@code ecoPreferred = true}, the same
 * BalancedRouteStrategy class is used but with eco-leaning weights (emissions 0.60).
 *
 * <p>The strategies are stateless and immutable, so one instance of each can be shared.</p>
 */
public class StrategySelector {

    private final RouteStrategy fastest = new FastestRouteStrategy();
    private final RouteStrategy cheapest = new CheapestRouteStrategy();
    private final RouteStrategy ecoFriendly = new EcoFriendlyRouteStrategy();
    private final RouteStrategy balanced = new BalancedRouteStrategy();
    private final RouteStrategy ecoLeaningBalanced = new BalancedRouteStrategy(0.20, 0.15, 0.05, 0.60);

    public RouteStrategy selectFor(DeliveryRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Delivery request must not be null.");
        }
        return switch (request.priority()) {
            case URGENT -> fastest;
            case ECONOMY -> cheapest;
            case ECO -> ecoFriendly;
            case STANDARD -> request.ecoPreferred() ? ecoLeaningBalanced : balanced;
        };
    }
}
