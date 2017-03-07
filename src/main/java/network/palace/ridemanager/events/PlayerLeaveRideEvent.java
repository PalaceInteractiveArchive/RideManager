package network.palace.ridemanager.events;

import lombok.Getter;
import lombok.Setter;
import network.palace.core.events.CoreEvent;
import network.palace.core.player.CPlayer;
import network.palace.ridemanager.handlers.Ride;
import org.bukkit.event.Cancellable;

/**
 * Created by Marc on 1/29/17.
 */
public class PlayerLeaveRideEvent extends CoreEvent implements Cancellable {
    @Getter private CPlayer player = null;
    @Getter private Ride ride = null;
    @Getter @Setter private boolean cancelled = true;

    public PlayerLeaveRideEvent(CPlayer player, Ride ride) {
        this.player = player;
        this.ride = ride;
    }
}
