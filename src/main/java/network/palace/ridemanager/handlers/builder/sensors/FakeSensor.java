package network.palace.ridemanager.handlers.builder.sensors;

import network.palace.ridemanager.handlers.actions.sensors.RideSensor;
import org.bukkit.Location;

public class FakeSensor extends RideSensor {

    public FakeSensor(Location loc, double radius) {
        super(loc, radius);
    }

    @Override
    public RideSensor duplicate() {
        return null;
    }
}
