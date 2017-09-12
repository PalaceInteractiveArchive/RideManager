package network.palace.ridemanager.events;

import lombok.AllArgsConstructor;
import lombok.Getter;
import network.palace.core.events.CoreEvent;

/**
 * @author Marc
 * @since 8/31/17
 */
@AllArgsConstructor
public class RideManagerStatusEvent extends CoreEvent {
    @Getter private Status status;

    public enum Status {
        STARTING, STOPPING
    }
}
