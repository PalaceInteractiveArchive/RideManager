package network.palace.ridemanager.handlers.actions;

import lombok.Getter;
import network.palace.ridemanager.handlers.builder.ActionType;
import network.palace.ridemanager.utils.MovementUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.util.Vector;

/**
 * Created by Marc on 5/2/17.
 */
public class CircleTurnAction extends MoveAction {
    @Getter private final Location to;
    @Getter private Location origin;
    @Getter private int angle;

    private final boolean clockwise;
    private double radius;
    private float originAngle;
    private float targetAngle;
    private float yawDifference;
    private double originalY;
    private double yDifference;
    private double yChange;
    private boolean finished = false;

    public CircleTurnAction(Location to, int angle) {
        this.to = to;
        this.angle = angle;
        this.clockwise = angle > 0;
    }

    @Override
    public void execute() {
        if (radius == 0) {
            if (Math.abs(angle) > 90 || angle == 0) {
                finished = true;
                Bukkit.getLogger().severe("Cannot have a turn travel more than 90 degrees or equal 0!");
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
        double angleChange = angle / ((Math.abs((2 * Math.PI * radius) / (360.0 / angle))) / cart.getSpeed());
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
//        Bukkit.broadcastMessage(yDifference + " " + yChange + " " + cart.getSpeed() + " " + target.getRelativeY());
        if (yDifference != 0) {
            target.setY(target.getY() + (yChange / (20 / cart.getSpeed())));
        }
        cart.setYaw(target.getYaw());
        Vector v = target.toVector().subtract(cart.getLocation().toVector());
        if (v.getY() == 0) {
            v.setY(MovementUtil.getYMin());
        }
        cart.setVelocity(v);
        cart.teleport(target);
//        if (original == null) {
//            this.original = cart.getLocation();
//            this.originAngle = (float) Math.toDegrees(Math.atan2(origin.getRelativeX() - original.getRelativeX(), original.getRelativeZ() - origin.getRelativeZ()));
//            this.yawDifference = cart.getYaw() - originAngle;
//            this.yDifference = origin.getRelativeY() - original.getRelativeY();
//            this.radius = Math.sqrt(Math.pow(original.getRelativeX() - origin.getRelativeX(), 2) + Math.pow(original.getRelativeZ() - origin.getRelativeZ(), 2));
//            this.angle = (float) Math.toDegrees(Math.acos(((original.getRelativeX() - origin.getRelativeX()) * (to.getRelativeX() - origin.getRelativeX()) +
//                    (original.getRelativeZ() - origin.getRelativeZ()) * (to.getRelativeZ() - origin.getRelativeZ())) / Math.pow(radius, 2))) * (clockwise ? 1 : -1);
//            double distance = Math.abs((2 * Math.PI * radius) / (360 / angle));
//            this.targetAngle = originAngle + angle;
//            Bukkit.broadcastMessage(origin + "\n" + to);
//        }
//        double angleChange = angle / ((Math.abs((2 * Math.PI * radius) / (360 / angle))) / cart.getSpeed());
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
        return new CircleTurnAction(origin.clone(), angle);
    }

    @Override
    public String toString() {
        return "Turn " + origin.getX() + "," + origin.getY() + "," + origin.getZ() + " " + angle;
    }

    @Override
    public ActionType getActionType() {
        return ActionType.TURN;
    }
}
