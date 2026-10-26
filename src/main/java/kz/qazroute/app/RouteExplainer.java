package kz.qazroute.app;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import kz.qazroute.model.DeliveryRequest;
import kz.qazroute.model.Route;
import kz.qazroute.strategy.RouteConstraints;
import kz.qazroute.strategy.RouteStrategy;

/**
 * Builds a short, readable explanation of a strategy's decision:
 * the selected route, how it was scored, and why other routes were rejected.
 */
public final class RouteExplainer {

    private RouteExplainer() {
        // utility class
    }

    /**
     * @throws kz.qazroute.exception.NoSuitableRouteException if the strategy cannot select a route
     */
    public static String explain(RouteStrategy strategy, List<Route> routes, DeliveryRequest request) {
        Route selected = strategy.selectRoute(routes, request);
        List<Route> candidates = RouteConstraints.filterFeasible(routes, request);

        List<String> lines = new ArrayList<>();
        lines.add("Selected route: " + selected.id() + " (" + selected.name() + ")");
        lines.add("Strategy: " + strategy.getStrategyName());
        lines.addAll(strategy.describeScore(selected, candidates));

        for (Route route : routes) {
            Optional<String> violation = RouteConstraints.findViolation(route, request);
            if (violation.isPresent()) {
                lines.add("");
                lines.add("Rejected route " + route.id() + ":");
                lines.add("  " + violation.get());
            }
        }
        return String.join("\n", lines);
    }
}
