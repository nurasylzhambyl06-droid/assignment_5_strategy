package kz.qazroute.strategy;

import java.util.List;
import java.util.Locale;

import kz.qazroute.model.Route;

/**
 * Chooses the route with the lowest environmental impact.
 *
 * <pre>
 * ecoScore = carbonEmission * emissionWeight + distanceKm * distanceWeight
 * </pre>
 *
 * <p>Why is a LOWER score better? Both ingredients are "pollution-like": every extra kilogram of CO2
 * and every extra kilometre driven (more fuel, more wear, more road use) harms the environment more.
 * So the smaller the sum, the greener the route. CO2 gets the larger default weight (0.7) because it
 * is the direct measurement of harm, while distance (0.3) is only an indirect signal.</p>
 */
public class EcoFriendlyRouteStrategy extends AbstractRouteStrategy {

    private static final double DEFAULT_EMISSION_WEIGHT = 0.7;
    private static final double DEFAULT_DISTANCE_WEIGHT = 0.3;

    private final double emissionWeight;
    private final double distanceWeight;

    public EcoFriendlyRouteStrategy() {
        this(DEFAULT_EMISSION_WEIGHT, DEFAULT_DISTANCE_WEIGHT);
    }

    public EcoFriendlyRouteStrategy(double emissionWeight, double distanceWeight) {
        if (!Double.isFinite(emissionWeight) || !Double.isFinite(distanceWeight)
                || emissionWeight < 0 || distanceWeight < 0) {
            throw new IllegalArgumentException("Eco weights must be finite and not negative.");
        }
        if (emissionWeight + distanceWeight <= 0) {
            throw new IllegalArgumentException("At least one eco weight must be greater than zero.");
        }
        this.emissionWeight = emissionWeight;
        this.distanceWeight = distanceWeight;
    }

    @Override
    public String getStrategyName() {
        return "EcoFriendlyRouteStrategy";
    }

    @Override
    protected double score(Route route, List<Route> candidates) {
        return route.carbonEmission() * emissionWeight + route.distanceKm() * distanceWeight;
    }

    @Override
    public List<String> describeScore(Route route, List<Route> candidates) {
        return List.of(
                String.format(Locale.US, "CO2 emission: %.0f kg x %.2f = %.1f",
                        route.carbonEmission(), emissionWeight, route.carbonEmission() * emissionWeight),
                String.format(Locale.US, "Distance: %.0f km x %.2f = %.1f",
                        route.distanceKm(), distanceWeight, route.distanceKm() * distanceWeight),
                String.format(Locale.US, "Eco score: %.1f (lowest = least environmental impact)",
                        score(route, candidates)));
    }
}
