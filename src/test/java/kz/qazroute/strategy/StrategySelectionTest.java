package kz.qazroute.strategy;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import kz.qazroute.TestRoutes;
import kz.qazroute.app.SampleData;
import kz.qazroute.model.DeliveryPriority;
import kz.qazroute.model.DeliveryRequest;
import kz.qazroute.model.Route;

/** Each strategy picks the expected route from the sample data (and from tiny custom lists). */
class StrategySelectionTest {

    private final List<Route> routes = SampleData.routes();
    private final DeliveryRequest request = SampleData.request(DeliveryPriority.STANDARD);

    @Test
    @DisplayName("Fastest strategy selects the lowest effective time (R1, 820 min)")
    void fastestSelectsLowestEffectiveTime() {
        Route selected = new FastestRouteStrategy().selectRoute(routes, request);

        assertEquals("R1", selected.id());
        assertEquals(820, selected.effectiveMinutes());
    }

    @Test
    @DisplayName("Fastest strategy counts the traffic penalty, not only the base time")
    void fastestIncludesTrafficPenalty() {
        Route lowBaseButJammed = TestRoutes.route("A", 100, 100, 1000, 10, 50, true); // 100 + 50 = 150
        Route higherBaseNoTraffic = TestRoutes.route("B", 100, 120, 1000, 10, 0, true); // 120

        Route selected = new FastestRouteStrategy()
                .selectRoute(List.of(lowBaseButJammed, higherBaseNoTraffic), TestRoutes.generousRequest());

        assertEquals("B", selected.id());
    }

    @Test
    @DisplayName("Cheapest strategy selects the lowest total cost (R3, 62,000 KZT)")
    void cheapestSelectsLowestTotalCost() {
        Route selected = new CheapestRouteStrategy().selectRoute(routes, request);

        assertEquals("R3", selected.id());
        assertEquals(62_000.0, selected.totalCost(), 1e-9);
    }

    @Test
    @DisplayName("Cheapest strategy uses base cost + distance cost, not base cost alone")
    void cheapestUsesBaseCostPlusDistanceCost() {
        Route lowBaseLongDistance = TestRoutes.route("A", 100, 60, 1000, 10, 0, true); // 1000 + 100*50 = 6000
        Route highBaseShortDistance = TestRoutes.route("B", 10, 60, 4000, 10, 0, true); // 4000 + 10*50 = 4500

        assertEquals(6000.0, lowBaseLongDistance.totalCost(), 1e-9);
        Route selected = new CheapestRouteStrategy()
                .selectRoute(List.of(lowBaseLongDistance, highBaseShortDistance), TestRoutes.generousRequest());

        assertEquals("B", selected.id());
    }

    @Test
    @DisplayName("Eco strategy selects the lowest eco score (R5, score 346.0)")
    void ecoSelectsLowestEcoScore() {
        Route selected = new EcoFriendlyRouteStrategy().selectRoute(routes, request);

        assertEquals("R5", selected.id());
    }

    @Test
    @DisplayName("Eco score combines CO2 and distance: 0.7 * CO2 + 0.3 * km")
    void ecoScoreCombinesEmissionAndDistance() {
        Route lowCo2LongDistance = TestRoutes.route("A", 300, 60, 1000, 90, 0, true);  // 63 + 90 = 153
        Route highCo2ShortDistance = TestRoutes.route("B", 100, 60, 1000, 100, 0, true); // 70 + 30 = 100
        List<Route> both = List.of(lowCo2LongDistance, highCo2ShortDistance);

        Route withDefaultWeights = new EcoFriendlyRouteStrategy().selectRoute(both, TestRoutes.generousRequest());
        Route onlyCo2Matters = new EcoFriendlyRouteStrategy(1.0, 0.0).selectRoute(both, TestRoutes.generousRequest());

        assertEquals("B", withDefaultWeights.id());
        assertEquals("A", onlyCo2Matters.id());
    }

    @Test
    @DisplayName("Balanced strategy selects the best compromise (R8)")
    void balancedSelectsBestCompromise() {
        Route selected = new BalancedRouteStrategy().selectRoute(routes, request);

        assertEquals("R8", selected.id());
    }

    @Test
    @DisplayName("Balanced weights decide the winner: all weight on time = fastest, all on cost = cheapest")
    void balancedFollowsItsWeights() {
        Route onlyTime = new BalancedRouteStrategy(1.0, 0.0, 0.0, 0.0).selectRoute(routes, request);
        Route onlyCost = new BalancedRouteStrategy(0.0, 1.0, 0.0, 0.0).selectRoute(routes, request);

        assertEquals("R1", onlyTime.id());
        assertEquals("R3", onlyCost.id());
    }

    @Test
    @DisplayName("Balanced strategy copes with routes that have identical values (no division by zero)")
    void balancedHandlesIdenticalRoutes() {
        Route first = TestRoutes.route("A1", 100, 60, 1000, 10, 0, true);
        Route second = TestRoutes.route("B2", 100, 60, 1000, 10, 0, true);

        Route selected = new BalancedRouteStrategy().selectRoute(List.of(second, first), TestRoutes.generousRequest());

        assertEquals("A1", selected.id());
    }

    @Test
    @DisplayName("Equal scores are resolved by the smaller route id, so results are deterministic")
    void tieIsBrokenByRouteId() {
        Route second = TestRoutes.route("B2", 100, 60, 1000, 10, 0, true);
        Route first = TestRoutes.route("A1", 100, 60, 1000, 10, 0, true);

        Route selected = new CheapestRouteStrategy().selectRoute(List.of(second, first), TestRoutes.generousRequest());

        assertEquals("A1", selected.id());
    }

    @Test
    @DisplayName("Fifth strategy: DistanceFirstStrategy selects the shortest route (R7, 960 km)")
    void distanceFirstSelectsShortestDistance() {
        Route selected = new DistanceFirstStrategy().selectRoute(routes, request);

        assertEquals("R7", selected.id());
        assertEquals(960.0, selected.distanceKm(), 1e-9);
    }

    @Test
    @DisplayName("Fifth strategy still respects hard constraints: an unavailable shortest route is skipped")
    void distanceFirstSkipsUnavailableRoute() {
        Route shortestButUnavailable = TestRoutes.route("A", 100, 60, 1000, 10, 0, false);
        Route longerAndAvailable = TestRoutes.route("B", 200, 60, 1000, 10, 0, true);

        Route selected = new DistanceFirstStrategy()
                .selectRoute(List.of(shortestButUnavailable, longerAndAvailable), TestRoutes.generousRequest());

        assertEquals("B", selected.id());
    }

    @Test
    @DisplayName("Each strategy has a distinct, readable name")
    void strategyNames() {
        assertEquals("FastestRouteStrategy", new FastestRouteStrategy().getStrategyName());
        assertEquals("CheapestRouteStrategy", new CheapestRouteStrategy().getStrategyName());
        assertEquals("EcoFriendlyRouteStrategy", new EcoFriendlyRouteStrategy().getStrategyName());
        assertEquals("BalancedRouteStrategy", new BalancedRouteStrategy().getStrategyName());
        assertEquals("DistanceFirstStrategy", new DistanceFirstStrategy().getStrategyName());
    }
}
