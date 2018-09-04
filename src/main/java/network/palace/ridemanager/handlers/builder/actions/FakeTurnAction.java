package network.palace.ridemanager.handlers.builder.actions;

import lombok.Getter;
import lombok.Setter;
import network.palace.ridemanager.handlers.actions.RideAction;
import network.palace.ridemanager.handlers.actions.TurnAction;
import network.palace.ridemanager.handlers.builder.ActionType;
import org.bukkit.Location;

@Getter
@Setter
public class FakeTurnAction extends FakeAction {
    private Location to;
    private Location p0;

    public FakeTurnAction() {
        this(null, null);
    }

    public FakeTurnAction(Location to, Location p0) {
        super(true);
        this.to = to;
        this.p0 = p0;
    }

    @Override
    public RideAction duplicate() {
        return new TurnAction(to, p0);
    }

    @Override
    public String toString() {
        String toS = to == null ? "0,0,0" : to.getX() + "," + to.getY() + "," + to.getZ();
        String p0S = p0 == null ? "0,0,0" : p0.getX() + "," + p0.getY() + "," + p0.getZ();
        return "Turn " + toS + " " + p0S;
    }

    @Override
    public ActionType getActionType() {
        return ActionType.TURN;
    }

    @Override
    public boolean areFieldsIncomplete() {
        return to == null || p0 == null;
    }
}
