package network.palace.ridemanager.listeners;

import network.palace.ridemanager.RideManager;
import network.palace.ridemanager.handlers.ride.Ride;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;

/**
 * Created by Marc on 1/29/17.
 */
public class ChunkListener implements Listener {

    @EventHandler(priority = EventPriority.LOWEST)
    public void onChunkLoad(ChunkLoadEvent event) {
        for (Ride ride : RideManager.getMovementUtil().getRides()) {
            try {
                ride.onChunkLoad(event.getChunk());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onChunkUnload(ChunkUnloadEvent event) {
        for (Ride ride : RideManager.getMovementUtil().getRides()) {
            try {
                ride.onChunkUnload(event.getChunk());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
