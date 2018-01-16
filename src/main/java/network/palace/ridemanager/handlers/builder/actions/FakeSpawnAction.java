package network.palace.ridemanager.handlers.builder.actions;

import lombok.Getter;
import lombok.Setter;
import network.palace.ridemanager.handlers.actions.RideAction;
import network.palace.ridemanager.handlers.actions.SpawnAction;
import org.bukkit.Location;

@Getter
@Setter
public class FakeSpawnAction extends FakeAction {
    private Location location;
    private double speed;
    private float yaw;

    public FakeSpawnAction() {
        this(null, 0, 0);
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
}
