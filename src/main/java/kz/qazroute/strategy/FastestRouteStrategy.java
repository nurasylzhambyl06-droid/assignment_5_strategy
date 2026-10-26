package kz.qazroute.strategy;

import java.util.List;

import kz.qazroute.model.Route;

/**
 * Chooses the route with the lowest EFFECTIVE delivery time.
 *
 * <pre>
 * effectiveTime = estimatedMinutes + trafficPenalty
 * </pre>
 *
 * The same effective time is used for the deadline check in {@link RouteConstraints},
 * so a route that is only fast "on paper" (before traffic) cannot sneak past the deadline.
 */
public class FastestRouteStrategy extends AbstractRouteStrategy {

    @Override
    public String getStrategyName() {
        return "FastestRouteStrategy";
    }

    @Override
    protected double score(Route route, List<Route> candidates) {
        return route.effectiveMinutes();
    }

    @Override
    public List<String> describeScore(Route route, List<Route> candidates) {
        return List.of(
                "Base estimated time: " + route.estimatedMinutes() + " minutes",
                "Traffic penalty: " + route.trafficPenalty() + " minutes",
                "Effective time: " + route.effectiveMinutes() + " minutes (lowest wins)");
    }
}
