package network.palace.ridemanager.handlers.ride.flat;

import lombok.Getter;
import lombok.Setter;
import network.palace.core.economy.CurrencyType;
import network.palace.ridemanager.handlers.ride.Ride;
import org.bukkit.Location;

public abstract class FlatRide extends Ride {
    @Getter protected FlatState state = FlatState.LOADING;
    @Getter protected boolean spawned = false;
    @Getter @Setter protected double speed = 0;
    @Getter protected boolean started = false;
    protected long startTime = 0;
    protected long ticks = 0;

    public FlatRide(String name, String displayName, int riders, double delay, Location exit, CurrencyType currencyType, int currencyAmount) {
        super(name, displayName, riders, delay, exit, currencyType, currencyAmount);
    }
}
