package network.palace.ridemanager.handlers.actions;

import org.bukkit.Location;

/**
 * Created by Marc on 5/2/17.
 */
public class TurnAction extends RideAction {
    private final Location to;
    private Location original = null;
    private final Location origin;
    private final boolean positive;
    private final double yDifference;
    private final double radius;
    private final double distance;
    private float targetAngle;
    private float dynamicAngle;
    private final float angle;
    private boolean finished = false;

    public TurnAction(Location to, Location origin, boolean positive) {
        super(true);
        this.to = to;
        this.origin = origin;
        this.positive = positive;
        this.yDifference = origin.getY() - original.getY();
        this.radius = Math.sqrt(Math.pow(original.getX() - origin.getX(), 2) + Math.pow(original.getZ() - origin.getZ(), 2));
        this.distance = Math.sqrt(Math.pow(to.getX() - original.getX(), 2) + Math.pow(to.getZ() - original.getZ(), 2));

        this.angle = (float) Math.acos(((original.getX() - origin.getX()) * (to.getX() - origin.getX()) +
                (original.getZ() - origin.getZ()) * (to.getZ() - origin.getZ())) / Math.pow(radius, 2));
    }

    @Override
    public void execute() {
        if (original == null) {
            this.original = cart.getLocation();
            this.targetAngle = cart.getYaw() + angle;
            this.dynamicAngle = cart.getYaw();
        }
        Location cur = cart.getLocation().clone();
        Location comparison = cur.clone();
        double angleChange = angle / ((Math.abs((2 * Math.PI * radius) / (360 / angle))) / cart.getPower());
        Location target = cart.getRide().getRelativeLocation(dynamicAngle += angleChange, radius, origin);
        target.setYaw(dynamicAngle);
        cart.teleport(target);
        comparison.setY(original.getY());
        if (dynamicAngle >= targetAngle) {
            finished = true;
        }
    }

    @Override
    public boolean isFinished() {
        return finished;
    }
}
