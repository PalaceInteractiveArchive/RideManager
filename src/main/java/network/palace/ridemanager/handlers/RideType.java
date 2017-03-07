package network.palace.ridemanager.handlers;

/**
 * Created by Marc on 1/22/17.
 */
public enum RideType {
    SIGN, COASTER, TEACUPS;

    public static RideType fromString(String s) {
        switch (s.toLowerCase()) {
            case "sign":
                return SIGN;
            case "coaster":
                return COASTER;
            case "teacups":
                return TEACUPS;
            default:
                return SIGN;
        }
    }
}
