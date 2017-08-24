package network.palace.ridemanager.handlers.actions;

import network.palace.ridemanager.handlers.Cart;
import network.palace.ridemanager.utils.MovementUtil;
import org.bukkit.Location;
import org.bukkit.util.Vector;

/**
 * @author Marc
 * @since 5/2/17
 */
public class StraightAction extends MoveAction {
    private Location to;
    private boolean finished = false;

    public StraightAction(Location to) {
        this.to = to;
        this.finalLocation = to;
    }

    @Override
    public void execute() {
        Location original = cart.getLocation();
        float yaw = (float) Math.toDegrees(Math.atan2(Math.abs(to.getX() - original.getX()), Math.abs(to.getZ() - original.getZ())));
        double distance = original.distance(to);
        Vector resultant = to.clone().subtract(original).toVector().normalize();
        double power = cart.getPower();
        Vector change = resultant.multiply(new Vector(power, power, power));
        Location next = cart.getLocation().add(change);
        next.setYaw(yaw);
        if (next.distance(original) >= distance) {
            to.setYaw(yaw);
            Vector v = to.toVector().subtract(original.toVector());
            if (v.getY() == 0) {
                v.setY(MovementUtil.getYMin());
            }
            cart.getStand().setVelocity(v);
            cart.teleport(to);
            finished = true;
        } else {
            Vector v = next.toVector().subtract(original.toVector());
            if (v.getY() == 0) {
                v.setY(MovementUtil.getYMin());
            }
            cart.getStand().setVelocity(v);
            cart.teleport(next);
        }
    }

    @Override
    public boolean isFinished() {
        return finished;
    }

    @Override
    public RideAction duplicate() {
        return new StraightAction(to);
    }

    @Override
    public String toString() {
        return "Straight " + to.getX() + "," + to.getY() + "," + to.getZ();
    }

    @Override
    public RideAction load(Cart cart) {
        setCart(cart);
        return this;
    }
}
