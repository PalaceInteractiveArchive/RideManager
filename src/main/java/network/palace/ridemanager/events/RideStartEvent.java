package network.palace.ridemanager.events;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import network.palace.core.events.CoreEvent;
import network.palace.ridemanager.handlers.Ride;

/**
 * @author Marc
 * @since 8/25/17
 */
@RequiredArgsConstructor
public class RideStartEvent extends CoreEvent {
    @Getter private final Ride ride;
}
