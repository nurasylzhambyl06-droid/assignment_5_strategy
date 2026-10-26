package kz.qazroute.strategy;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import kz.qazroute.TestRoutes;
import kz.qazroute.app.SampleData;
import kz.qazroute.exception.NoSuitableRouteException;
import kz.qazroute.model.DeliveryPriority;
import kz.qazroute.model.DeliveryRequest;
import kz.qazroute.model.Route;

/** A route that breaks a hard constraint must never win, and bad input must fail loudly. */
class HardConstraintsTest {

    private final List<Route> routes = SampleData.routes();

    private static List<RouteStrategy> allStrategies() {
        return List.of(new FastestRouteStrategy(), new CheapestRouteStrategy(), new EcoFriendlyRouteStrategy(),
                new BalancedRouteStrategy(), new DistanceFirstStrategy());
    }

    @Test
    @DisplayName("Budget constraint: with 70,000 KZT the fastest route changes from R1 to R8")
    void budgetConstraintRemovesTooExpensiveRoutes() {
        DeliveryRequest normal = SampleData.request(DeliveryPriority.URGENT);
        DeliveryRequest smallBudget = new DeliveryRequest(SampleData.ASTANA, SampleData.SEMEY,
                DeliveryPriority.URGENT, 70_000, 900, false);

        assertEquals("R1", new FastestRouteStrategy().selectRoute(routes, normal).id());
        assertEquals("R8", new FastestRouteStrategy().selectRoute(routes, smallBudget).id());
    }

    @Test
    @DisplayName("Deadline constraint: with 840 minutes the cheapest route changes from R3 to R8")
    void deadlineConstraintRemovesTooSlowRoutes() {
        DeliveryRequest normal = SampleData.request(DeliveryPriority.ECONOMY);
        DeliveryRequest shortDeadline = new DeliveryRequest(SampleData.ASTANA, SampleData.SEMEY,
                DeliveryPriority.ECONOMY, 80_000, 840, false);

        assertEquals("R3", new CheapestRouteStrategy().selectRoute(routes, normal).id());
        assertEquals("R8", new CheapestRouteStrategy().selectRoute(routes, shortDeadline).id());
    }

    @Test
    @DisplayName("Deadline is checked against effective time (base + traffic), not base time alone")
    void deadlineUsesEffectiveTime() {
        // base time 100 fits a 120-minute deadline, but 100 + 50 traffic = 150 does not
        Route jammed = TestRoutes.route("A", 100, 100, 1000, 10, 50, true);

        NoSuitableRouteException e = assertThrows(NoSuitableRouteException.class,
                () -> new FastestRouteStrategy().selectRoute(List.of(jammed), TestRoutes.request(1_000_000, 120)));

        assertTrue(e.getMessage().contains("Deadline exceeded (150 min > 120 min)"));
    }

    @Test
    @DisplayName("Routes exactly at the budget and the deadline are still accepted")
    void limitsAreInclusive() {
        Route exact = TestRoutes.route("A", 100, 100, 1000, 10, 20, true); // cost 6000, time 120
        DeliveryRequest request = TestRoutes.request(6000, 120);

        assertEquals("A", new FastestRouteStrategy().selectRoute(List.of(exact), request).id());
    }

    @Test
    @DisplayName("Unavailable route never wins, even though R6 would be the fastest (600 min)")
    void unavailableRouteNeverWins() {
        Route fastest = new FastestRouteStrategy().selectRoute(routes, SampleData.request(DeliveryPriority.URGENT));

        assertFalse(routes.get(5).available());
        assertEquals("R6", routes.get(5).id());
        assertEquals("R1", fastest.id());
    }

    @Test
    @DisplayName("No strategy ever returns R2 (over budget), R4 (too slow) or R6 (unavailable)")
    void violatingRoutesNeverWinWithAnyStrategy() {
        DeliveryRequest request = SampleData.request(DeliveryPriority.STANDARD);

        for (RouteStrategy strategy : allStrategies()) {
            String id = strategy.selectRoute(routes, request).id();
            assertFalse(List.of("R2", "R4", "R6").contains(id), strategy.getStrategyName() + " returned " + id);
        }
    }

    @Test
    @DisplayName("No suitable route: NoSuitableRouteException lists why every route was rejected")
    void noSuitableRouteThrowsMeaningfulException() {
        DeliveryRequest tooCheap = new DeliveryRequest(SampleData.ASTANA, SampleData.SEMEY,
                DeliveryPriority.STANDARD, 10_000, 900, false);

        NoSuitableRouteException e = assertThrows(NoSuitableRouteException.class,
                () -> new CheapestRouteStrategy().selectRoute(routes, tooCheap));

        assertTrue(e.getMessage().contains("No suitable route from Astana to Semey"));
        assertTrue(e.getMessage().contains("Rejected route R1: Budget exceeded"));
        assertTrue(e.getMessage().contains("Rejected route R6: Route is unavailable."));
    }

    @Test
    @DisplayName("Every strategy throws NoSuitableRouteException instead of returning null")
    void everyStrategyThrowsWhenNothingFits() {
        DeliveryRequest impossible = TestRoutes.request(1, 1);

        for (RouteStrategy strategy : allStrategies()) {
            assertThrows(NoSuitableRouteException.class, () -> strategy.selectRoute(routes, impossible),
                    strategy.getStrategyName());
        }
    }

    @Test
    @DisplayName("Only an unavailable route in the list -> NoSuitableRouteException")
    void onlyUnavailableRoute() {
        Route unavailable = TestRoutes.route("A", 100, 60, 1000, 10, 0, false);

        NoSuitableRouteException e = assertThrows(NoSuitableRouteException.class,
                () -> new FastestRouteStrategy().selectRoute(List.of(unavailable), TestRoutes.generousRequest()));

        assertTrue(e.getMessage().contains("Route is unavailable."));
    }

    @Test
    @DisplayName("Empty route list -> NoSuitableRouteException")
    void emptyRouteList() {
        NoSuitableRouteException e = assertThrows(NoSuitableRouteException.class,
                () -> new FastestRouteStrategy().selectRoute(List.of(), TestRoutes.generousRequest()));

        assertTrue(e.getMessage().contains("empty"));
    }

    @Test
    @DisplayName("Missing request or route list -> IllegalArgumentException")
    void missingInputIsRejected() {
        assertThrows(IllegalArgumentException.class, () -> new FastestRouteStrategy().selectRoute(routes, null));
        assertThrows(IllegalArgumentException.class,
                () -> new FastestRouteStrategy().selectRoute(null, TestRoutes.generousRequest()));
    }

    @Test
    @DisplayName("Routes that serve a different city pair are rejected")
    void wrongCityPairIsRejected() {
        DeliveryRequest otherCities = new DeliveryRequest(SampleData.KARAGANDA, SampleData.PAVLODAR,
                DeliveryPriority.URGENT, 80_000, 900, false);

        NoSuitableRouteException e = assertThrows(NoSuitableRouteException.class,
                () -> new FastestRouteStrategy().selectRoute(routes, otherCities));

        assertTrue(e.getMessage().contains("Route connects Astana to Semey, not Karaganda to Pavlodar."));
    }
}
