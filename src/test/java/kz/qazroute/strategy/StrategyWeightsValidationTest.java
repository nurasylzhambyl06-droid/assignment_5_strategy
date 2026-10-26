package kz.qazroute.strategy;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** Invalid weights must be rejected when the strategy is created, not later. */
class StrategyWeightsValidationTest {

    @Test
    @DisplayName("Balanced weights that add up to 2.0 are rejected")
    void balancedWeightsMustSumToOne() {
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> new BalancedRouteStrategy(0.5, 0.5, 0.5, 0.5));

        assertTrue(e.getMessage().contains("add up to 1.0"));
    }

    @Test
    @DisplayName("Balanced weights that add up to less than 1.0 are rejected")
    void balancedWeightsBelowOneAreRejected() {
        assertThrows(IllegalArgumentException.class, () -> new BalancedRouteStrategy(0.1, 0.1, 0.1, 0.1));
    }

    @Test
    @DisplayName("A negative balanced weight is rejected, even if the sum is 1.0")
    void negativeBalancedWeightIsRejected() {
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> new BalancedRouteStrategy(-0.2, 0.6, 0.3, 0.3));

        assertTrue(e.getMessage().contains("not negative"));
    }

    @Test
    @DisplayName("NaN balanced weights are rejected")
    void nanBalancedWeightIsRejected() {
        assertThrows(IllegalArgumentException.class,
                () -> new BalancedRouteStrategy(Double.NaN, 0.4, 0.3, 0.3));
    }

    @Test
    @DisplayName("Valid balanced weights are accepted (0.40 / 0.30 / 0.10 / 0.20)")
    void validBalancedWeightsAreAccepted() {
        BalancedRouteStrategy strategy = new BalancedRouteStrategy(0.40, 0.30, 0.10, 0.20);

        assertEquals("BalancedRouteStrategy", strategy.getStrategyName());
    }

    @Test
    @DisplayName("Negative or all-zero eco weights are rejected")
    void invalidEcoWeightsAreRejected() {
        assertThrows(IllegalArgumentException.class, () -> new EcoFriendlyRouteStrategy(-1.0, 0.5));
        assertThrows(IllegalArgumentException.class, () -> new EcoFriendlyRouteStrategy(0.0, 0.0));
    }
}
