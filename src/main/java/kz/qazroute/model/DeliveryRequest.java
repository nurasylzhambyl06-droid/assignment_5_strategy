package kz.qazroute.model;

/**
 * What the customer asks for. The hard limits ({@code maxBudget}, {@code maxDeliveryMinutes})
 * can never be violated by a selected route.
 *
 * @param origin             city the goods leave from
 * @param destination        city the goods must reach
 * @param priority           decides which strategy {@code StrategySelector} picks
 * @param maxBudget          maximum total cost in KZT
 * @param maxDeliveryMinutes maximum delivery time in minutes (traffic included)
 * @param ecoPreferred       true if the customer would like a greener choice when priority is STANDARD
 */
public record DeliveryRequest(
        Location origin,
        Location destination,
        DeliveryPriority priority,
        double maxBudget,
        int maxDeliveryMinutes,
        boolean ecoPreferred) {

    public DeliveryRequest {
        if (origin == null || destination == null) {
            throw new IllegalArgumentException("Delivery request needs an origin and a destination.");
        }
        if (origin.equals(destination)) {
            throw new IllegalArgumentException("Delivery origin and destination must differ.");
        }
        if (priority == null) {
            throw new IllegalArgumentException("Delivery priority is required.");
        }
        if (!Double.isFinite(maxBudget) || maxBudget < 0) {
            throw new IllegalArgumentException("Maximum budget must not be negative, got " + maxBudget);
        }
        if (maxDeliveryMinutes <= 0) {
            throw new IllegalArgumentException("Maximum delivery time must be positive, got " + maxDeliveryMinutes);
        }
    }
}
