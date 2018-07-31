package network.palace.ridemanager.handlers.ride;

import lombok.Getter;
import network.palace.core.economy.CurrencyType;
import network.palace.core.player.CPlayer;
import network.palace.ridemanager.RideManager;
import network.palace.ridemanager.events.RideStartEvent;
import org.bukkit.*;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.entity.ArmorStand;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Marc on 1/26/17.
 */
public class SignRide extends Ride {
    private final Location spawnSign;
    @Getter private final int yAxis;
    @Getter private String modelName;
    private List<OldCart> carts = new ArrayList<>();

    public SignRide(String name, String displayName, int riders, double delay, Location exit, Location spawnSign, String modelName) {
        super(name, displayName, riders, delay, exit, CurrencyType.BALANCE, 0, 0);
        this.spawnSign = spawnSign;
        this.yAxis = spawnSign.getBlockY();
        this.modelName = modelName;
    }

    @Override
    public void move() {
        for (OldCart c : getCarts()) {
            c.move();
        }
    }

    @Override
    public void despawn() {
        for (OldCart c : getCarts()) {
            c.despawn();
        }
    }

    @Override
    public void handleEject(CPlayer player, boolean force) {
        handleEject(player);
    }

    @Override
    public void start(List<CPlayer> riders) {
        new RideStartEvent(this).call();
        for (CPlayer player : riders) {
            if (getOnRide().contains(player.getUniqueId())) {
                riders.remove(player);
            }
        }
        World w = Bukkit.getWorlds().get(0);
        Sign s = (Sign) w.getBlockAt(spawnSign).getState();
        Location sloc = spawnSign;
        BlockFace direction = RideManager.getBlockFace(s.getLine(3));
        float yaw = RideManager.getYaw(direction);
        Location loc = new Location(w, sloc.getBlockX() + 0.5, Double.parseDouble(s.getLine(2)),
                sloc.getBlockZ() + 0.5, yaw, 0);
        ItemStack item = new ItemStack(Material.STONE);
        OldCart c = new OldCart(this, loc, item, direction, modelName);
        for (CPlayer tp : riders) {
//            c.addPassenger(tp);
            tp.sendMessage(ChatColor.GREEN + "Ride starting in 1 second!");
            getOnRide().add(tp.getUniqueId());
        }
        Bukkit.getScheduler().runTaskLater(RideManager.getInstance(), () -> c.setPower(0.1), 20L);
        carts.add(c);
    }

    @Override
    public boolean sitDown(CPlayer player, ArmorStand stand) {
        return true;
    }

    private List<OldCart> getCarts() {
        return new ArrayList<>(carts);
    }

    @Override
    public void onChunkLoad(Chunk chunk) {
    }

    @Override
    public void onChunkUnload(Chunk chunk) {
    }
}
