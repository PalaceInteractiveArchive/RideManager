package network.palace.ridemanager.handlers;

public enum CurrencyType {
    BALANCE, TOKENS;

    public static CurrencyType fromString(String s) {
        switch (s.toLowerCase()) {
            case "balance":
                return BALANCE;
            case "tokens":
                return TOKENS;
            default:
                return BALANCE;
        }
    }
}
