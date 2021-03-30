package network.palace.ridemanager.handlers.ride.flat;

import lombok.Getter;
import lombok.Setter;
import network.palace.core.Core;
import network.palace.core.economy.currency.CurrencyType;
import network.palace.core.player.CPlayer;
import network.palace.core.player.CPlayerActionBarManager;
import network.palace.core.player.Rank;
import network.palace.core.utils.ItemUtil;
import network.palace.ridemanager.RideManager;
import network.palace.ridemanager.events.RideStartEvent;
import network.palace.ridemanager.handlers.ride.ChunkStand;
import network.palace.ridemanager.utils.MovementUtil;
import org.bukkit.*;
import org.bukkit.entity.ArmorStand;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.EulerAngle;
import org.bukkit.util.Vector;

import java.util.*;

/**
 * Created by Marc on 3/10/17.
 */
public class AerialCarouselRide extends FlatRide {
    @Getter private double aerialRadius = 6.5;
    @Getter private double supportRadius = 4.5;
    @Getter private final boolean small;
    @Getter private FlatState state = FlatState.LOADING;
    private Location center;
    @Getter private boolean spawned = false;
    @Getter private List<Vehicle> vehicles = new ArrayList<>();
    @Getter @Setter private double speed = 0; //Full speed is 0.2
    @Getter @Setter private double heightSpeed = 0;
    @Getter private double height = 3;
    @Getter private double movein = 0.9;
    @Getter private double maxHeight;
    @Getter private double supportAngle = 45;
    @Getter private boolean canFly = false;
    @Getter private boolean started = false;
    private long startTime = 0;
    private long ticks = 0;
    private int taskID;

    public AerialCarouselRide(String name, String displayName, double delay, Location exit, Location center, CurrencyType currencyType,
                              int currencyAmount, int honorAmount, int achievementId) {
        this(name, displayName, delay, exit, center, currencyType, currencyAmount, honorAmount, achievementId, 6.5, 4.5);
    }

    public AerialCarouselRide(String name, String displayName, double delay, Location exit, Location center, CurrencyType currencyType,
                              int currencyAmount, int honorAmount, int achievementId, double aerialRadius, double supportRadius) {
        this(name, displayName, delay, exit, center, currencyType, currencyAmount, honorAmount, achievementId, aerialRadius, supportRadius, true);
    }

    public AerialCarouselRide(String name, String displayName, double delay, Location exit, Location center, CurrencyType currencyType,
                              int currencyAmount, int honorAmount, int achievementId, double aerialRadius, double supportRadius, boolean small) {
        this(name, displayName, delay, exit, center, currencyType, currencyAmount, honorAmount, achievementId, aerialRadius, supportRadius, small, 45, 3, 0.9);
    }

    public AerialCarouselRide(String name, String displayName, double delay, Location exit, Location center, CurrencyType currencyType,
                              int currencyAmount, int honorAmount, int achievementId, double aerialRadius, double supportRadius, boolean small, double angle, double height, double movein) {
        super(name, displayName, 16, delay, exit, currencyType, currencyAmount, honorAmount, achievementId);
        this.center = center.clone().add(0, -1.31, 0);
        this.aerialRadius = aerialRadius;
        this.supportRadius = supportRadius;
        this.supportAngle = angle;
        this.height = height;
        this.movein = movein;
        this.maxHeight = center.getY() + height;
        this.small = small;
        loadSurroundingChunks(center);
        spawn();
    }

    private void startTask() {
        stopTask();
        taskID = Core.runTaskTimer(RideManager.getInstance(), () -> {
            for (Vehicle v : getVehicles()) {
                CPlayer p = Core.getPlayerManager().getPlayer(v.getPassenger());
                if (p == null) continue;
                CPlayerActionBarManager ab = p.getActionBar();
                if (!canFly) {
                    ab.show(ChatColor.RED + "Flying Disabled");
                    continue;
                }
                switch (v.getFlyingState()) {
                    case HOVERING:
                        ab.show(ChatColor.YELLOW + "Hovering");
                        break;
                    case ASCENDING:
                        ab.show(ChatColor.GREEN + "▲ Ascending ▲");
                        break;
                    case DESCENDING:
                        ab.show(ChatColor.RED + "▼ Descending ▼");
                        break;
                }
            }
        }, 0L, 5L);
    }

    private void stopTask() {
        if (taskID != 0) Core.cancelTask(taskID);
    }

    public void spawn() {
        if (isSpawned()) return;
        startTask();
        World w = getWorld();
        double an = small ? 30 : 22.5;
        Location loc1 = getRelativeLocation(0, aerialRadius, this.center);
        Location loc2 = getRelativeLocation(an, aerialRadius, this.center);
        Location loc3 = getRelativeLocation(an * 2, aerialRadius, this.center);
        Location loc4 = getRelativeLocation(an * 3, aerialRadius, this.center);
        Location loc5 = getRelativeLocation(an * 4, aerialRadius, this.center);
        Location loc6 = getRelativeLocation(an * 5, aerialRadius, this.center);
        Location loc7 = getRelativeLocation(an * 6, aerialRadius, this.center);
        Location loc8 = getRelativeLocation(an * 7, aerialRadius, this.center);
        Location loc9 = getRelativeLocation(an * 8, aerialRadius, this.center);
        Location loc10 = getRelativeLocation(an * 9, aerialRadius, this.center);
        Location loc11 = getRelativeLocation(an * 10, aerialRadius, this.center);
        Location loc12 = getRelativeLocation(an * 11, aerialRadius, this.center);

        loc1.setYaw(90);
        loc2.setYaw(90 - (float) an);
        loc3.setYaw(90 - (float) an * 2);
        loc4.setYaw(90 - (float) an * 3);
        loc5.setYaw(90 - (float) an * 4);
        loc6.setYaw(90 - (float) an * 5);
        loc7.setYaw(90 - (float) an * 6);
        loc8.setYaw(90 - (float) an * 7);
        loc9.setYaw(90 - (float) an * 8);
        loc10.setYaw(90 - (float) an * 9);
        loc11.setYaw(90 - (float) an * 10);
        loc12.setYaw(90 - (float) an * 11);

        Vehicle h1 = new Vehicle(loc1, 0);
        Vehicle h2 = new Vehicle(loc2, an);
        Vehicle h3 = new Vehicle(loc3, an * 2);
        Vehicle h4 = new Vehicle(loc4, an * 3);
        Vehicle h5 = new Vehicle(loc5, an * 4);
        Vehicle h6 = new Vehicle(loc6, an * 5);
        Vehicle h7 = new Vehicle(loc7, an * 6);
        Vehicle h8 = new Vehicle(loc8, an * 7);
        Vehicle h9 = new Vehicle(loc9, an * 8);
        Vehicle h10 = new Vehicle(loc10, an * 9);
        Vehicle h11 = new Vehicle(loc11, an * 10);
        Vehicle h12 = new Vehicle(loc12, an * 11);

        if (!small) {
            Location loc13 = getRelativeLocation(an * 12, aerialRadius, this.center);
            Location loc14 = getRelativeLocation(an * 13, aerialRadius, this.center);
            Location loc15 = getRelativeLocation(an * 14, aerialRadius, this.center);
            Location loc16 = getRelativeLocation(an * 15, aerialRadius, this.center);
            Vehicle h13 = new Vehicle(loc13, an * 12);
            Vehicle h14 = new Vehicle(loc14, an * 13);
            Vehicle h15 = new Vehicle(loc15, an * 14);
            Vehicle h16 = new Vehicle(loc16, an * 15);
            this.vehicles = new LinkedList<>(Arrays.asList(h1, h2, h3, h4, h5, h6, h7, h8, h9, h10, h11, h12, h13, h14, h15, h16));
        } else {
            this.vehicles = new LinkedList<>(Arrays.asList(h1, h2, h3, h4, h5, h6, h7, h8, h9, h10, h11, h12));
        }

        this.spawned = true;
    }

    @Override
    public void start(List<CPlayer> riders) {
        if (started) return;
        new RideStartEvent(this).call();
        state = FlatState.RUNNING;
        riders.removeIf(player -> getOnRide().contains(player.getUniqueId()));
        int hc = 1;
        Vehicle h = getVehicle(hc);
        for (CPlayer tp : riders) {
            while (h != null && h.getPassenger() != null) {
                h = getVehicle(hc++);
            }
            if (h == null) {
                tp.sendMessage(ChatColor.RED + "We ran out of seats, sorry!");
                tp.teleport(getExit());
                continue;
            }
            if (tp.isSneaking()) {
                tp.sendMessage(ChatColor.RED + "You cannot board a ride while sneaking!");
                tp.teleport(getExit());
                continue;
            }
            h.addPassenger(tp);
            addToOnRide(tp.getUniqueId());
            h = getVehicle(hc++);
        }
        started = true;
        startTime = System.currentTimeMillis();
    }

    @Override
    public boolean handleEject(CPlayer player, boolean async) {
        UUID uuid = player.getUniqueId();
        for (Vehicle c : getVehicles()) {
            CPlayer p = Core.getPlayerManager().getPlayer(c.getPassenger());
            if (p != null && p.getUniqueId().equals(uuid)) {
                c.eject(async);
                if (!state.equals(FlatState.LOADING)) {
                    p.sendMessage(ChatColor.GREEN + "You were ejected from the ride!");
                }
                return true;
            }
        }
        return false;
    }

    @Override
    public void handleEject(CPlayer player, boolean async, boolean force) {
        handleEject(player, async);
    }

    @Override
    public boolean sitDown(CPlayer player, ArmorStand armorStand) {
        if (!state.equals(FlatState.LOADING) || getOnRide().size() >= getRiders() || getOnRide().contains(player.getUniqueId())) {
            return false;
        }
        UUID uuid = armorStand.getUniqueId();
        for (Vehicle v : getVehicles()) {
            ChunkStand stand = v.getStand();
            if (!stand.getStand().isPresent()) continue;
            if (stand.getStand().get().getUniqueId().equals(uuid) && v.addPassenger(player)) {
                addToOnRide(player.getUniqueId());
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean sitDown(CPlayer player, int entityId) {
        if (!state.equals(FlatState.LOADING) || getOnRide().size() >= getRiders() || getOnRide().contains(player.getUniqueId())) {
            return false;
        }
        for (Vehicle v : getVehicles()) {
            ChunkStand stand = v.getStand();
            if (!stand.getStand().isPresent()) continue;
            if (stand.getStand().get().getEntityId() == entityId) {
                addToOnRide(player.getUniqueId());
                Core.runTask(RideManager.getInstance(), () -> v.addPassenger(player));
                return true;
            }
        }
        return false;
    }

    @Override
    public void move() {
        if (isStarted()) {
            if (ticks != 0 && ticks % 20 == 0) {
                switch ((int) (ticks / 20)) {
                    case 0:
                        speed = 1.5;
                        for (UUID uuid : getOnRide()) {
                            CPlayer cp = Core.getPlayerManager().getPlayer(uuid);
                            if (cp == null || cp.getRank().getRankId() < Rank.CM.getRankId()) continue;
                            ItemStack i = cp.getInventory().getItem(4);
                            if (i == null || !i.getType().equals(Material.THIN_GLASS)) continue;
                            cp.performCommand("build");
                        }
                        break;
                    case 1:
                        speed = 1;
                        break;
                    case 2:
                        speed = 0.9;
                        break;
                    case 3:
                        speed = 0.8;
                        break;
                    case 4:
                        speed = 0.65;
                        break;
                    case 5:
                        speed = 0.525;
                        break;
                    case 6:
                        speed = 0.3125;
                        break;
                    case 7:
                        speed = 0.2;
                        canFly = true;
                        ItemStack item = ItemUtil.create(Material.LEVER, ChatColor.GREEN + "Ride Control");
                        for (UUID uuid : getOnRide()) {
                            CPlayer cp = Core.getPlayerManager().getPlayer(uuid);
                            if (cp == null) continue;
                            cp.getInventory().setItem(4, item);
                            cp.playSound(cp.getLocation(), Sound.ENTITY_ITEM_PICKUP, 1, 1);
                            cp.sendMessage(ChatColor.GREEN + "You can fly now, click the item in the middle of your hotbar to start flying!");
                        }
                        break;
                    case 75:
                        canFly = false;
                        getVehicles().stream().forEach(v -> v.setFlyingState(FlyingState.DESCENDING));
                        break;
                    case 82:
                        speed = 0.3;
                        break;
                    case 83:
                        speed = 0.4;
                        break;
                    case 84:
                        speed = 0.5;
                        break;
                    case 85:
                        speed = 0.6;
                        break;
                    case 86:
                        speed = 0.7;
                        break;
                    case 87:
                        speed = 0.8;
                        break;
                    case 88:
                        speed = 0.9;
                        break;
                    case 89:
                        speed = 1;
                        break;
                    case 90:
                        speed = 1.5;
                        break;
                    case 92:
                        speed = 2;
                        break;
                    case 93:
                        speed = 0;
                        break;
                    case 96:
                        ejectPlayers();
                        state = FlatState.LOADING;
                        started = false;
                        break;
                }
            }
            if (System.currentTimeMillis() - startTime >= 3000) {
                ticks++;
            }
        }

        if (!isSpawned()) return;

        Vector vel = new Vector(0, MovementUtil.getYMin(), 0);
        if (speed == 0) {
            for (Vehicle v : vehicles) {
                v.setVelocity(vel);
            }
            return;
        }

        double tableChange = 360 / (speed * 20 * 60);
        double supportChange = -Math.toRadians((supportAngle * 3) / (speed * 20 * 60));
        for (Vehicle v : getVehicles()) {
            double a = (v.getAngle() - tableChange) % 360;
            v.setAngle(a);

            ChunkStand vehicle = v.getStand();
            ChunkStand support = v.getSupport();

            Location l = vehicle.getLocation();
            Location l2 = support.getLocation();

            Location center = this.center.clone();
            Location supportCenter = this.center.clone().add(0, height / 2, 0);

            boolean vertMove = false;
            boolean up = false;
            FlyingState state = v.getFlyingState();
            if ((l.getY() > maxHeight && state.equals(FlyingState.ASCENDING)) || (l.getY() - 0.05 < center.getY() && state.equals(FlyingState.DESCENDING))) {
                v.setLastFlyingState(v.getFlyingState());
                v.setFlyingState(FlyingState.HOVERING);
                CPlayer p = Core.getPlayerManager().getPlayer(v.getPassenger());
                if (p != null) p.playSound(p.getLocation(), Sound.BLOCK_LEVER_CLICK, 1, 0.5f);
            }
            if (state.equals(FlyingState.ASCENDING) && l.getY() < maxHeight) {
                center.setY(l.getY() + 0.05);
                supportCenter.setY(l2.getY() + 0.025);
                up = true;
                vertMove = true;
            } else if ((state.equals(FlyingState.ASCENDING) && l.getY() >= maxHeight) || state.equals(FlyingState.HOVERING)) {
                center.setY(l.getY());
                supportCenter.setY(l2.getY());
            } else if (state.equals(FlyingState.DESCENDING) && l.getY() >= center.getY()) {
                center.setY(l.getY() - 0.05);
                supportCenter.setY(l2.getY() - 0.025);
                up = false;
                vertMove = true;
            }

            double r = aerialRadius;
            if (support.getHeadPose().getX() < 0) {
                r -= Math.abs(Math.toDegrees(support.getHeadPose().getX())) * 0.025;
            }
            Location n = getRelativeLocation(a, r, center);
            double radius = supportRadius - (((movein * (supportCenter.getY() / this.center.clone().add(0, height / 2, 0).getY())) - movein) * 38);
            Location n2 = getRelativeLocation(a, radius, supportCenter);
            n.setYaw((float) (l.getYaw() + tableChange) % 360);
            n2.setYaw((float) (l2.getYaw() + tableChange) % 360);

            vehicle.teleport(n);
            support.teleport(n2);

            if (vertMove) {
                if (up) {
                    support.setHeadPose(support.getHeadPose().add(supportChange, 0, 0));
                } else {
                    support.setHeadPose(support.getHeadPose().add(-supportChange, 0, 0));
                }
            }
        }
    }

    @Override
    public void despawn() {
        Bukkit.getScheduler().cancelTask(taskID);
        if (!isSpawned()) return;
        spawned = false;
        for (Vehicle v : getVehicles()) {
            v.despawn();
        }
    }

    private void ejectPlayers() {
        for (Vehicle c : getVehicles()) {
            c.eject(false);
        }
        clearOnRide();
    }

    public Location getCenter() {
        return center.clone().add(0, 1.31, 0);
    }

    private Vehicle getVehicle(int i) {
        return vehicles.get(i - 1);
    }

    public Vehicle getVehicle(UUID uuid) {
        for (Vehicle v : getVehicles()) {
            if (v.getPassenger() == null) continue;
            if (v.getPassenger().equals(uuid)) return v;
        }
        return null;
    }

    @Getter
    public class Vehicle {
        private ChunkStand stand;
        private ChunkStand support;
        @Setter private double angle;
        private Vector velocity = new Vector(0, MovementUtil.getYMin(), 0);

        @Setter private FlyingState flyingState = FlyingState.HOVERING;
        @Setter private FlyingState lastFlyingState = FlyingState.HOVERING;

        public Vehicle(Location standLoc, double angle) {
            this.stand = new ChunkStand(standLoc, true, new EulerAngle(0, Math.toRadians(-90), 0));
            this.stand.setHelmet(ItemUtil.create(Material.SHEARS, 1, 9));

            this.angle = angle;

            Location loc = getRelativeLocation(angle, supportRadius, center);
            loc.add(0, height / 2, 0);
            this.support = new ChunkStand(loc, false, new EulerAngle(Math.toRadians(supportAngle), Math.toRadians(-angle), 0));
            this.support.setHelmet(ItemUtil.create(Material.SHEARS, 1, 10));

            this.stand.spawn();
            this.support.spawn();
        }

        public boolean addPassenger(CPlayer player) {
            player.getScoreboard().toggleTags(true);
            return stand.addPassenger(player);
        }

        public UUID getPassenger() {
            return stand.getPassenger();
        }

        public void setVelocity(Vector v) {
            this.velocity = v;
            stand.setVelocity(v);
            support.setVelocity(v);
        }

        public void eject(boolean async) {
            if (getPassenger() != null) {
                CPlayer p = Core.getPlayerManager().getPlayer(getPassenger());
                if (p != null) {
                    eject(p, async);
                } else {
                    stand.getStand().ifPresent(s -> emptyStand(s, false));
                    removeFromOnRide(stand.getPassenger());
                }
            }
        }

        public boolean eject(CPlayer player, boolean async) {
            if (player == null) return false;
            boolean ejected = false;
            if (getPassenger() != null && getPassenger().equals(player.getUniqueId())) {
                stand.getStand().ifPresent(s -> emptyStand(s, async));
                removeFromOnRide(player.getUniqueId());
                player.getScoreboard().toggleTags(false);
                ejected = true;
            }
            return ejected;
        }

        public void despawn() {
            eject(false);
            stand.despawn();
            support.despawn();
        }

        public void chunkLoaded(Chunk c) {
            stand.chunkLoaded(c);
            support.chunkLoaded(c);
        }

        public void chunkUnloaded(Chunk c) {
            stand.chunkUnloaded(c);
            support.chunkUnloaded(c);
        }
    }

    public enum FlyingState {
        HOVERING, ASCENDING, DESCENDING
    }

    @Override
    public void onChunkLoad(Chunk chunk) {
        if (!isSpawned()) return;
        for (Vehicle v : vehicles) {
            v.chunkLoaded(chunk);
        }
    }

    @Override
    public void onChunkUnload(Chunk chunk) {
        if (!isSpawned()) return;
        for (Vehicle v : vehicles) {
            v.chunkUnloaded(chunk);
        }
    }

    @Override
    public boolean isRideStand(ArmorStand stand) {
        UUID uuid = stand.getUniqueId();
        for (Vehicle v : vehicles) {
            if (!v.getStand().isSpawned()) continue;
            if (v.getStand().getUniqueId().equals(uuid)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean isRideStand(int id) {
        for (Vehicle v : vehicles) {
            if (!v.getStand().isSpawned()) continue;
            if (v.getStand().getEntityId() == id) {
                return true;
            }
        }
        return false;
    }
}
