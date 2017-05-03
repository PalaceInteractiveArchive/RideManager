package network.palace.ridemanager.handlers;

/**
 * Created by Marc on 1/22/17.
 */
public enum RideType {
    FILE, SIGN, COASTER, TEACUPS, CAROUSEL, AERIAL_CAROUSEL;

    public static RideType fromString(String s) {
        switch (s.toLowerCase()) {
            case "file":
                return FILE;
            case "sign":
                return SIGN;
            case "coaster":
                return COASTER;
            case "teacups":
                return TEACUPS;
            case "carousel":
                return CAROUSEL;
            case "aerialcarousel":
            case "aerial_carousel":
                return AERIAL_CAROUSEL;
            default:
                return SIGN;
        }
    }
}
