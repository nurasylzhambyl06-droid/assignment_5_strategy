package kz.qazroute.planner;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import kz.qazroute.app.SampleData;
import kz.qazroute.model.DeliveryPriority;
import kz.qazroute.model.DeliveryRequest;
import kz.qazroute.model.Route;
import kz.qazroute.strategy.CheapestRouteStrategy;
import kz.qazroute.strategy.DistanceFirstStrategy;
import kz.qazroute.strategy.EcoFriendlyRouteStrategy;
import kz.qazroute.strategy.FastestRouteStrategy;
import kz.qazroute.strategy.RouteStrategy;

/** The planner (Context) only delegates; the same object works with any RouteStrategy. */
class RoutePlannerTest {

    private final List<Route> routes = SampleData.routes();
    private final DeliveryRequest request = SampleData.request(DeliveryPriority.STANDARD);

    /** A strategy defined inside the test: proves the planner depends only on the interface. */
    private static class FirstRouteStrategy implements RouteStrategy {
        @Override
        public Route selectRoute(List<Route> routes, DeliveryRequest request) {
            return routes.get(0);
        }

        @Override
        public String getStrategyName() {
            return "FirstRouteStrategy";
        }

        @Override
        public List<String> describeScore(Route route, List<Route> candidates) {
            return List.of("First in the list");
        }
    }

    @Test
    @DisplayName("Runtime switching: the same planner returns R1, then R3, then R5")
    void runtimeStrategySwitching() {
        RoutePlanner planner = new RoutePlanner(new FastestRouteStrategy());
        assertEquals("R1", planner.planRoute(routes, request).id());

        planner.setStrategy(new CheapestRouteStrategy());
        assertEquals("R3", planner.planRoute(routes, request).id());

        planner.setStrategy(new EcoFriendlyRouteStrategy());
        assertEquals("R5", planner.planRoute(routes, request).id());
        assertEquals("EcoFriendlyRouteStrategy", planner.getStrategy().getStrategyName());
    }

    @Test
    @DisplayName("Fifth strategy plugs into the unchanged planner and selects R7")
    void fifthStrategyWorksWithUnchangedPlanner() {
        RoutePlanner planner = new RoutePlanner(new FastestRouteStrategy());

        planner.setStrategy(new DistanceFirstStrategy());

        assertEquals("R7", planner.planRoute(routes, request).id());
    }

    @Test
    @DisplayName("Planner delegates to any RouteStrategy implementation, even one written in a test")
    void plannerDelegatesToTheStrategy() {
        RouteStrategy firstRoute = new FirstRouteStrategy();
        RoutePlanner planner = new RoutePlanner(firstRoute);

        assertSame(firstRoute, planner.getStrategy());
        assertEquals("R1", planner.planRoute(routes, request).id());
    }

    @Test
    @DisplayName("Planner rejects a null strategy in the constructor and in setStrategy")
    void nullStrategyIsRejected() {
        assertThrows(IllegalArgumentException.class, () -> new RoutePlanner(null));

        RoutePlanner planner = new RoutePlanner(new FastestRouteStrategy());
        assertThrows(IllegalArgumentException.class, () -> planner.setStrategy(null));
        assertEquals("FastestRouteStrategy", planner.getStrategy().getStrategyName());
    }
}
