package network.palace.ridemanager.handlers.builder.actions;

import lombok.Getter;
import lombok.Setter;
import network.palace.ridemanager.handlers.actions.RideAction;
import network.palace.ridemanager.handlers.builder.ActionType;
import org.bukkit.Location;

@Getter
@Setter
public class FakeTurnAction extends FakeAction {
    private Location origin;
    private int angle;

    public FakeTurnAction() {
        this(null, Integer.MAX_VALUE);
    }

    public FakeTurnAction(Location origin, int angle) {
        super(true);
        this.origin = origin;
        this.angle = angle;
    }

    @Override
    public RideAction duplicate() {
        return new network.palace.ridemanager.handlers.actions.TurnAction(origin, angle);
    }

    @Override
    public String toString() {
        return "";
    }

    @Override
    public ActionType getActionType() {
        return ActionType.TURN;
    }

    @Override
    public boolean areFieldsIncomplete() {
        return origin == null || angle == Integer.MAX_VALUE;
    }
}
