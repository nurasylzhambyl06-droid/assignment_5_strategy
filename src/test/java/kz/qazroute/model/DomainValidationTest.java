package kz.qazroute.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import kz.qazroute.TestRoutes;
import kz.qazroute.app.SampleData;

/** Invalid domain data is rejected as soon as the object is created. */
class DomainValidationTest {

    private static Route routeWith(double distanceKm, int minutes, double baseCost, double carbon, int traffic) {
        return new Route("X", "Test route", SampleData.ASTANA, SampleData.SEMEY,
                distanceKm, minutes, baseCost, carbon, traffic, true);
    }

    @Test
    @DisplayName("Negative distance is rejected")
    void negativeDistanceIsRejected() {
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> routeWith(-1, 100, 1000, 10, 0));

        assertTrue(e.getMessage().contains("distance must be positive"));
    }

    @Test
    @DisplayName("Negative cost is rejected")
    void negativeCostIsRejected() {
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> routeWith(100, 100, -1, 10, 0));

        assertTrue(e.getMessage().contains("cost must not be negative"));
    }

    @Test
    @DisplayName("Negative delivery time and negative traffic penalty are rejected")
    void negativeTimeIsRejected() {
        assertThrows(IllegalArgumentException.class, () -> routeWith(100, -5, 1000, 10, 0));
        assertThrows(IllegalArgumentException.class, () -> routeWith(100, 100, 1000, 10, -5));
    }

    @Test
    @DisplayName("Negative carbon emission is rejected")
    void negativeEmissionIsRejected() {
        assertThrows(IllegalArgumentException.class, () -> routeWith(100, 100, 1000, -10, 0));
    }

    @Test
    @DisplayName("A route needs two different cities")
    void routeNeedsDifferentCities() {
        assertThrows(IllegalArgumentException.class, () -> new Route("X", "Loop", SampleData.ASTANA,
                SampleData.ASTANA, 100, 100, 1000, 10, 0, true));
        assertThrows(IllegalArgumentException.class, () -> new Route("X", "No origin", null,
                SampleData.SEMEY, 100, 100, 1000, 10, 0, true));
    }

    @Test
    @DisplayName("Route formulas: effective time = base + traffic, total cost = base cost + km * 50")
    void routeDerivedValues() {
        Route route = TestRoutes.route("A", 200, 100, 3000, 10, 25, true);

        assertEquals(125, route.effectiveMinutes());
        assertEquals(13_000.0, route.totalCost(), 1e-9);
    }

    @Test
    @DisplayName("Invalid delivery requests are rejected (budget, deadline, priority, cities)")
    void invalidDeliveryRequestsAreRejected() {
        assertThrows(IllegalArgumentException.class, () -> new DeliveryRequest(SampleData.ASTANA,
                SampleData.SEMEY, DeliveryPriority.URGENT, -1, 900, false));
        assertThrows(IllegalArgumentException.class, () -> new DeliveryRequest(SampleData.ASTANA,
                SampleData.SEMEY, DeliveryPriority.URGENT, 1000, 0, false));
        assertThrows(IllegalArgumentException.class, () -> new DeliveryRequest(SampleData.ASTANA,
                SampleData.SEMEY, null, 1000, 900, false));
        assertThrows(IllegalArgumentException.class, () -> new DeliveryRequest(SampleData.ASTANA,
                SampleData.ASTANA, DeliveryPriority.URGENT, 1000, 900, false));
        assertThrows(IllegalArgumentException.class, () -> new DeliveryRequest(null,
                SampleData.SEMEY, DeliveryPriority.URGENT, 1000, 900, false));
    }

    @Test
    @DisplayName("Invalid locations are rejected (empty name, latitude out of range)")
    void invalidLocationsAreRejected() {
        assertThrows(IllegalArgumentException.class, () -> new Location(" ", 50, 70));
        assertThrows(IllegalArgumentException.class, () -> new Location("Nowhere", 123, 70));
        assertThrows(IllegalArgumentException.class, () -> new Location("Nowhere", 50, 200));
    }
}
