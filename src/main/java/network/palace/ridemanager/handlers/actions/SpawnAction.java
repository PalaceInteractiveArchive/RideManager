package network.palace.ridemanager.handlers.actions;

import network.palace.ridemanager.handlers.Cart;
import network.palace.ridemanager.utils.MovementUtil;
import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.util.Vector;

/**
 * @author Marc
 * @since 8/10/17
 */
public class SpawnAction extends MoveAction {
    private final Location loc;
    private final double speed;
    private float yaw;
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
        finished = true;
    }

    @Override
    public boolean isFinished() {
        return finished;
    }

    @Override
    public RideAction duplicate() {
        return new SpawnAction(loc, speed, yaw);
    }

    @Override
    public String toString() {
        return "Spawn " + loc.getX() + "," + loc.getY() + "," + loc.getZ() + " " + speed;
    }

    @Override
    public RideAction load(Cart cart) {
        setCart(cart);
        return this;
    }
}
