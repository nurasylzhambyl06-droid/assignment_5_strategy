package kz.qazroute.strategy;

import java.util.List;
import java.util.Locale;

import kz.qazroute.model.Route;

/**
 * Fifth strategy: chooses the route with the lowest distance (hard constraints still apply).
 *
 * <p>Adding this class required NO change to {@code RoutePlanner} (Open/Closed Principle).</p>
 */
public class DistanceFirstStrategy extends AbstractRouteStrategy {

    @Override
    public String getStrategyName() {
        return "DistanceFirstStrategy";
    }

    @Override
    protected double score(Route route, List<Route> candidates) {
        return route.distanceKm();
    }

    @Override
    public List<String> describeScore(Route route, List<Route> candidates) {
        return List.of(String.format(Locale.US, "Distance: %.0f km (lowest wins)", route.distanceKm()));
    }
}
