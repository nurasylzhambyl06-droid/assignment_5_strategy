package kz.qazroute.strategy;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import kz.qazroute.exception.NoSuitableRouteException;
import kz.qazroute.model.DeliveryRequest;
import kz.qazroute.model.Route;

/**
 * The hard constraints, written once and shared by every strategy.
 *
 * <p>A route that breaks any rule below is removed BEFORE scoring, so it can never win,
 * no matter how good its score would be.</p>
 *
 * <ol>
 *   <li>The route must connect the requested origin and destination.</li>
 *   <li>The route must be available.</li>
 *   <li>{@code totalCost} must not exceed the request budget.</li>
 *   <li>{@code effectiveMinutes} (travel time + traffic penalty) must not exceed the deadline.</li>
 * </ol>
 */
public final class RouteConstraints {

    private RouteConstraints() {
        // utility class: static methods only
    }

    /** Returns the reason why the route is rejected, or empty if the route is acceptable. */
    public static Optional<String> findViolation(Route route, DeliveryRequest request) {
        if (!route.origin().equals(request.origin()) || !route.destination().equals(request.destination())) {
            return Optional.of("Route connects " + route.origin() + " to " + route.destination()
                    + ", not " + request.origin() + " to " + request.destination() + ".");
        }
        if (!route.available()) {
            return Optional.of("Route is unavailable.");
        }
        if (route.totalCost() > request.maxBudget()) {
            return Optional.of(String.format(Locale.US, "Budget exceeded (%.0f KZT > %.0f KZT).",
                    route.totalCost(), request.maxBudget()));
        }
        if (route.effectiveMinutes() > request.maxDeliveryMinutes()) {
            return Optional.of(String.format(Locale.US, "Deadline exceeded (%d min > %d min).",
                    route.effectiveMinutes(), request.maxDeliveryMinutes()));
        }
        return Optional.empty();
    }

    /**
     * Validates the input and keeps only routes that satisfy every hard constraint.
     *
     * @throws IllegalArgumentException   if the request, the list or one of its routes is null
     * @throws NoSuitableRouteException   if the list is empty or no route is acceptable
     */
    public static List<Route> filterFeasible(List<Route> routes, DeliveryRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Delivery request must not be null.");
        }
        if (routes == null) {
            throw new IllegalArgumentException("Route list must not be null.");
        }
        if (routes.isEmpty()) {
            throw new NoSuitableRouteException("The route list is empty: there is nothing to choose from.");
        }

        List<Route> feasible = new ArrayList<>();
        List<String> rejections = new ArrayList<>();
        for (Route route : routes) {
            if (route == null) {
                throw new IllegalArgumentException("Route list must not contain null elements.");
            }
            Optional<String> violation = findViolation(route, request);
            if (violation.isPresent()) {
                rejections.add("\n  Rejected route " + route.id() + ": " + violation.get());
            } else {
                feasible.add(route);
            }
        }

        if (feasible.isEmpty()) {
            throw new NoSuitableRouteException("No suitable route from " + request.origin() + " to "
                    + request.destination() + "." + String.join("", rejections));
        }
        return feasible;
    }
}
