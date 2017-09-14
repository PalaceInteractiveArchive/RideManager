package network.palace.ridemanager.events;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import network.palace.core.events.CoreEvent;
import network.palace.ridemanager.handlers.Ride;

import java.util.UUID;

/**
 * @author Marc
 * @since 9/13/17
 */
@RequiredArgsConstructor
public class RideEndEvent extends CoreEvent {
    @Getter private final Ride ride;
    @Getter private final UUID[] players;
}
