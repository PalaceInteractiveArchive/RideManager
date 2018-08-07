package network.palace.ridemanager.handlers.builder.actions;

import lombok.Getter;
import lombok.Setter;
import network.palace.ridemanager.handlers.actions.RideAction;
import network.palace.ridemanager.handlers.actions.SpawnAction;
import network.palace.ridemanager.handlers.builder.ActionType;
import org.bukkit.Location;

@Getter
@Setter
public class FakeSpawnAction extends FakeAction {
    private Location location;
    private double speed;
    private float yaw;

    public FakeSpawnAction() {
        this(null, Double.MAX_VALUE, Float.MAX_VALUE);
    }

    public FakeSpawnAction(Location loc, double speed, float yaw) {
        super(true);
        location = loc;
        this.speed = speed;
        this.yaw = yaw;
    }

    @Override
    public RideAction duplicate() {
        return new SpawnAction(location, speed, yaw);
    }

    @Override
    public String toString() {
        return "";
    }

    @Override
    public ActionType getActionType() {
        return ActionType.SPAWN;
    }

    @Override
    public boolean areFieldsIncomplete() {
        return location == null || !(speed != Double.MAX_VALUE) || !(yaw != Float.MAX_VALUE);
    }
}
