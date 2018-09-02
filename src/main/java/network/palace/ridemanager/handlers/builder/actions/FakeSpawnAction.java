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
    private Location loc;
    private double speed;
    private float yaw;

    public FakeSpawnAction() {
        this(null, Double.MAX_VALUE, Float.MAX_VALUE);
    }

    public FakeSpawnAction(Location loc, double speed, float yaw) {
        super(true);
        this.loc = loc;
        this.speed = speed;
        this.yaw = yaw;
    }

    @Override
    public RideAction duplicate() {
        return new SpawnAction(loc, speed, yaw);
    }

    @Override
    public String toString() {
        if (loc == null) {
            return "Spawn 0,0,0 " + speed + " " + yaw;
        }
        return "Spawn " + loc.getX() + "," + loc.getY() + "," + loc.getZ() + " " + speed + " " + yaw;
    }

    @Override
    public ActionType getActionType() {
        return ActionType.SPAWN;
    }

    @Override
    public boolean areFieldsIncomplete() {
        return loc == null || !(speed != Double.MAX_VALUE) || !(yaw != Float.MAX_VALUE);
    }
}
