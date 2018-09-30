package network.palace.ridemanager.handlers.actions;

import lombok.Getter;
import network.palace.ridemanager.handlers.builder.ActionType;
import network.palace.ridemanager.utils.MathUtil;
import network.palace.ridemanager.utils.MovementUtil;
import org.bukkit.Location;
import org.bukkit.util.Vector;

public class TurnAction extends MoveAction {
    @Getter private Location to;
    @Getter private Location p0 = null;
    private Location p1 = null;
    private Location p2 = null;
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
        }

        if (t >= 1) {
            t = 1;
            Vector v = to.toVector().subtract(cart.getLocation().toVector());
            if (v.getY() == 0) {
                v.setY(MovementUtil.getYMin());
            }
            cart.setVelocity(v);
            to.setYaw(MathUtil.getBezierAngleAt(t, p0, p1, p2) - 90);
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
        next.setYaw(MathUtil.getBezierAngleAt(t, p0, p1, p2) - 90);
        cart.teleport(next);

        t += changeOfT();
    }

    private double changeOfT() {
        double speed = cart.getSpeed();

        double v1_x = 2 * p1.getX() - 4 * p0.getX() + 2 * p2.getX();
        double v1_z = 2 * p1.getZ() - 4 * p0.getZ() + 2 * p2.getZ();

        double v2_x = 2 * p0.getX() - 2 * p1.getX();
        double v2_z = 2 * p0.getZ() - 2 * p1.getZ();

        Vector v1 = new Vector(v1_x, 0, v1_z);
        Vector v2 = new Vector(v2_x, 0, v2_z);

        double newT = (speed / ((v1.multiply(t)).add(v2)).length());
        if (newT > 1) {
            return 1;
        } else {
            return newT;
        }
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
        return "Turn " + to.getX() + "," + to.getY() + "," + to.getZ() + " " + p0.getX() + "," + p0.getY() + "," + p0.getZ();
    }

    @Override
    public ActionType getActionType() {
        return ActionType.TURN;
    }
}
