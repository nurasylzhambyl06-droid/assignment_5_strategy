package kz.qazroute.app;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import kz.qazroute.exception.NoSuitableRouteException;
import kz.qazroute.model.DeliveryPriority;
import kz.qazroute.model.DeliveryRequest;
import kz.qazroute.model.Route;
import kz.qazroute.strategy.BalancedRouteStrategy;
import kz.qazroute.strategy.CheapestRouteStrategy;
import kz.qazroute.strategy.EcoFriendlyRouteStrategy;
import kz.qazroute.strategy.FastestRouteStrategy;
import kz.qazroute.strategy.RouteStrategy;

/** Explanations and comparison mode (console output is built as text, so it can be tested). */
class ExplanationAndComparisonTest {

    private final List<Route> routes = SampleData.routes();
    private final DeliveryRequest request = SampleData.request(DeliveryPriority.STANDARD);

    /** A strategy that always fails: used to check that comparison mode keeps going. */
    private static class AlwaysFailingStrategy implements RouteStrategy {
        @Override
        public Route selectRoute(List<Route> routes, DeliveryRequest request) {
            throw new NoSuitableRouteException("this strategy always fails");
        }

        @Override
        public String getStrategyName() {
            return "AlwaysFailingStrategy";
        }

        @Override
        public List<String> describeScore(Route route, List<Route> candidates) {
            return List.of();
        }
    }

    @Test
    @DisplayName("Explanation of the fastest route shows base time, traffic penalty, effective time and rejections")
    void fastestExplanationShowsTimesAndRejections() {
        String text = RouteExplainer.explain(new FastestRouteStrategy(), routes, request);

        assertTrue(text.contains("Selected route: R1 (Direct Highway via Pavlodar)"));
        assertTrue(text.contains("Strategy: FastestRouteStrategy"));
        assertTrue(text.contains("Base estimated time: 780 minutes"));
        assertTrue(text.contains("Traffic penalty: 40 minutes"));
        assertTrue(text.contains("Effective time: 820 minutes"));
        assertTrue(text.contains("Rejected route R2:\n  Budget exceeded"));
        assertTrue(text.contains("Rejected route R4:\n  Deadline exceeded"));
        assertTrue(text.contains("Rejected route R6:\n  Route is unavailable."));
    }

    @Test
    @DisplayName("Explanation of the cheapest route shows the cost formula with real numbers")
    void cheapestExplanationShowsCostFormula() {
        String text = RouteExplainer.explain(new CheapestRouteStrategy(), routes, request);

        assertTrue(text.contains("Selected route: R3"));
        assertTrue(text.contains("Base cost: 6000 KZT"));
        assertTrue(text.contains("Distance cost: 1120 km x 50 KZT/km = 56000 KZT"));
        assertTrue(text.contains("Total cost: 62000 KZT"));
    }

    @Test
    @DisplayName("Explanation of the balanced route shows the four normalized criteria and the final score")
    void balancedExplanationShowsNormalizedValues() {
        String text = RouteExplainer.explain(new BalancedRouteStrategy(), routes, request);

        assertTrue(text.contains("Selected route: R8"));
        assertTrue(text.contains("Time:      normalized 0.200 x weight 0.40"));
        assertTrue(text.contains("Balanced score: 0.317"));
    }

    @Test
    @DisplayName("Comparison mode: every strategy shows its own route, time, cost, distance and CO2")
    void comparisonShowsEachStrategyResult() {
        List<RouteStrategy> four = List.of(new FastestRouteStrategy(), new CheapestRouteStrategy(),
                new EcoFriendlyRouteStrategy(), new BalancedRouteStrategy());

        String table = StrategyComparison.compare(four, routes, request);

        assertTrue(table.contains("FastestRouteStrategy     | R1 Direct Highway via Pavlodar"));
        assertTrue(table.contains("CheapestRouteStrategy    | R3 Rail Freight via Karaganda"));
        assertTrue(table.contains("EcoFriendlyRouteStrategy | R5 Electric Truck via Karaganda"));
        assertTrue(table.contains("BalancedRouteStrategy    | R8 Mixed Rail-Road via Karaganda"));
        assertTrue(table.contains("74,000"));
        assertTrue(table.contains("1,120"));
        assertFalse(table.contains("NO SUITABLE ROUTE"));
    }

    @Test
    @DisplayName("Comparison mode: a failing strategy is reported and the other strategies still run")
    void comparisonSurvivesAFailingStrategy() {
        List<RouteStrategy> strategies = List.of(new FastestRouteStrategy(), new AlwaysFailingStrategy(),
                new CheapestRouteStrategy());

        String table = StrategyComparison.compare(strategies, routes, request);

        assertTrue(table.contains("R1 Direct Highway via Pavlodar"));
        assertTrue(table.contains("R3 Rail Freight via Karaganda"));
        assertTrue(table.contains("AlwaysFailingStrategy"));
        assertTrue(table.contains("NO SUITABLE ROUTE"));
        assertTrue(table.contains("Failure -> AlwaysFailingStrategy: this strategy always fails"));
    }

    @Test
    @DisplayName("Comparison mode with an impossible request reports a failure for every strategy")
    void comparisonWithImpossibleRequest() {
        DeliveryRequest impossible = new DeliveryRequest(SampleData.ASTANA, SampleData.SEMEY,
                DeliveryPriority.STANDARD, 1_000, 900, false);

        String table = StrategyComparison.compare(List.of(new FastestRouteStrategy(), new BalancedRouteStrategy()),
                routes, impossible);

        assertEquals(2, table.split("NO SUITABLE ROUTE", -1).length - 1);
        assertTrue(table.contains("Failure -> FastestRouteStrategy: No suitable route from Astana to Semey"));
    }

    @Test
    @DisplayName("Explaining when nothing fits throws NoSuitableRouteException (nothing is hidden)")
    void explanationPropagatesNoSuitableRoute() {
        DeliveryRequest impossible = new DeliveryRequest(SampleData.ASTANA, SampleData.SEMEY,
                DeliveryPriority.STANDARD, 1_000, 900, false);

        assertThrows(NoSuitableRouteException.class,
                () -> RouteExplainer.explain(new FastestRouteStrategy(), routes, impossible));
    }
}
