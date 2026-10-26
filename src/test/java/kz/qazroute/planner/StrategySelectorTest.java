package kz.qazroute.planner;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import kz.qazroute.app.SampleData;
import kz.qazroute.model.DeliveryPriority;
import kz.qazroute.model.DeliveryRequest;
import kz.qazroute.model.Route;
import kz.qazroute.strategy.BalancedRouteStrategy;
import kz.qazroute.strategy.CheapestRouteStrategy;
import kz.qazroute.strategy.EcoFriendlyRouteStrategy;
import kz.qazroute.strategy.FastestRouteStrategy;
import kz.qazroute.strategy.RouteStrategy;

/** StrategySelector maps the delivery priority to a strategy. */
class StrategySelectorTest {

    private final StrategySelector selector = new StrategySelector();

    @Test
    @DisplayName("URGENT request -> FastestRouteStrategy")
    void urgentSelectsFastest() {
        RouteStrategy strategy = selector.selectFor(SampleData.request(DeliveryPriority.URGENT));

        assertInstanceOf(FastestRouteStrategy.class, strategy);
    }

    @Test
    @DisplayName("ECONOMY request -> CheapestRouteStrategy")
    void economySelectsCheapest() {
        RouteStrategy strategy = selector.selectFor(SampleData.request(DeliveryPriority.ECONOMY));

        assertInstanceOf(CheapestRouteStrategy.class, strategy);
    }

    @Test
    @DisplayName("ECO request -> EcoFriendlyRouteStrategy")
    void ecoSelectsEcoFriendly() {
        RouteStrategy strategy = selector.selectFor(SampleData.request(DeliveryPriority.ECO));

        assertInstanceOf(EcoFriendlyRouteStrategy.class, strategy);
    }

    @Test
    @DisplayName("STANDARD request -> BalancedRouteStrategy")
    void standardSelectsBalanced() {
        RouteStrategy strategy = selector.selectFor(SampleData.request(DeliveryPriority.STANDARD));

        assertInstanceOf(BalancedRouteStrategy.class, strategy);
    }

    @Test
    @DisplayName("STANDARD + ecoPreferred keeps BalancedRouteStrategy but with eco-leaning weights (R5 instead of R8)")
    void standardWithEcoPreferenceUsesEcoLeaningWeights() {
        DeliveryRequest plain = SampleData.request(DeliveryPriority.STANDARD, false);
        DeliveryRequest ecoPreferred = SampleData.request(DeliveryPriority.STANDARD, true);

        RouteStrategy plainStrategy = selector.selectFor(plain);
        RouteStrategy ecoStrategy = selector.selectFor(ecoPreferred);

        assertInstanceOf(BalancedRouteStrategy.class, ecoStrategy);
        assertEquals("R8", plainStrategy.selectRoute(SampleData.routes(), plain).id());
        assertEquals("R5", ecoStrategy.selectRoute(SampleData.routes(), ecoPreferred).id());
    }

    @Test
    @DisplayName("ecoPreferred does not change the strategy of non-STANDARD priorities")
    void ecoPreferredIgnoredForOtherPriorities() {
        RouteStrategy strategy = selector.selectFor(SampleData.request(DeliveryPriority.URGENT, true));

        assertInstanceOf(FastestRouteStrategy.class, strategy);
    }

    @Test
    @DisplayName("Selector + planner end to end: each priority gets its own winning route")
    void selectorAndPlannerWorkTogether() {
        RoutePlanner planner = new RoutePlanner(new FastestRouteStrategy());
        String[] expectedRouteIds = {"R1", "R3", "R5", "R8"}; // URGENT, ECONOMY, ECO, STANDARD

        DeliveryPriority[] priorities = {DeliveryPriority.URGENT, DeliveryPriority.ECONOMY,
                DeliveryPriority.ECO, DeliveryPriority.STANDARD};
        for (int i = 0; i < priorities.length; i++) {
            DeliveryRequest request = SampleData.request(priorities[i]);
            planner.setStrategy(selector.selectFor(request));

            Route route = planner.planRoute(SampleData.routes(), request);

            assertEquals(expectedRouteIds[i], route.id(), priorities[i].name());
        }
    }

    @Test
    @DisplayName("A null request is rejected")
    void nullRequestIsRejected() {
        assertThrows(IllegalArgumentException.class, () -> selector.selectFor(null));
    }
}
