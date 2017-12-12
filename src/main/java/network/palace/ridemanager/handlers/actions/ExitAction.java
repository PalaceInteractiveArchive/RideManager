package network.palace.ridemanager.handlers.actions;

import lombok.Getter;
import network.palace.ridemanager.utils.MovementUtil;
import org.bukkit.Location;
import org.bukkit.util.Vector;

/**
 * @author Marc
 * @since 8/10/17
 */
public class ExitAction extends MoveAction {
    @Getter private final Location to;
    private boolean finished = false;

    public ExitAction(Location to) {
        this.to = to;
        this.finalLocation = to;
    }

    @Override
    public void execute() {
        Location original = cart.getLocation();
        float yaw = (float) Math.toDegrees(Math.atan2(original.getZ() - to.getZ(), original.getX() - to.getX())) + 90;
        double distance = original.distance(to);
        Vector resultant = to.clone().subtract(original).toVector().normalize();
        double power = cart.getPower();
        Vector change = resultant.multiply(new Vector(power, power, power));
        Location next = cart.getLocation().add(change);
        if (getCart().getRide().isAutoYaw()) {
            next.setYaw(yaw);
        } else {
            next.setYaw(original.getYaw());
        }
        if (next.distance(original) >= distance) {
            if (getCart().getRide().isAutoYaw()) {
                to.setYaw(yaw);
            } else {
                to.setYaw(original.getYaw());
            }
            cart.teleport(to);
            finished = true;
            cart.empty();
        } else {
            Vector v = next.toVector().subtract(original.toVector());
            if (v.getY() == 0) {
                v.setY(MovementUtil.getYMin());
            }
            cart.setVelocity(v);
            cart.teleport(next);
        }
    }

    @Override
    public boolean isFinished() {
        return finished;
    }

    @Override
    public RideAction duplicate() {
        return new ExitAction(to.clone());
    }

    @Override
    public String toString() {
        return "Exit " + to.getX() + "," + to.getY() + "," + to.getZ();
    }
}
