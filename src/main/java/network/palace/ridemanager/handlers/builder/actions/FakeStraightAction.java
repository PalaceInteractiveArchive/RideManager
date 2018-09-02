package network.palace.ridemanager.handlers.builder.actions;

import lombok.Getter;
import lombok.Setter;
import network.palace.ridemanager.handlers.actions.RideAction;
import network.palace.ridemanager.handlers.builder.ActionType;
import org.bukkit.Location;

@Getter
@Setter
public class FakeStraightAction extends FakeAction {
    private Location to;
    private String autoYaw;

    public FakeStraightAction() {
        this(null, "");
    }

    public FakeStraightAction(Location to, String autoYaw) {
        super(true);
        this.to = to;
        this.autoYaw = autoYaw;
    }

    @Override
    public RideAction duplicate() {
        return new network.palace.ridemanager.handlers.actions.StraightAction(to, autoYaw);
    }

    @Override
    public String toString() {
        if (to == null) {
            return "Straight 0,0,0" + (autoYaw.isEmpty() ? "" : " " + autoYaw);
        }
        return "Straight " + to.getX() + "," + to.getY() + "," + to.getZ() + (autoYaw.isEmpty() ? "" : " " + autoYaw);
    }

    @Override
    public ActionType getActionType() {
        return ActionType.STRAIGHT;
    }

    @Override
    public boolean areFieldsIncomplete() {
        return to == null || autoYaw == null;
    }
}
