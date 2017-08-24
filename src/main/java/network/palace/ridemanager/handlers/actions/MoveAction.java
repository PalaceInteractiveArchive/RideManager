package network.palace.ridemanager.handlers.actions;

import network.palace.ridemanager.handlers.Cart;
import org.bukkit.Location;

/**
 * @author Marc
 * @since 8/10/17
 */
public abstract class MoveAction extends RideAction {
    protected Location finalLocation = null;

    public MoveAction() {
        super(true);
    }

    @Override
    public RideAction load(Cart cart) {
        setCart(cart);
        RideAction a = cart.getPreviousAction(id);
        while (!(a instanceof MoveAction)) {
            a = cart.getPreviousAction(a.getId());
            if (a == null) break;
        }
        if (a != null) {
            finalLocation = ((MoveAction) a).finalLocation;
        } else {
            finalLocation = cart.getRide().getSpawn();
        }
        return this;
    }
}
