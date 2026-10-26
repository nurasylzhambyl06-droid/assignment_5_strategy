package kz.qazroute.app;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import kz.qazroute.exception.NoSuitableRouteException;
import kz.qazroute.model.DeliveryPriority;
import kz.qazroute.model.DeliveryRequest;
import kz.qazroute.model.Route;
import kz.qazroute.planner.RoutePlanner;
import kz.qazroute.planner.StrategySelector;
import kz.qazroute.strategy.BalancedRouteStrategy;
import kz.qazroute.strategy.CheapestRouteStrategy;
import kz.qazroute.strategy.DistanceFirstStrategy;
import kz.qazroute.strategy.EcoFriendlyRouteStrategy;
import kz.qazroute.strategy.FastestRouteStrategy;
import kz.qazroute.strategy.RouteStrategy;

/**
 * QazRoute - Regional Logistics Optimizer: console demonstration of the Strategy Pattern.
 */
public class QazRouteApp {

    private static final List<Route> ROUTES = SampleData.routes();

    public static void main(String[] args) {
        printBanner();
        printRoutes();
        runtimeSwitchingDemo();
        automaticSelectionDemo();
        comparisonDemo();
        fifthStrategyDemo();
        failureScenariosDemo();
    }

    // ---------------------------------------------------------------- demos

    private static void runtimeSwitchingDemo() {
        printHeader("DEMO 1: RUNTIME STRATEGY SWITCHING (one RoutePlanner object, three strategies)");
        DeliveryRequest request = SampleData.request(DeliveryPriority.STANDARD);
        printRequest(request);

        RoutePlanner planner = new RoutePlanner(new FastestRouteStrategy());
        showPlannerResult(planner, request);

        planner.setStrategy(new CheapestRouteStrategy());
        showPlannerResult(planner, request);

        planner.setStrategy(new EcoFriendlyRouteStrategy());
        showPlannerResult(planner, request);
    }

    private static void automaticSelectionDemo() {
        printHeader("DEMO 2: AUTOMATIC STRATEGY SELECTION (StrategySelector)");
        StrategySelector selector = new StrategySelector();
        RoutePlanner planner = new RoutePlanner(new FastestRouteStrategy());

        for (DeliveryPriority priority : DeliveryPriority.values()) {
            showAutomaticChoice(selector, planner, SampleData.request(priority), priority.name());
        }
        showAutomaticChoice(selector, planner,
                SampleData.request(DeliveryPriority.STANDARD, true), "STANDARD + ecoPreferred");
    }

    private static void comparisonDemo() {
        printHeader("DEMO 3: STRATEGY COMPARISON MODE (same request, four strategies)");
        DeliveryRequest request = SampleData.request(DeliveryPriority.STANDARD);
        printRequest(request);
        System.out.println(StrategyComparison.compare(requiredStrategies(), ROUTES, request));

        System.out.println();
        System.out.println("Same comparison with a budget of only 10,000 KZT (failures are shown, nothing crashes):");
        DeliveryRequest tooCheap = new DeliveryRequest(SampleData.ASTANA, SampleData.SEMEY,
                DeliveryPriority.STANDARD, 10_000, SampleData.DEMO_DEADLINE_MINUTES, false);
        System.out.println(StrategyComparison.compare(requiredStrategies(), ROUTES, tooCheap));
    }

    private static void fifthStrategyDemo() {
        printHeader("DEMO 4: FIFTH STRATEGY - DistanceFirstStrategy (RoutePlanner was not changed)");
        DeliveryRequest request = SampleData.request(DeliveryPriority.STANDARD);

        RoutePlanner planner = new RoutePlanner(new FastestRouteStrategy());
        planner.setStrategy(new DistanceFirstStrategy());
        showPlannerResult(planner, request);

        List<RouteStrategy> allFive = new ArrayList<>(requiredStrategies());
        allFive.add(new DistanceFirstStrategy());
        System.out.println("Comparison with all five strategies:");
        System.out.println(StrategyComparison.compare(allFive, ROUTES, request));
    }

    private static void failureScenariosDemo() {
        printHeader("DEMO 5: FAILURE SCENARIOS (every error is reported, none is hidden)");
        RoutePlanner planner = new RoutePlanner(new FastestRouteStrategy());
        DeliveryRequest request = SampleData.request(DeliveryPriority.URGENT);
        Route unavailable = ROUTES.get(5); // R6 Night Shuttle

        expectFailure("1. Empty route list", () -> planner.planRoute(List.of(), request));
        expectFailure("2. No suitable route (budget of only 10,000 KZT)", () -> planner.planRoute(ROUTES,
                new DeliveryRequest(SampleData.ASTANA, SampleData.SEMEY, DeliveryPriority.URGENT, 10_000, 900, false)));
        expectFailure("3. Invalid balanced weights (sum is 2.0)", () -> new BalancedRouteStrategy(0.5, 0.5, 0.5, 0.5));
        expectFailure("3b. Invalid balanced weights (negative weight)", () -> new BalancedRouteStrategy(-0.1, 0.5, 0.3, 0.3));
        expectFailure("4. Only an unavailable route", () -> planner.planRoute(List.of(unavailable), request));
        expectFailure("5. Negative distance", () -> new Route("X1", "Broken route", SampleData.ASTANA,
                SampleData.SEMEY, -50, 100, 1000, 10, 0, true));
        expectFailure("5b. Negative cost", () -> new Route("X2", "Broken route", SampleData.ASTANA,
                SampleData.SEMEY, 50, 100, -1000, 10, 0, true));
        expectFailure("6. Missing (null) delivery request", () -> planner.planRoute(ROUTES, null));
        expectFailure("6b. Invalid request (negative budget)", () -> new DeliveryRequest(SampleData.ASTANA,
                SampleData.SEMEY, DeliveryPriority.URGENT, -5, 900, false));
        expectFailure("7. Request for a city pair that no route serves (Kostanay -> Kokshetau)", () -> planner.planRoute(ROUTES,
                new DeliveryRequest(SampleData.KOSTANAY, SampleData.KOKSHETAU, DeliveryPriority.URGENT, 80_000, 900, false)));
    }

    // -------------------------------------------------------------- helpers

    private static List<RouteStrategy> requiredStrategies() {
        return List.of(new FastestRouteStrategy(), new CheapestRouteStrategy(),
                new EcoFriendlyRouteStrategy(), new BalancedRouteStrategy());
    }

    private static void showPlannerResult(RoutePlanner planner, DeliveryRequest request) {
        System.out.println();
        System.out.println(">>> RoutePlanner now uses: " + planner.getStrategy().getStrategyName());
        System.out.println(RouteExplainer.explain(planner.getStrategy(), ROUTES, request));
        Route route = planner.planRoute(ROUTES, request);
        System.out.println("Result: " + route.id() + " " + route.name());
    }

    private static void showAutomaticChoice(StrategySelector selector, RoutePlanner planner,
                                            DeliveryRequest request, String label) {
        planner.setStrategy(selector.selectFor(request));
        Route route = planner.planRoute(ROUTES, request);
        System.out.printf("%-24s -> %-24s -> %s %s%n",
                label, planner.getStrategy().getStrategyName(), route.id(), route.name());
    }

    private static void expectFailure(String label, Runnable action) {
        try {
            action.run();
            System.out.println(label + " -> UNEXPECTED: no exception was thrown");
        } catch (NoSuitableRouteException | IllegalArgumentException e) {
            System.out.println(label);
            System.out.println("    " + e.getClass().getSimpleName() + ": " + e.getMessage().replace("\n", "\n    "));
        }
    }

    private static void printBanner() {
        System.out.println("=".repeat(80));
        System.out.println(" QazRoute - Regional Logistics Optimizer");
        System.out.println(" Smart Delivery & Route Optimization Engine (Strategy Pattern demo)");
        System.out.println("=".repeat(80));
        System.out.println("Note: all routes and metrics are fictional demo data, not real-time logistics data.");
    }

    private static void printRoutes() {
        printHeader("SAMPLE ROUTES (candidate routes, Astana -> Semey)");
        System.out.printf(Locale.US, "%-3s %-31s %-9s %9s %11s %9s %8s%n",
                "Id", "Name", "Available", "Time(min)", "Cost(KZT)", "Dist(km)", "CO2(kg)");
        for (Route route : ROUTES) {
            System.out.printf(Locale.US, "%-3s %-31s %-9s %9d %,11.0f %,9.0f %8.0f%n",
                    route.id(), route.name(), route.available() ? "yes" : "NO",
                    route.effectiveMinutes(), route.totalCost(), route.distanceKm(), route.carbonEmission());
        }
        System.out.println("Time = travel time + traffic penalty. Cost = base cost + distance x "
                + (int) Route.COST_PER_KM_KZT + " KZT/km.");
    }

    private static void printRequest(DeliveryRequest request) {
        System.out.printf(Locale.US, "Request: %s -> %s | priority %s | budget %,.0f KZT | deadline %d min | eco preferred: %s%n",
                request.origin(), request.destination(), request.priority(), request.maxBudget(),
                request.maxDeliveryMinutes(), request.ecoPreferred() ? "yes" : "no");
    }

    private static void printHeader(String title) {
        System.out.println();
        System.out.println("=".repeat(80));
        System.out.println(title);
        System.out.println("=".repeat(80));
    }
}
