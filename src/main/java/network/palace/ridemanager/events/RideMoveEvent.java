package network.palace.ridemanager.events;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import network.palace.core.events.CoreEvent;
import org.bukkit.Location;
import org.bukkit.entity.Entity;

@Getter
@RequiredArgsConstructor
public class RideMoveEvent extends CoreEvent {
    private final Entity vehicle;
    private final Location from;
    private final Location to;
}
