package network.palace.ridemanager.handlers.actions;

import org.bukkit.Location;
import org.bukkit.block.Block;

/**
 * Created by Marc on 5/7/17.
 */
public class BlockAction extends RideAction {
    private final Location loc;
    private final int id;
    private final byte data;
    private boolean finished = false;

    public BlockAction(Location loc, int id, byte data) {
        super(false);
        this.loc = loc;
        this.id = id;
        this.data = data;
    }

    @Override
    @SuppressWarnings("deprecation")
    public void execute() {
        Block block = loc.getBlock();
        block.setTypeId(id);
        block.setData(data);
        finished = true;
    }

    @Override
    public boolean isFinished() {
        return finished;
    }
}
