package network.palace.ridemanager.handlers.actions.sensors;

import lombok.Getter;
import lombok.Setter;
import network.palace.ridemanager.handlers.ride.file.Cart;
import org.bukkit.Location;

import java.util.UUID;

public abstract class RideSensor {
    @Getter protected UUID id = UUID.randomUUID();
    @Getter @Setter protected Cart cart = null;
    @Getter protected Location location;
    @Getter protected double radius;
    @Getter protected boolean activated = false;

    public RideSensor(Location loc, double radius) {
        this.location = loc;
        this.radius = radius;
    }

    public boolean isInRadius(Location loc) {
        return loc.distance(location) <= radius;
    }

    public void activate() {
        this.activated = true;
    }

    public RideSensor load(Cart cart) {
        setCart(cart);
        return this;
    }

    public abstract RideSensor duplicate();
}
