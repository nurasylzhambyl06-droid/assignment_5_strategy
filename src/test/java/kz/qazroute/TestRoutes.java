package kz.qazroute;

import kz.qazroute.app.SampleData;
import kz.qazroute.model.DeliveryPriority;
import kz.qazroute.model.DeliveryRequest;
import kz.qazroute.model.Route;

/** Small helpers so tests can build their own tiny, predictable route lists. */
public final class TestRoutes {

    private TestRoutes() {
    }

    /** Astana -> Semey route with a readable name. totalCost = baseCost + distanceKm * 50. */
    public static Route route(String id, double distanceKm, int minutes, double baseCost,
                              double carbonKg, int trafficPenalty, boolean available) {
        return new Route(id, "Test route " + id, SampleData.ASTANA, SampleData.SEMEY,
                distanceKm, minutes, baseCost, carbonKg, trafficPenalty, available);
    }

    /** Astana -> Semey request with a generous or custom budget and deadline. */
    public static DeliveryRequest request(double maxBudget, int maxMinutes) {
        return new DeliveryRequest(SampleData.ASTANA, SampleData.SEMEY, DeliveryPriority.STANDARD,
                maxBudget, maxMinutes, false);
    }

    /** A request that no sensible test route can violate. */
    public static DeliveryRequest generousRequest() {
        return request(10_000_000, 100_000);
    }
}
