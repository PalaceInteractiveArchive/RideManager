package network.palace.ridemanager.handlers.builder.actions;

import lombok.Getter;
import lombok.Setter;
import network.palace.ridemanager.handlers.actions.RideAction;
import org.bukkit.Location;

@Getter
@Setter
public class FakeExitAction extends FakeAction {
    private Location to;
    private String autoYaw;

    public FakeExitAction() {
        this(null, "");
    }

    public FakeExitAction(Location to, String autoYaw) {
        super(true);
        this.to = to;
        this.autoYaw = autoYaw;
    }

    @Override
    public RideAction duplicate() {
        return new network.palace.ridemanager.handlers.actions.ExitAction(to.clone(), autoYaw);
    }

    @Override
    public String toString() {
        return "";
    }
}
