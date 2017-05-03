package network.palace.ridemanager.handlers.actions;

import org.bukkit.Location;

/**
 * Created by Marc on 5/2/17.
 */
public class TeleportAction extends RideAction {
    private final Location to;
    private boolean finished = false;

    public TeleportAction(Location to) {
        super(true);
        this.to = to;
    }

    @Override
    public void execute() {
        cart.teleport(to);
        finished = true;
    }

    @Override
    public boolean isFinished() {
        return finished;
    }
}
