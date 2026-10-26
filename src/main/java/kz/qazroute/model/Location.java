package kz.qazroute.model;

/**
 * A city in the QazRoute network.
 *
 * <p>Coordinates are illustrative and only validated for a sensible range.</p>
 */
public record Location(String name, double latitude, double longitude) {

    public Location {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Location name must not be empty.");
        }
        if (!(latitude >= -90 && latitude <= 90)) {
            throw new IllegalArgumentException("Latitude must be between -90 and 90: " + latitude);
        }
        if (!(longitude >= -180 && longitude <= 180)) {
            throw new IllegalArgumentException("Longitude must be between -180 and 180: " + longitude);
        }
    }

    @Override
    public String toString() {
        return name;
    }
}
