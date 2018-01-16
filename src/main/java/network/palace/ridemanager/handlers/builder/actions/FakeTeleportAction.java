package network.palace.ridemanager.handlers.builder.actions;

import lombok.Getter;
import lombok.Setter;
import network.palace.ridemanager.handlers.actions.RideAction;
import org.bukkit.Location;

@Getter
@Setter
public class FakeTeleportAction extends FakeAction {
    private Location to;

    public FakeTeleportAction() {
        this(null);
    }

    public FakeTeleportAction(Location to) {
        super(true);
        this.to = to;
    }

    @Override
    public RideAction duplicate() {
        return new network.palace.ridemanager.handlers.actions.TeleportAction(to);
    }

    @Override
    public String toString() {
        return "";
    }
}
