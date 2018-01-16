package network.palace.ridemanager.events;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import network.palace.core.events.CoreEvent;
import network.palace.core.player.CPlayer;
import network.palace.ridemanager.handlers.ride.Ride;
import org.bukkit.event.Cancellable;

/**
 * Created by Marc on 1/29/17.
 */
@RequiredArgsConstructor
public class PlayerLeaveRideEvent extends CoreEvent implements Cancellable {
    @Getter private final CPlayer player;
    @Getter private final Ride ride;
    @Getter @Setter private boolean cancelled = true;
}
