package kz.qazroute.model;

/**
 * One transportation option between two cities.
 *
 * <p>All values are fictional and illustrative (see README). Units:</p>
 * <ul>
 *   <li>{@code distanceKm} - kilometres</li>
 *   <li>{@code estimatedMinutes} - pure travel time in minutes</li>
 *   <li>{@code baseCost} - fixed fee in KZT (loading, handling, tolls)</li>
 *   <li>{@code carbonEmission} - kilograms of CO2 for the whole trip</li>
 *   <li>{@code trafficPenalty} - extra minutes lost to traffic</li>
 * </ul>
 *
 * <h2>Two derived values used by every strategy</h2>
 * <pre>
 * effectiveMinutes = estimatedMinutes + trafficPenalty
 * totalCost        = baseCost + distanceKm * COST_PER_KM_KZT
 * </pre>
 * Keeping these formulas here means the budget check, the deadline check and the
 * strategies always agree on what "time" and "cost" mean.
 */
public record Route(
        String id,
        String name,
        Location origin,
        Location destination,
        double distanceKm,
        int estimatedMinutes,
        double baseCost,
        double carbonEmission,
        int trafficPenalty,
        boolean available) {

    /** Distance part of the delivery cost, in KZT per kilometre. */
    public static final double COST_PER_KM_KZT = 50.0;

    public Route {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("Route id must not be empty.");
        }
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Route " + id + ": name must not be empty.");
        }
        if (origin == null || destination == null) {
            throw new IllegalArgumentException("Route " + id + ": origin and destination are required.");
        }
        if (origin.equals(destination)) {
            throw new IllegalArgumentException("Route " + id + ": origin and destination must differ.");
        }
        if (!Double.isFinite(distanceKm) || distanceKm <= 0) {
            throw new IllegalArgumentException("Route " + id + ": distance must be positive, got " + distanceKm);
        }
        if (estimatedMinutes <= 0) {
            throw new IllegalArgumentException("Route " + id + ": estimated time must be positive, got " + estimatedMinutes);
        }
        if (!Double.isFinite(baseCost) || baseCost < 0) {
            throw new IllegalArgumentException("Route " + id + ": cost must not be negative, got " + baseCost);
        }
        if (!Double.isFinite(carbonEmission) || carbonEmission < 0) {
            throw new IllegalArgumentException("Route " + id + ": carbon emission must not be negative, got " + carbonEmission);
        }
        if (trafficPenalty < 0) {
            throw new IllegalArgumentException("Route " + id + ": traffic penalty must not be negative, got " + trafficPenalty);
        }
    }

    /** Travel time including traffic: {@code estimatedMinutes + trafficPenalty}. */
    public int effectiveMinutes() {
        return estimatedMinutes + trafficPenalty;
    }

    /** Delivery cost in KZT: {@code baseCost + distanceKm * COST_PER_KM_KZT}. */
    public double totalCost() {
        return baseCost + distanceKm * COST_PER_KM_KZT;
    }
}
