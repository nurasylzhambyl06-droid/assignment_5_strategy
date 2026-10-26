package kz.qazroute.model;

/**
 * What the customer cares about most. {@code StrategySelector} maps each value to one strategy.
 */
public enum DeliveryPriority {
    /** Deliver as fast as possible. */
    URGENT,
    /** Deliver as cheaply as possible. */
    ECONOMY,
    /** Deliver with the lowest environmental impact. */
    ECO,
    /** No single focus: balance time, cost, distance and emissions. */
    STANDARD
}
