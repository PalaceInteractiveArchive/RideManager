package network.palace.ridemanager.handlers.actions;

import network.palace.ridemanager.utils.MovementUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.util.EulerAngle;
import org.bukkit.util.Vector;

/**
 * @author Marc
 * @since 5/2/17
 */
public class InclineAction extends MoveAction {
    private Location to;
    private int angle;
    private double starting = 361;
    private double slope = 0;
    private Location start;
    private double distance = 0;
    private boolean isAsymptote = false;
    private boolean finished = false;
    private double yDifference = 0;

    public InclineAction(Location to, int angle) {
        this.to = to;
        if (angle > 90) {
            angle = 90;
        }
        this.angle = angle;
        this.finalLocation = to;
        isAsymptote = Math.toRadians(angle) % (Math.PI / 2) == 0;
    }

    @Override
    public void execute() {
        Location original = cart.getLocation();
        double distance = original.distance(to);
        EulerAngle head = cart.getHeadPose();
        if (starting == 361) {
            //Location calculations
            yDifference = to.getY() - original.getY();
            this.distance = MovementUtil.pythag2(yDifference, to.distance(original));
            if (isAsymptote) {
                slope = 89.9999999;
            } else {
                slope = Math.tan(Math.toRadians(angle));
            }
            start = original.clone();
            //Head calculations
            starting = Math.toDegrees(head.getX());
        }

        double change = (angle - starting) / (20 / cart.getPower());
        cart.setHeadPose(cart.getHeadPose().add(Math.toRadians(-change), 0, 0));
        Bukkit.broadcastMessage(Math.toDegrees(head.getX()) + " " + change + " " + angle + " " + starting);

        Vector v = new Vector(to.getX() - original.getX(), 0, to.getZ() - original.getZ()).normalize();
        Location next = original.clone().add(v.multiply(cart.getPower()));

        double y = (10 * slope * Math.pow(original.getX() - start.getX(), 2)) / (Math.pow(to.getX() - start.getX(), 2)) + start.getY();
        next.setY(y);

        if (next.distance(original) >= distance) {
            finished = true;
            v = next.toVector().subtract(to.toVector());
            if (v.getY() == 0) {
                v.setY(MovementUtil.getYMin());
            }
            cart.setVelocity(v);
            cart.teleport(to);
        } else {
            v = next.toVector().subtract(cart.getLocation().toVector());
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
        return new InclineAction(to.clone(), angle);
    }

    @Override
    public String toString() {
        return "";
    }
}
