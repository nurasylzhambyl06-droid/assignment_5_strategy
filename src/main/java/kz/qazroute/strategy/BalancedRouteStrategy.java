package kz.qazroute.strategy;

import java.util.List;
import java.util.Locale;
import java.util.function.ToDoubleFunction;

import kz.qazroute.model.Route;

/**
 * Chooses the route with the best compromise between time, cost, distance and CO2.
 *
 * <h2>Step 1 - normalization (min-max)</h2>
 * Minutes, KZT, kilometres and kilograms cannot be added directly. So each criterion is converted
 * to a number between 0 and 1 among the routes that passed the hard constraints:
 * <pre>
 * normalized = (value - minValue) / (maxValue - minValue)
 * </pre>
 * 0 means "the best of the group", 1 means "the worst of the group".
 * If all routes have the same value, the criterion cannot separate them and counts as 0.
 *
 * <h2>Step 2 - weighted sum</h2>
 * <pre>
 * score = timeWeight * normTime + costWeight * normCost
 *       + distanceWeight * normDistance + emissionWeight * normEmission
 * </pre>
 * The weights must be non-negative and add up to 1.0. Lower score = better compromise.
 * "Time" is the effective time (travel + traffic), "cost" is the total cost.
 */
public class BalancedRouteStrategy extends AbstractRouteStrategy {

    private static final double SUM_TOLERANCE = 1e-9;

    private final double timeWeight;
    private final double costWeight;
    private final double distanceWeight;
    private final double emissionWeight;

    /** Default weights: time 0.40, cost 0.30, distance 0.10, emissions 0.20. */
    public BalancedRouteStrategy() {
        this(0.40, 0.30, 0.10, 0.20);
    }

    public BalancedRouteStrategy(double timeWeight, double costWeight,
                                 double distanceWeight, double emissionWeight) {
        double[] weights = {timeWeight, costWeight, distanceWeight, emissionWeight};
        double sum = 0;
        for (double weight : weights) {
            if (!Double.isFinite(weight) || weight < 0) {
                throw new IllegalArgumentException("Balanced weights must be finite and not negative.");
            }
            sum += weight;
        }
        if (Math.abs(sum - 1.0) > SUM_TOLERANCE) {
            throw new IllegalArgumentException("Balanced weights must add up to 1.0, but they add up to " + sum);
        }
        this.timeWeight = timeWeight;
        this.costWeight = costWeight;
        this.distanceWeight = distanceWeight;
        this.emissionWeight = emissionWeight;
    }

    @Override
    public String getStrategyName() {
        return "BalancedRouteStrategy";
    }

    @Override
    protected double score(Route route, List<Route> candidates) {
        return timeWeight * normalizedTime(route, candidates)
                + costWeight * normalizedCost(route, candidates)
                + distanceWeight * normalizedDistance(route, candidates)
                + emissionWeight * normalizedEmission(route, candidates);
    }

    @Override
    public List<String> describeScore(Route route, List<Route> candidates) {
        return List.of(
                String.format(Locale.US, "Time:      normalized %.3f x weight %.2f", normalizedTime(route, candidates), timeWeight),
                String.format(Locale.US, "Cost:      normalized %.3f x weight %.2f", normalizedCost(route, candidates), costWeight),
                String.format(Locale.US, "Distance:  normalized %.3f x weight %.2f", normalizedDistance(route, candidates), distanceWeight),
                String.format(Locale.US, "Emissions: normalized %.3f x weight %.2f", normalizedEmission(route, candidates), emissionWeight),
                String.format(Locale.US, "Balanced score: %.3f (lowest = best compromise)", score(route, candidates)));
    }

    private double normalizedTime(Route route, List<Route> candidates) {
        return normalize(route.effectiveMinutes(), candidates, Route::effectiveMinutes);
    }

    private double normalizedCost(Route route, List<Route> candidates) {
        return normalize(route.totalCost(), candidates, Route::totalCost);
    }

    private double normalizedDistance(Route route, List<Route> candidates) {
        return normalize(route.distanceKm(), candidates, Route::distanceKm);
    }

    private double normalizedEmission(Route route, List<Route> candidates) {
        return normalize(route.carbonEmission(), candidates, Route::carbonEmission);
    }

    private static double normalize(double value, List<Route> candidates, ToDoubleFunction<Route> criterion) {
        double min = candidates.stream().mapToDouble(criterion).min().orElse(value);
        double max = candidates.stream().mapToDouble(criterion).max().orElse(value);
        if (max == min) {
            return 0.0;
        }
        return (value - min) / (max - min);
    }
}
