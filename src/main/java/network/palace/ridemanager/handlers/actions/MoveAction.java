package network.palace.ridemanager.handlers.actions;

import org.bukkit.Location;
import org.bukkit.util.Vector;

/**
 * Created by Marc on 5/2/17.
 */
public class MoveAction extends RideAction {
    private Location to;
    private boolean finished = false;

    public MoveAction(Location to) {
        super(true);
        this.to = to;
    }

    @Override
    public void execute() {
        Location original = cart.getLocation();
        double distance = original.distance(to);
        Vector resultant = to.clone().subtract(original).toVector().normalize();
        double power = cart.getPower();
        Vector change = resultant.multiply(new Vector(power, power, power));
        Location next = cart.getLocation().add(change);
        if (next.distance(original) >= distance) {
            to.setYaw(next.getYaw());
            cart.teleport(to);
            finished = true;
        } else {
            cart.teleport(next);
        }
    }

    @Override
    public boolean isFinished() {
        return finished;
    }
}
