package kz.qazroute.app;

import java.util.List;

import kz.qazroute.model.DeliveryPriority;
import kz.qazroute.model.DeliveryRequest;
import kz.qazroute.model.Location;
import kz.qazroute.model.Route;

/**
 * Fictional, deterministic demonstration data.
 *
 * <p>The routes and all their numbers are ILLUSTRATIVE. They are not real-time road or logistics data.</p>
 *
 * <p>All 8 routes are alternatives for the same job: Astana -> Semey. Each strategy has a different winner:
 * fastest R1, cheapest R3, eco R5, balanced R8, distance-first R7. The other routes break a hard constraint
 * for the standard request (R2 budget, R4 deadline, R6 unavailable).</p>
 */
public final class SampleData {

    public static final Location ASTANA = new Location("Astana", 51.1694, 71.4491);
    public static final Location KARAGANDA = new Location("Karaganda", 49.8047, 73.1094);
    public static final Location KOKSHETAU = new Location("Kokshetau", 53.2948, 69.4048);
    public static final Location PAVLODAR = new Location("Pavlodar", 52.2873, 76.9674);
    public static final Location KOSTANAY = new Location("Kostanay", 53.2144, 63.6246);
    public static final Location SEMEY = new Location("Semey", 50.4111, 80.2275);

    /** Maximum budget of the standard demo request, in KZT. */
    public static final double DEMO_BUDGET = 80_000;
    /** Maximum delivery time of the standard demo request, in minutes (about 15 hours). */
    public static final int DEMO_DEADLINE_MINUTES = 900;

    private SampleData() {
        // utility class
    }

    /** The 8 demo routes, all from Astana to Semey. Order: id, name, distance, minutes, base cost, CO2, traffic, available. */
    public static List<Route> routes() {
        return List.of(
                route("R1", "Direct Highway via Pavlodar", 1000, 780, 24_000, 230, 40, true),
                route("R2", "Express Toll Road via Pavlodar", 1020, 640, 42_000, 250, 10, true),
                route("R3", "Rail Freight via Karaganda", 1120, 850, 6_000, 70, 30, true),
                route("R4", "Northern Loop via Kostanay", 1400, 1000, 5_000, 320, 60, true),
                route("R5", "Electric Truck via Karaganda", 1060, 830, 20_000, 40, 20, true),
                route("R6", "Night Shuttle via Pavlodar", 980, 600, 15_000, 200, 0, false),
                route("R7", "Regional Road via Kokshetau", 960, 840, 18_000, 245, 55, true),
                route("R8", "Mixed Rail-Road via Karaganda", 1050, 810, 14_000, 110, 25, true));
    }

    /** Astana -> Semey request with the standard demo budget and deadline. */
    public static DeliveryRequest request(DeliveryPriority priority) {
        return request(priority, false);
    }

    public static DeliveryRequest request(DeliveryPriority priority, boolean ecoPreferred) {
        return new DeliveryRequest(ASTANA, SEMEY, priority, DEMO_BUDGET, DEMO_DEADLINE_MINUTES, ecoPreferred);
    }

    private static Route route(String id, String name, double distanceKm, int minutes,
                               double baseCost, double carbonKg, int trafficPenalty, boolean available) {
        return new Route(id, name, ASTANA, SEMEY, distanceKm, minutes, baseCost, carbonKg, trafficPenalty, available);
    }
}
