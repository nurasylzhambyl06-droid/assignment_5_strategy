package kz.qazroute.strategy;

import java.util.List;

import kz.qazroute.model.DeliveryRequest;
import kz.qazroute.model.Route;

/**
 * The Strategy interface: one family of algorithms ("pick a route"), each in its own class.
 *
 * <p>{@code RoutePlanner} (the Context) only knows this interface, never the concrete classes.</p>
 */
public interface RouteStrategy {

    /**
     * Picks the best route according to this strategy's own rule.
     *
     * @throws kz.qazroute.exception.NoSuitableRouteException if the list is empty or no route
     *         satisfies the hard constraints (availability, budget, deadline, cities)
     * @throws IllegalArgumentException if {@code routes} or {@code request} is null
     */
    Route selectRoute(List<Route> routes, DeliveryRequest request);

    /** Short readable name, used in console output and explanations. */
    String getStrategyName();

    /**
     * Explains, line by line, how this strategy scored a route (used by explanations).
     *
     * @param route      the route to describe
     * @param candidates the routes that passed the hard constraints (some scores, like the
     *                   balanced one, are relative to this group)
     */
    List<String> describeScore(Route route, List<Route> candidates);
}
