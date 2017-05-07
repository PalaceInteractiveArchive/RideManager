package network.palace.ridemanager.handlers.actions;

import org.bukkit.Bukkit;
import org.bukkit.Location;

/**
 * Created by Marc on 5/2/17.
 */
public class TurnAction extends RideAction {
    private final Location to;
    private Location original = null;
    private final Location origin;
    private final boolean positive;
    private double radius;
    private float originAngle;
    private float targetAngle;
    private float angle;
    private float yawDifference;
    private boolean finished = false;

    public TurnAction(Location to, Location origin, boolean positive) {
        super(true);
        this.to = to;
        this.origin = origin;
        this.positive = positive;
    }

    @Override
    public void execute() {
        if (original == null) {
            this.original = cart.getLocation();
            this.originAngle = (float) Math.toDegrees(Math.atan2(origin.getX() - original.getX(), original.getZ() - origin.getZ()));
            this.yawDifference = cart.getYaw() - originAngle;
            double yDifference = origin.getY() - original.getY();
            this.radius = Math.sqrt(Math.pow(original.getX() - origin.getX(), 2) + Math.pow(original.getZ() - origin.getZ(), 2));
            this.angle = (float) Math.toDegrees(Math.acos(((original.getX() - origin.getX()) * (to.getX() - origin.getX()) +
                    (original.getZ() - origin.getZ()) * (to.getZ() - origin.getZ())) / Math.pow(radius, 2))) * (positive ? 1 : -1);
            double distance = Math.abs((2 * Math.PI * radius) / (360 / angle));
            this.targetAngle = originAngle + angle;
            Bukkit.broadcastMessage(origin + "\n" + to);
        }
        double angleChange = angle / ((Math.abs((2 * Math.PI * radius) / (360 / angle))) / cart.getPower());
        float dynamicAngle;
        if ((positive && originAngle + angleChange > targetAngle) || (!positive && originAngle + angleChange < targetAngle)) {
            dynamicAngle = targetAngle;
        } else {
            dynamicAngle = originAngle += angleChange;
        }
        Location target = cart.getRide().getRelativeLocation(-dynamicAngle, radius, origin);
        target.setYaw(dynamicAngle + yawDifference);
        cart.setYaw(target.getYaw());
        cart.teleport(target);
        if ((positive && dynamicAngle + angleChange > targetAngle) || (!positive && dynamicAngle + angleChange < targetAngle)) {
            finished = true;
        }
    }

    @Override
    public boolean isFinished() {
        return finished;
    }
}
