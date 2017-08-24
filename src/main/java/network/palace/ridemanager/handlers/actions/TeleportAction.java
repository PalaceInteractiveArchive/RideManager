package network.palace.ridemanager.handlers.actions;

import network.palace.ridemanager.handlers.Cart;
import network.palace.ridemanager.utils.MovementUtil;
import org.bukkit.Location;
import org.bukkit.util.Vector;

/**
 * Created by Marc on 5/2/17.
 */
public class TeleportAction extends MoveAction {
    private final Location to;
    private boolean finished = false;

    public TeleportAction(Location to) {
        this.to = to;
        this.finalLocation = to;
    }

    @Override
    public void execute() {
        cart.teleport(to);
        cart.setYaw(to.getYaw());
        cart.getStand().setVelocity(new Vector(0, MovementUtil.getYMin(), 0));
        finished = true;
    }

    @Override
    public boolean isFinished() {
        return finished;
    }

    @Override
    public RideAction duplicate() {
        return new TeleportAction(to);
    }

    @Override
    public String toString() {
        return "Teleport " + to.getX() + "," + to.getY() + "," + to.getZ();
    }

    @Override
    public RideAction load(Cart cart) {
        setCart(cart);
        return this;
    }
}
