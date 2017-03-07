package network.palace.ridemanager.handlers;

import lombok.Getter;
import network.palace.core.Core;
import network.palace.core.player.CPlayer;
import network.palace.ridemanager.RideManager;
import org.bukkit.*;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Created by Marc on 1/26/17.
 */
public class SignRide extends Ride {
    private final Location spawnSign;
    @Getter private final int yAxis;
    private List<Cart> carts = new ArrayList<>();

    public SignRide(String name, String displayName, int riders, double delay, Location exit, Location spawnSign) {
        super(name, displayName, riders, delay, exit);
        this.spawnSign = spawnSign;
        this.yAxis = spawnSign.getBlockY();
    }

    @Override
    public void move() {
        for (Cart c : getCarts()) {
            c.move();
        }
    }

    @Override
    public void despawn() {
        for (Cart c : getCarts()) {
            c.despawn();
        }
    }

    @Override
    public void start() {
        List<UUID> queue = getQueue();
        List<UUID> riding = new ArrayList<>();
        if (queue.size() < getRiders()) {
            for (UUID uuid : queue) {
                riding.add(uuid);
            }
            queue.clear();
        } else {
            for (int i = 0; i < getRiders(); i++) {
                riding.add(queue.get(0));
                queue.remove(0);
            }
        }
        List<CPlayer> riders = new ArrayList<>();
        for (UUID uuid : riding) {
            CPlayer tp = Core.getPlayerManager().getPlayer(uuid);
            if (tp == null) {
                continue;
            }
            riders.add(tp);
        }
        World w = Bukkit.getWorlds().get(0);
        Sign s = (Sign) w.getBlockAt(spawnSign).getState();
        Location sloc = spawnSign;
        BlockFace direction = RideManager.getBlockFace(s.getLine(3));
        float yaw = RideManager.getYaw(direction);
        Location loc = new Location(w, sloc.getBlockX() + 0.5, Double.parseDouble(s.getLine(2)),
                sloc.getBlockZ() + 0.5, yaw, 0);
        ItemStack item = new ItemStack(Material.STONE);
        Cart c = new Cart(this, loc, item, direction);
        for (CPlayer tp : riders) {
//            c.addPassenger(tp);
            tp.sendMessage(ChatColor.GREEN + "Ride starting in 1 second!");
            getOnRide().add(tp.getUniqueId());
        }
        Bukkit.getScheduler().runTaskLater(RideManager.getInstance(), new Runnable() {
            @Override
            public void run() {
                c.setPower(0.1);
            }
        }, 20L);
        carts.add(c);
    }

    private List<Cart> getCarts() {
        return new ArrayList<>(carts);
    }
}
