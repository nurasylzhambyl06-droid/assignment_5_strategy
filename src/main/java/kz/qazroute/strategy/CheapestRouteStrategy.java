package kz.qazroute.strategy;

import java.util.List;
import java.util.Locale;

import kz.qazroute.model.Route;

/**
 * Chooses the route with the lowest total delivery cost.
 *
 * <pre>
 * totalCost = baseCost + distanceKm * 50 KZT/km     (see Route.totalCost())
 * </pre>
 *
 * The base cost is a fixed fee (loading, handling, tolls); the distance cost grows with kilometres.
 * It is deliberately simple: no fuel prices, taxes or discounts.
 */
public class CheapestRouteStrategy extends AbstractRouteStrategy {

    @Override
    public String getStrategyName() {
        return "CheapestRouteStrategy";
    }

    @Override
    protected double score(Route route, List<Route> candidates) {
        return route.totalCost();
    }

    @Override
    public List<String> describeScore(Route route, List<Route> candidates) {
        double distanceCost = route.distanceKm() * Route.COST_PER_KM_KZT;
        return List.of(
                String.format(Locale.US, "Base cost: %.0f KZT", route.baseCost()),
                String.format(Locale.US, "Distance cost: %.0f km x %.0f KZT/km = %.0f KZT",
                        route.distanceKm(), Route.COST_PER_KM_KZT, distanceCost),
                String.format(Locale.US, "Total cost: %.0f KZT (lowest wins)", route.totalCost()));
    }
}
