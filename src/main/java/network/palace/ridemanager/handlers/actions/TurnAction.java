package network.palace.ridemanager.handlers.actions;

import org.bukkit.Location;
import org.bukkit.util.Vector;

/**
 * Created by Marc on 5/2/17.
 */
public class MoveAction extends RideAction {
    private final Location original;
    private final double distance;
    private final Vector change;
    private boolean finished = false;

    public MoveAction(Location to) {
        super(true);
        original = cart.getLocation();
        distance = original.distance(to);
        Vector resultant = to.subtract(original).toVector().normalize();
        double power = cart.getPower();
        change = resultant.divide(new Vector(power, power, power));
    }

    @Override
    public void execute() {
        Location cur = cart.getLocation();
        double power = cart.getPower();
        Vector v = new Vector(change.getX() * power, change.getY() * power, change.getZ() * power);
        cart.teleport(cur.add(v));
        if (cur.distance(original) >= distance) {
            finished = true;
        }
    }

    @Override
    public boolean isFinished() {
        return finished;
    }
}
