package network.palace.ridemanager.handlers;

public enum SensorType {
    BLOCK, SHOW, SPEED, TEXT;

    public static SensorType fromString(String s) {
        switch (s.toLowerCase()) {
            case "block":
                return BLOCK;
            case "show":
                return SHOW;
            case "speed":
                return SPEED;
            case "text":
                return TEXT;
        }
        return null;
    }
}
