package kz.qazroute.strategy;

import java.util.Comparator;
import java.util.List;

import kz.qazroute.model.DeliveryRequest;
import kz.qazroute.model.Route;

/**
 * Shared skeleton of every concrete strategy, so the selection steps are written only once:
 *
 * <ol>
 *   <li>Remove routes that break a hard constraint ({@link RouteConstraints}).</li>
 *   <li>Give every remaining route a score (this is the only part that differs per strategy).</li>
 *   <li>Return the route with the LOWEST score. If scores are equal, the smaller route id wins,
 *       so results are always deterministic.</li>
 * </ol>
 *
 * <p>Concrete strategies still implement {@link RouteStrategy}; they only add their own
 * {@link #score(Route, List)}.</p>
 */
public abstract class AbstractRouteStrategy implements RouteStrategy {

    @Override
    public final Route selectRoute(List<Route> routes, DeliveryRequest request) {
        List<Route> candidates = RouteConstraints.filterFeasible(routes, request);
        return candidates.stream()
                .min(Comparator.comparingDouble((Route route) -> score(route, candidates))
                        .thenComparing(Route::id))
                .orElseThrow(() -> new IllegalStateException("No candidates left after filtering."));
    }

    /**
     * Score of a route: LOWER IS ALWAYS BETTER.
     *
     * @param candidates all routes that passed the hard constraints (used for normalization)
     */
    protected abstract double score(Route route, List<Route> candidates);
}
