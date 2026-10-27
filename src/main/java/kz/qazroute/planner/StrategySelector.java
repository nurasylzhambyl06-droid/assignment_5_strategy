package kz.qazroute.planner;

import java.util.EnumMap;
import java.util.Map;

import kz.qazroute.model.DeliveryPriority;
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
 * <p>The mapping is a plain {@link Map} (an {@link EnumMap}, the Map made for enum keys),
 * so there is no {@code switch} and no {@code if/else} chain over priorities:</p>
 *
 * <pre>
 * URGENT   -> FastestRouteStrategy
 * ECONOMY  -> CheapestRouteStrategy
 * ECO      -> EcoFriendlyRouteStrategy
 * STANDARD -> BalancedRouteStrategy
 * </pre>
 *
 * To support a new priority: add the enum constant and ONE {@code put(...)} line in the constructor.
 *
 * <p>Small extra rule: for STANDARD requests with {@code ecoPreferred = true}, the same
 * BalancedRouteStrategy class is used but with eco-leaning weights (emissions 0.60).</p>
 *
 * <p>The strategies are stateless and immutable, so one instance of each can be shared.</p>
 */
public class StrategySelector {

    private final Map<DeliveryPriority, RouteStrategy> strategyByPriority = new EnumMap<>(DeliveryPriority.class);
    private final RouteStrategy ecoLeaningBalanced = new BalancedRouteStrategy(0.20, 0.15, 0.05, 0.60);

    public StrategySelector() {
        strategyByPriority.put(DeliveryPriority.URGENT, new FastestRouteStrategy());
        strategyByPriority.put(DeliveryPriority.ECONOMY, new CheapestRouteStrategy());
        strategyByPriority.put(DeliveryPriority.ECO, new EcoFriendlyRouteStrategy());
        strategyByPriority.put(DeliveryPriority.STANDARD, new BalancedRouteStrategy());
    }

    public RouteStrategy selectFor(DeliveryRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Delivery request must not be null.");
        }
        if (request.priority() == DeliveryPriority.STANDARD && request.ecoPreferred()) {
            return ecoLeaningBalanced;
        }
        RouteStrategy strategy = strategyByPriority.get(request.priority());
        if (strategy == null) {
            throw new IllegalStateException("No strategy is registered for priority " + request.priority());
        }
        return strategy;
    }
}
