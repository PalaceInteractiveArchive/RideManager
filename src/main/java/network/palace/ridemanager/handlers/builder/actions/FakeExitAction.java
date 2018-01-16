package network.palace.ridemanager.handlers.builder.actions;

import lombok.Getter;
import lombok.Setter;
import network.palace.ridemanager.handlers.actions.RideAction;
import org.bukkit.Location;

@Getter
@Setter
public class FakeExitAction extends FakeAction {
    private Location to;

    public FakeExitAction() {
        this(null);
    }

    public FakeExitAction(Location to) {
        super(true);
        this.to = to;
    }

    @Override
    public RideAction duplicate() {
        return new network.palace.ridemanager.handlers.actions.ExitAction(to.clone());
    }

    @Override
    public String toString() {
        return "";
    }
}
