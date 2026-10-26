package kz.qazroute.app;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import kz.qazroute.exception.NoSuitableRouteException;
import kz.qazroute.model.DeliveryRequest;
import kz.qazroute.model.Route;
import kz.qazroute.strategy.RouteStrategy;

/**
 * Comparison mode: every strategy independently solves the SAME request and the results are
 * shown side by side. A strategy that finds no route is shown as a failure; the others still run.
 */
public final class StrategyComparison {

    private static final String ROW_FORMAT = "%-24s | %-33s | %10s | %11s | %13s | %8s%n";

    private StrategyComparison() {
        // utility class
    }

    public static String compare(List<RouteStrategy> strategies, List<Route> routes, DeliveryRequest request) {
        StringBuilder table = new StringBuilder();
        table.append(String.format(Locale.US, ROW_FORMAT,
                "Strategy", "Selected route", "Time (min)", "Cost (KZT)", "Distance (km)", "CO2 (kg)"));
        table.append("-".repeat(114)).append('\n');

        List<String> failures = new ArrayList<>();
        for (RouteStrategy strategy : strategies) {
            try {
                Route route = strategy.selectRoute(routes, request);
                table.append(String.format(Locale.US, ROW_FORMAT,
                        strategy.getStrategyName(),
                        route.id() + " " + route.name(),
                        route.effectiveMinutes(),
                        String.format(Locale.US, "%,.0f", route.totalCost()),
                        String.format(Locale.US, "%,.0f", route.distanceKm()),
                        String.format(Locale.US, "%.0f", route.carbonEmission())));
            } catch (NoSuitableRouteException e) {
                table.append(String.format(Locale.US, ROW_FORMAT,
                        strategy.getStrategyName(), "NO SUITABLE ROUTE", "-", "-", "-", "-"));
                failures.add(strategy.getStrategyName() + ": " + firstLine(e.getMessage()));
            }
        }

        for (String failure : failures) {
            table.append("Failure -> ").append(failure).append('\n');
        }
        return table.toString().stripTrailing();
    }

    /** Exception messages can list every rejected route; the table only needs the headline. */
    private static String firstLine(String message) {
        int end = message.indexOf('\n');
        return end < 0 ? message : message.substring(0, end);
    }
}
