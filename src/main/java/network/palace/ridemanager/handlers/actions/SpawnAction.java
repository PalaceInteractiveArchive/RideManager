package network.palace.ridemanager.handlers.actions;

import lombok.Getter;
import network.palace.ridemanager.handlers.ModelMap;
import network.palace.ridemanager.handlers.Seat;
import network.palace.ridemanager.utils.MovementUtil;
import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.util.Vector;

/**
 * @author Marc
 * @since 8/10/17
 */
public class SpawnAction extends MoveAction {
    @Getter private final Location loc;
    @Getter private final double speed;
    @Getter private float yaw;
    private boolean finished = false;

    public SpawnAction(Location loc, double speed, float yaw) {
        this.loc = loc;
        this.speed = speed;
        this.yaw = yaw;
        this.finalLocation = loc;
    }

    @Override
    public void execute() {
        loc.setYaw(yaw);
        loc.setY(loc.getY() - MovementUtil.armorStandHeight);
        ArmorStand stand = loc.getWorld().spawn(loc, ArmorStand.class);
        stand.setGravity(true);
        stand.setBasePlate(false);
        stand.setArms(false);
        stand.setHelmet(cart.getModel());
        stand.setVelocity(new Vector(0, MovementUtil.getYMin(), 0));
        cart.setStand(stand);
        cart.setYaw(yaw);
        cart.setPower(speed);
        ModelMap map = cart.getMap();
        for (Seat seat : map.getSeats()) {
            Seat copy = seat.copy();
            copy.spawn(loc, cart.getRide());
            cart.addSeat(copy);
        }
        /*
        if (angle < 0) {
            angle = 360 + angle;
        }
        double rad = Math.toRadians(angle);
        double x = Math.sin(rad) * radius;
        double z = Math.cos(rad) * radius;
        return center.clone().add(x, 0, z);
         */
        finished = true;
    }

    @Override
    public boolean isFinished() {
        return finished;
    }

    @Override
    public RideAction duplicate() {
        return new SpawnAction(loc.clone(), speed, yaw);
    }

    @Override
    public String toString() {
        return "Spawn " + loc.getX() + "," + loc.getY() + "," + loc.getZ() + " " + speed + " " + yaw;
    }
}
