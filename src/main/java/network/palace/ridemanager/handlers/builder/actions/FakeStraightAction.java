package network.palace.ridemanager.handlers.builder.actions;

import lombok.Getter;
import lombok.Setter;
import network.palace.ridemanager.handlers.actions.RideAction;
import org.bukkit.Location;

@Getter
@Setter
public class FakeStraightAction extends FakeAction {
    private Location to;

    public FakeStraightAction() {
        this(null);
    }

    public FakeStraightAction(Location to) {
        super(true);
        this.to = to;
    }

    @Override
    public RideAction duplicate() {
        return new network.palace.ridemanager.handlers.actions.StraightAction(to);
    }

    @Override
    public String toString() {
        return "";
    }
}
