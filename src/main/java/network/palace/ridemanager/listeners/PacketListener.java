package network.palace.ridemanager.listeners;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import network.palace.core.Core;
import network.palace.core.player.CPlayer;
import network.palace.ridemanager.RideManager;
import network.palace.ridemanager.events.PlayerLeaveRideEvent;
import network.palace.ridemanager.handlers.Ride;
import org.bukkit.event.Listener;

import java.lang.reflect.Field;

/**
 * Created by Marc on 1/29/17.
 */
public class PacketListener implements Listener {

    public PacketListener() {
        ProtocolManager manager = ProtocolLibrary.getProtocolManager();
        manager.addPacketListener(new PacketAdapter(RideManager.getInstance(), PacketType.Play.Client.STEER_VEHICLE) {
            @Override
            public void onPacketReceiving(PacketEvent event) {
                CPlayer player = Core.getPlayerManager().getPlayer(event.getPlayer());
                PacketContainer packet = event.getPacket();
                try {
                    Field f = packet.getBooleans().getFields().get(1);
                    f.setAccessible(true);
                    if (!f.getBoolean(packet.getHandle())) {
                        return;
                    }
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                    return;
                }
                Ride ride = RideManager.getInstance().getCurrentRide(player);
                if (ride == null) {
                    return;
                }
                PlayerLeaveRideEvent leaveRide = new PlayerLeaveRideEvent(player, ride);
                leaveRide.call();
                event.setCancelled(leaveRide.isCancelled());
            }
        });
    }
}
