package network.palace.ridemanager.handlers.actions.sensors;

import org.bukkit.Location;
import org.bukkit.Material;

public class BlockSensor extends RideSensor {
    private final Location blockLoc;
    private final Material type;

    public BlockSensor(Location loc, double radius, Location blockLoc, Material type) {
        super(loc, radius);
        this.blockLoc = blockLoc;
        this.type = type;
    }

    @Override
    public void activate() {
        super.activate();
        blockLoc.getBlock().setType(type);
    }

    @Override
    public RideSensor duplicate() {
        return new BlockSensor(location, radius, blockLoc, type);
    }
}
