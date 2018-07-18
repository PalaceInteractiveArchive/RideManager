package network.palace.ridemanager.handlers.ride.flat;

import lombok.Getter;
import lombok.Setter;
import network.palace.core.Core;
import network.palace.core.economy.CurrencyType;
import network.palace.core.player.CPlayer;
import network.palace.ridemanager.handlers.ride.Ride;
import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;

public abstract class FlatRide extends Ride {
    @Getter protected FlatState state = FlatState.LOADING;
    @Getter protected boolean spawned = false;
    @Getter @Setter protected double speed = 0;
    @Getter protected boolean started = false;
    protected long startTime = 0;
    protected long ticks = 0;

    public FlatRide(String name, String displayName, int riders, double delay, Location exit, CurrencyType currencyType, int currencyAmount, int honorAmount) {
        super(name, displayName, riders, delay, exit, currencyType, currencyAmount, honorAmount);
    }

    protected void emptyStand(ArmorStand stand) {
        if (stand.getPassengers().isEmpty()) return;
        for (Entity e : stand.getPassengers()) {
            CPlayer p = Core.getPlayerManager().getPlayer(e.getUniqueId());
            if (p == null) continue;
            final Location pLoc = p.getLocation();
            stand.removePassenger(p.getBukkitPlayer());
            Location loc = getExit();
            if (state.equals(FlatState.LOADING)) {
                loc = stand.getLocation().add(0, 2, 0);
                loc.setYaw(pLoc.getYaw());
                loc.setPitch(pLoc.getPitch());
            }
            p.teleport(loc);
        }
    }
}
