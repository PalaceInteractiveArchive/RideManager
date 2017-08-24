package network.palace.ridemanager.handlers.actions;

import network.palace.ridemanager.handlers.Cart;
import network.palace.ridemanager.utils.MovementUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.util.Vector;

/**
 * Created by Marc on 5/2/17.
 */
public class TurnAction extends MoveAction {
    private final Location origin;
    private final boolean clockwise;
    private double radius;

    private float originAngle;
    private float targetAngle;
    private int angle;
    private float yawDifference;
    private double originalY;
    private double yDifference;
    private double yChange;
    private boolean finished = false;

    public TurnAction(Location origin, int angle) {
        this.origin = origin;
        this.angle = angle;
        this.clockwise = angle > 0;
    }

    @Override
    public void execute() {
        if (radius == 0) {
            if (angle > 180 || angle == 0) {
                finished = true;
                Bukkit.getLogger().severe("Cannot have a turn travel more than 180 degrees or equal 0!");
                return;
            }
            Location original = cart.getLocation();
            radius = original.distance(origin);
            originAngle = (float) Math.toDegrees(Math.atan2(origin.getX() - original.getX(), original.getZ() - origin.getZ()));
            yawDifference = cart.getYaw() - originAngle;
            originalY = original.getY();
            yDifference = 2 * (origin.getY() - original.getY());
            yChange = MovementUtil.pythag((Math.abs(angle) * radius * Math.PI) / 180, yDifference);
            targetAngle = originAngle + angle;
        }
        double angleChange = angle / ((Math.abs((2 * Math.PI * radius) / (360 / angle))) / (cart.getPower() * 1.66));
        float dynamicAngle;
        if ((clockwise && originAngle + angleChange > targetAngle) || (!clockwise && originAngle + angleChange < targetAngle)) {
            dynamicAngle = targetAngle;
            finished = true;
        } else {
            dynamicAngle = originAngle += angleChange;
        }
        Location rel = origin.clone();
        Location current = cart.getLocation();
        rel.setY(current.getY());
        Location target = cart.getRide().getRelativeLocation(-dynamicAngle, radius, rel);
        target.setYaw(dynamicAngle + yawDifference);
//        Bukkit.broadcastMessage(yDifference + " " + yChange + " " + cart.getPower() + " " + target.getY());
        if (yDifference != 0) {
            target.setY(target.getY() + (yChange / (20 / cart.getPower())));
        }
        cart.setYaw(target.getYaw());
        Vector v = target.toVector().subtract(cart.getLocation().toVector());
        if (v.getY() == 0) {
            v.setY(MovementUtil.getYMin());
        }
        cart.getStand().setVelocity(v);
        cart.teleport(target);
//        if (original == null) {
//            this.original = cart.getLocation();
//            this.originAngle = (float) Math.toDegrees(Math.atan2(origin.getX() - original.getX(), original.getZ() - origin.getZ()));
//            this.yawDifference = cart.getYaw() - originAngle;
//            this.yDifference = origin.getY() - original.getY();
//            this.radius = Math.sqrt(Math.pow(original.getX() - origin.getX(), 2) + Math.pow(original.getZ() - origin.getZ(), 2));
//            this.angle = (float) Math.toDegrees(Math.acos(((original.getX() - origin.getX()) * (to.getX() - origin.getX()) +
//                    (original.getZ() - origin.getZ()) * (to.getZ() - origin.getZ())) / Math.pow(radius, 2))) * (clockwise ? 1 : -1);
//            double distance = Math.abs((2 * Math.PI * radius) / (360 / angle));
//            this.targetAngle = originAngle + angle;
//            Bukkit.broadcastMessage(origin + "\n" + to);
//        }
//        double angleChange = angle / ((Math.abs((2 * Math.PI * radius) / (360 / angle))) / cart.getPower());
//        float dynamicAngle;
//        if ((clockwise && originAngle + angleChange > targetAngle) || (!clockwise && originAngle + angleChange < targetAngle)) {
//            dynamicAngle = targetAngle;
//        } else {
//            dynamicAngle = originAngle += angleChange;
//        }
//        Location target = cart.getRide().getRelativeLocation(-dynamicAngle, radius, origin);
//        target.setYaw(dynamicAngle + yawDifference);
//        cart.setYaw(target.getYaw());
//        cart.teleport(target);
//        if ((clockwise && dynamicAngle + angleChange > targetAngle) || (!clockwise && dynamicAngle + angleChange < targetAngle)) {
//            finished = true;
//        }
    }

    @Override
    public boolean isFinished() {
        return finished;
    }

    @Override
    public RideAction duplicate() {
        return new TurnAction(origin, angle);
    }

    @Override
    public String toString() {
        return "";
    }

    @Override
    public RideAction load(Cart cart) {
        setCart(cart);
        return this;
    }
}
