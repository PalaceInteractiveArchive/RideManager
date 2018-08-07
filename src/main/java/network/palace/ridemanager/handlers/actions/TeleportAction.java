package network.palace.ridemanager.handlers.actions;

import lombok.Getter;
import network.palace.ridemanager.handlers.builder.ActionType;
import network.palace.ridemanager.utils.MovementUtil;
import org.bukkit.Location;
import org.bukkit.util.Vector;

/**
 * Created by Marc on 5/2/17.
 */
public class TeleportAction extends MoveAction {
    @Getter private final Location to;
    private boolean finished = false;

    public TeleportAction(Location to) {
        this.to = to;
        this.finalLocation = to;
    }

    @Override
    public void execute() {
        cart.teleport(to);
        cart.setVelocity(new Vector(0, MovementUtil.getYMin(), 0));
        finished = true;
    }

    @Override
    public boolean isFinished() {
        return finished;
    }

    @Override
    public RideAction duplicate() {
        return new TeleportAction(to.clone());
    }

    @Override
    public String toString() {
        return "Teleport " + to.getX() + "," + to.getY() + "," + to.getZ();
    }

    @Override
    public ActionType getActionType() {
        return ActionType.TELEPORT;
    }
}
