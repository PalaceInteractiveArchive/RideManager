package network.palace.ridemanager.handlers.actions;

import lombok.Getter;
import network.palace.ridemanager.handlers.builder.ActionType;
import network.palace.ridemanager.utils.MovementUtil;
import org.bukkit.Location;
import org.bukkit.util.Vector;

public class TurnAction extends MoveAction {
    @Getter private Location to;
    @Getter private Location p0 = null;
    private Location p1 = null;
    private Location p2 = null;
    private float angle = 0;
    private Location original;
    private double t = 0;
    private double yDifference;
    private boolean finished = false;

    public TurnAction(Location to, Location p0) {
        this.to = to;
        this.p0 = p0;
    }

    @Override
    public void execute() {
        if (p1 == null) {
            original = cart.getLocation();

            double p1_x = original.getX();
            double p1_z = original.getZ();

            double p2_x = to.getX();
            double p2_z = to.getZ();

            p1 = new Location(original.getWorld(), p1_x, original.getY(), p1_z);
            p2 = new Location(original.getWorld(), p2_x, original.getY(), p2_z);

            yDifference = to.getY() - original.getY();

            double x = 2 * (p2.getX() - p0.getX());
            double z = 2 * (p2.getZ() - p0.getZ());

            angle = (float) Math.toDegrees(((float) Math.atan2(z, x)));
        }

        if (t >= 1) {
            Vector v = to.toVector().subtract(cart.getLocation().toVector());
            if (v.getY() == 0) {
                v.setY(MovementUtil.getYMin());
            }
            cart.setVelocity(v);
            to.setYaw(original.getYaw() + angle);
            cart.teleport(to);
            finished = true;
            return;
        }

        double x = (Math.pow((1 - t), 2) * p1.getX())
                + (2 * t * (1 - t) * p0.getX())
                + Math.pow(t, 2) * p2.getX();

        double y;
        if (yDifference != 0) {
            y = original.getY() + (yDifference * t);
        } else {
            y = original.getY();
        }

        double z = (Math.pow((1 - t), 2) * p1.getZ())
                + (2 * t * (1 - t) * p0.getZ())
                + Math.pow(t, 2) * p2.getZ();

        Location next = new Location(original.getWorld(), x, y, z, original.getYaw(), 0);

        Vector v = next.toVector().subtract(cart.getLocation().toVector());
        if (v.getY() == 0) {
            v.setY(MovementUtil.getYMin());
        }
        cart.setVelocity(v);
        next.setYaw(original.getYaw() + (float) (angle * t));
        cart.teleport(next);

        t += cart.getSpeed() * 0.1;
    }

    @Override
    public boolean isFinished() {
        return finished;
    }

    @Override
    public RideAction duplicate() {
        return new TurnAction(to.clone(), p0.clone());
    }

    @Override
    public String toString() {
        return "Turn " + to.getX() + "," + to.getY() + "," + to.getZ() + " " + angle;
    }

    @Override
    public ActionType getActionType() {
        return ActionType.TURN;
    }
}
