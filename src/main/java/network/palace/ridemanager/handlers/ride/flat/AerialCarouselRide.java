package network.palace.ridemanager.handlers.ride.flat;

import lombok.Getter;
import lombok.Setter;
import network.palace.core.Core;
import network.palace.core.economy.CurrencyType;
import network.palace.core.player.CPlayer;
import network.palace.core.player.CPlayerActionBarManager;
import network.palace.core.player.Rank;
import network.palace.core.utils.ItemUtil;
import network.palace.core.utils.MathUtil;
import network.palace.ridemanager.RideManager;
import network.palace.ridemanager.events.RideStartEvent;
import network.palace.ridemanager.handlers.ride.Ride;
import network.palace.ridemanager.utils.MovementUtil;
import org.bukkit.*;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.EulerAngle;

import java.util.*;

/**
 * Created by Marc on 3/10/17.
 */
public class AerialCarouselRide extends Ride {
    @Getter private double aerialRadius = 6.5;
    @Getter private double supportRadius = 4.5;
    @Getter private final boolean small;
    @Getter private FlatState state = FlatState.LOADING;
    @Getter private Location center;
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

    public AerialCarouselRide(String name, String displayName, double delay, Location exit, Location center, CurrencyType currencyType, int currencyAmount, int honorAmount, int achievementId) {
        this(name, displayName, delay, exit, center, currencyType, currencyAmount, honorAmount, achievementId, 6.5, 4.5);
    }

    public AerialCarouselRide(String name, String displayName, double delay, Location exit, Location center, CurrencyType currencyType, int currencyAmount, int honorAmount, int achievementId, double aerialRadius, double supportRadius) {
        this(name, displayName, delay, exit, center, currencyType, currencyAmount, honorAmount, achievementId, aerialRadius, supportRadius, true);
    }

    public AerialCarouselRide(String name, String displayName, double delay, Location exit, Location center, CurrencyType currencyType, int currencyAmount, int honorAmount, int achievementId, double aerialRadius, double supportRadius, boolean small) {
        this(name, displayName, delay, exit, center, currencyType, currencyAmount, honorAmount, achievementId, aerialRadius, supportRadius, small, 45, 3, 0.9);
    }

    public AerialCarouselRide(String name, String displayName, double delay, Location exit, Location center, CurrencyType currencyType, int currencyAmount, int honorAmount, int achievementId, double aerialRadius, double supportRadius, boolean small, double angle, double height, double movein) {
        super(name, displayName, 16, delay, exit, currencyType, currencyAmount, honorAmount, achievementId);
        this.center = center;
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
                CPlayer p = v.getPassenger();
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

        ArmorStand a1 = lock(w.spawn(loc1, ArmorStand.class));
        ArmorStand a2 = lock(w.spawn(loc2, ArmorStand.class));
        ArmorStand a3 = lock(w.spawn(loc3, ArmorStand.class));
        ArmorStand a4 = lock(w.spawn(loc4, ArmorStand.class));
        ArmorStand a5 = lock(w.spawn(loc5, ArmorStand.class));
        ArmorStand a6 = lock(w.spawn(loc6, ArmorStand.class));
        ArmorStand a7 = lock(w.spawn(loc7, ArmorStand.class));
        ArmorStand a8 = lock(w.spawn(loc8, ArmorStand.class));
        ArmorStand a9 = lock(w.spawn(loc9, ArmorStand.class));
        ArmorStand a10 = lock(w.spawn(loc10, ArmorStand.class));
        ArmorStand a11 = lock(w.spawn(loc11, ArmorStand.class));
        ArmorStand a12 = lock(w.spawn(loc12, ArmorStand.class));

        a1.setVisible(false);
        a2.setVisible(false);
        a3.setVisible(false);
        a4.setVisible(false);
        a5.setVisible(false);
        a6.setVisible(false);
        a7.setVisible(false);
        a8.setVisible(false);
        a9.setVisible(false);
        a10.setVisible(false);
        a11.setVisible(false);
        a12.setVisible(false);

        a1.setGravity(false);
        a2.setGravity(false);
        a3.setGravity(false);
        a4.setGravity(false);
        a5.setGravity(false);
        a6.setGravity(false);
        a7.setGravity(false);
        a8.setGravity(false);
        a9.setGravity(false);
        a10.setGravity(false);
        a11.setGravity(false);
        a12.setGravity(false);

        EulerAngle pose = new EulerAngle(0, Math.toRadians(-90), 0);

        a1.setHeadPose(pose);
        a2.setHeadPose(pose);
        a3.setHeadPose(pose);
        a4.setHeadPose(pose);
        a5.setHeadPose(pose);
        a6.setHeadPose(pose);
        a7.setHeadPose(pose);
        a8.setHeadPose(pose);
        a9.setHeadPose(pose);
        a10.setHeadPose(pose);
        a11.setHeadPose(pose);
        a12.setHeadPose(pose);

        ItemStack item = ItemUtil.create(Material.SHEARS, 1, 9);

        a1.setHelmet(item);
        a2.setHelmet(item);
        a3.setHelmet(item);
        a4.setHelmet(item);
        a5.setHelmet(item);
        a6.setHelmet(item);
        a7.setHelmet(item);
        a8.setHelmet(item);
        a9.setHelmet(item);
        a10.setHelmet(item);
        a11.setHelmet(item);
        a12.setHelmet(item);

        Vehicle h1 = new Vehicle(a1, 0);
        Vehicle h2 = new Vehicle(a2, an);
        Vehicle h3 = new Vehicle(a3, an * 2);
        Vehicle h4 = new Vehicle(a4, an * 3);
        Vehicle h5 = new Vehicle(a5, an * 4);
        Vehicle h6 = new Vehicle(a6, an * 5);
        Vehicle h7 = new Vehicle(a7, an * 6);
        Vehicle h8 = new Vehicle(a8, an * 7);
        Vehicle h9 = new Vehicle(a9, an * 8);
        Vehicle h10 = new Vehicle(a10, an * 9);
        Vehicle h11 = new Vehicle(a11, an * 10);
        Vehicle h12 = new Vehicle(a12, an * 11);

        if (!small) {
            Location loc13 = getRelativeLocation(an * 12, aerialRadius, this.center);
            Location loc14 = getRelativeLocation(an * 13, aerialRadius, this.center);
            Location loc15 = getRelativeLocation(an * 14, aerialRadius, this.center);
            Location loc16 = getRelativeLocation(an * 15, aerialRadius, this.center);
            loc13.setYaw(90 - (float) an * 12);
            loc14.setYaw(90 - (float) an * 13);
            loc15.setYaw(90 - (float) an * 14);
            loc16.setYaw(90 - (float) an * 15);
            ArmorStand a13 = lock(w.spawn(loc13, ArmorStand.class));
            ArmorStand a14 = lock(w.spawn(loc14, ArmorStand.class));
            ArmorStand a15 = lock(w.spawn(loc15, ArmorStand.class));
            ArmorStand a16 = lock(w.spawn(loc16, ArmorStand.class));
            a13.setVisible(false);
            a14.setVisible(false);
            a15.setVisible(false);
            a16.setVisible(false);
            a13.setGravity(false);
            a14.setGravity(false);
            a15.setGravity(false);
            a16.setGravity(false);
            a13.setHeadPose(pose);
            a14.setHeadPose(pose);
            a15.setHeadPose(pose);
            a16.setHeadPose(pose);
            a13.setHelmet(item);
            a14.setHelmet(item);
            a15.setHelmet(item);
            a16.setHelmet(item);
            Vehicle h13 = new Vehicle(a13, an * 12);
            Vehicle h14 = new Vehicle(a14, an * 13);
            Vehicle h15 = new Vehicle(a15, an * 14);
            Vehicle h16 = new Vehicle(a16, an * 15);
            this.vehicles = new LinkedList<>(Arrays.asList(h1, h2, h3, h4, h5, h6, h7, h8, h9, h10, h11, h12, h13, h14, h15, h16));
        } else {
            this.vehicles = new LinkedList<>(Arrays.asList(h1, h2, h3, h4, h5, h6, h7, h8, h9, h10, h11, h12));
        }


        this.spawned = true;
    }

    /*
     @Override
    public void start(List<CPlayer> riders) {
        if (started) return;
        new RideStartEvent(this).call();
        state = FlatState.RUNNING;
        for (CPlayer player : new ArrayList<>(riders)) {
            if (getOnRide().contains(player.getUniqueId())) {
                riders.remove(player);
            }
        }
        int hc = 1;
        Horse h = getHorse(hc);
        for (CPlayer tp : riders) {
            while (h.getPassenger() != null) {
                h = getHorse(hc++);
                if (h == null) break;
            }
            if (h == null) break;
            h.addPassenger(tp.getBukkitPlayer());
            addToOnRide(tp.getUniqueId());
            h = getHorse(hc++);
        }
        started = true;
        startTime = System.currentTimeMillis();
    }
     */

    @Override
    public void start(List<CPlayer> riders) {
        if (started) return;
        new RideStartEvent(this).call();
        state = FlatState.RUNNING;
        for (CPlayer player : new ArrayList<>(riders)) {
            if (getOnRide().contains(player.getUniqueId())) {
                riders.remove(player);
            }
        }
        int hc = 1;
        Vehicle h = getVehicle(hc);
        for (CPlayer tp : riders) {
            while (h.getPassenger() != null) {
                h = getVehicle(hc++);
                if (h == null) break;
            }
            if (h == null) {
                tp.sendMessage(ChatColor.RED + "We ran out of seats, sorry!");
                tp.teleport(getExit());
                continue;
            }
            if (tp.getBukkitPlayer().isSneaking()) {
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
//        Bukkit.getScheduler().runTaskLater(RideManager.getInstance(), new Runnable() {
//            @Override
//            public void run() {
//                Bukkit.getScheduler().cancelTask(taskID);
//            }
//        }, 2000L);
    }

    @Override
    public boolean handleEject(CPlayer player, boolean async) {
        for (Vehicle c : getVehicles()) {
            CPlayer p = c.getPassenger();
            if (p != null && p.getUniqueId().equals(player.getUniqueId())) {
                removeFromOnRide(player.getUniqueId());
                c.eject(async);
                p.sendMessage(ChatColor.GREEN + "You were ejected from the ride!");
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
    public boolean sitDown(CPlayer player, ArmorStand stand) {
        if (!state.equals(FlatState.LOADING) || getOnRide().size() >= 18 || getOnRide().contains(player.getUniqueId())) {
            return false;
        }
        UUID uuid = stand.getUniqueId();
        for (Vehicle v : getVehicles()) {
            Optional<ArmorStand> s = v.getStand();
            if (!s.isPresent()) continue;
            if (s.get().getUniqueId().equals(uuid)) {
                v.addPassenger(player);
                addToOnRide(player.getUniqueId());
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean sitDown(CPlayer player, int entityId) {
        if (!state.equals(FlatState.LOADING) || getOnRide().size() >= 18 || getOnRide().contains(player.getUniqueId())) {
            return false;
        }
        for (Vehicle v : getVehicles()) {
            Optional<ArmorStand> s = v.getStand();
            if (!s.isPresent()) continue;
            if (s.get().getEntityId() == entityId) {
                Core.runTask(RideManager.getInstance(), () -> v.addPassenger(player));
                addToOnRide(player.getUniqueId());
                return true;
            }
        }
        return false;
    }

    @Override
    public void move() {
        if (started) {
            if (ticks != 0 && ticks % 20 == 0) {
                switch ((int) (ticks / 20)) {
                    case 0:
                        speed = 1.5;
                        for (UUID uuid : getOnRide()) {
                            CPlayer cp = Core.getPlayerManager().getPlayer(uuid);
                            if (cp == null || cp.getRank().getRankId() < Rank.MOD.getRankId()) continue;
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
        if (!isSpawned() || speed == 0) {
            return;
        }
        double tableChange = 360 / (speed * 20 * 60);
        double supportChange = -Math.toRadians((supportAngle * 3) / (speed * 20 * 60));
        for (Vehicle c : getVehicles()) {
            double a = (c.getAngle() - tableChange) % 360;
            c.setAngle(a);
            Optional<ArmorStand> st = c.getStand();
            Optional<ArmorStand> su = c.getSupport();
            if (!st.isPresent() || !su.isPresent()) {
                return;
            }
            ArmorStand vehicle = st.get();
            ArmorStand support = su.get();
            Location l = vehicle.getLocation();
            Location l2 = support.getLocation();
            Location center = this.center.clone();
            Location supportCenter = this.center.clone().add(0, height / 2, 0);
            boolean vertMove = false;
            boolean up = false;
            FlyingState state = c.getFlyingState();
            if ((l.getY() > maxHeight && state.equals(FlyingState.ASCENDING)) || (l.getY() - 0.05 < center.getY() && state.equals(FlyingState.DESCENDING))) {
                c.setLastFlyingState(c.getFlyingState());
                c.setFlyingState(FlyingState.HOVERING);
                CPlayer p = c.getPassenger();
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
            teleport(vehicle, n);
            teleport(support, n2);
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
        if (!isSpawned()) {
            return;
        }
        spawned = false;
        for (Vehicle v : getVehicles()) {
            v.despawn();
        }
    }

    private Vehicle getVehicle(int i) {
        return vehicles.get(i - 1);
    }

    private void ejectPlayers() {
        for (Vehicle c : getVehicles()) {
            c.eject(false);
        }
        clearOnRide();
    }

    public Vehicle getVehicle(UUID uuid) {
        for (Vehicle v : getVehicles()) {
            if (v.getPassenger() == null) continue;
            if (v.getPassenger().getUniqueId().equals(uuid)) {
                return v;
            }
        }
        return null;
    }

    public class Vehicle {
        private Optional<ArmorStand> stand = Optional.empty();
        private Optional<ArmorStand> support = Optional.empty();

        private boolean spawned = false;
        @Getter private World world;
        @Getter private double x, y, z;
        private int chunkX, chunkZ;
        @Getter @Setter private float yaw = 0;

        private UUID standID;
        @Getter @Setter private double angle;
        private UUID supportID;
        @Getter private UUID id = UUID.randomUUID();
        @Getter @Setter private FlyingState flyingState = FlyingState.HOVERING;
        @Getter @Setter private FlyingState lastFlyingState = FlyingState.HOVERING;

        public Vehicle(Location loc, double angle) {
            this.angle = angle;
            spawn(loc);

            ArmorStand stand = lock(loc.getWorld().spawn(loc, ArmorStand.class));
            this.stand = Optional.of(stand);
            ArmorStand support = lock(loc.getWorld().spawn(getRelativeLocation(angle, supportRadius, center).add(0, height / 2, 0), ArmorStand.class));
            ItemStack pole = ItemUtil.create(Material.SHEARS, 1, 10);
            support.setGravity(false);
            support.setVisible(false);
            support.setHeadPose(support.getHeadPose().add(Math.toRadians(supportAngle), Math.toRadians(360 - angle), 0));
            support.setHelmet(pole);
            this.support = Optional.of(support);
        }

        public void updateLocation(Location loc) {
            this.world = loc.getWorld();
            this.x = loc.getX();
            this.y = loc.getY();
            this.z = loc.getZ();
            chunkX = MathUtil.floor(x) >> 4;
            chunkZ = MathUtil.floor(z) >> 4;
        }

        public Location getLocation() {
            return new Location(world, x, y, z).add(0, MovementUtil.armorStandHeight, 0);
        }

        public Chunk getChunk() {
            return world.getChunkAt(chunkX, chunkZ);
        }

        public void spawn(Location loc) {
            setYaw(loc.getYaw());
            loc.setY(loc.getY() - MovementUtil.armorStandHeight);
            updateLocation(loc);
            spawned = true;

            if (!getChunk().isLoaded()) return;

            chunkLoaded(getChunk());
        }

        public void chunkLoaded(Chunk c) {
            if (!spawned || stand.isPresent() || !c.equals(getChunk()) || !c.isLoaded()) return;

            Location loc = getLocation();

            ArmorStand stand = lock(loc.getWorld().spawn(loc, ArmorStand.class));
            this.stand = Optional.of(stand);
            ArmorStand support = lock(loc.getWorld().spawn(getRelativeLocation(angle, supportRadius, center).add(0, height / 2, 0), ArmorStand.class));
            ItemStack pole = ItemUtil.create(Material.SHEARS, 1, 10);
            support.setGravity(false);
            support.setVisible(false);
            support.setHeadPose(support.getHeadPose().add(Math.toRadians(supportAngle), Math.toRadians(360 - angle), 0));
            support.setHelmet(pole);
            this.support = Optional.of(support);
        }

        public Vehicle(ArmorStand stand, double angle) {
            this.standID = stand.getUniqueId();
            this.angle = angle;
            ArmorStand support = lock(stand.getWorld().spawn(getRelativeLocation(angle, supportRadius, center).add(0, height / 2, 0), ArmorStand.class));
            this.supportID = support.getUniqueId();
            ItemStack pole = ItemUtil.create(Material.SHEARS, 1, 10);
            support.setGravity(false);
            support.setVisible(false);
            support.setHeadPose(support.getHeadPose().add(Math.toRadians(supportAngle), Math.toRadians(360 - angle), 0));
            support.setHelmet(pole);
        }

        public Optional<ArmorStand> getStand() {
            if (standID == null) return Optional.empty();
            return getWorld().getEntitiesByClass(ArmorStand.class).stream().filter(s -> s.getUniqueId().equals(standID)).findFirst();
        }

        public Optional<ArmorStand> getSupport() {
            if (supportID == null) return Optional.empty();
            return getWorld().getEntitiesByClass(ArmorStand.class).stream().filter(s -> s.getUniqueId().equals(supportID)).findFirst();
        }

        public void addPassenger(CPlayer player) {
            Optional<ArmorStand> s = getStand();
            if (!s.isPresent() || !s.get().getPassengers().isEmpty()) {
                return;
            }
            player.getScoreboard().toggleTags(true);
            s.get().addPassenger(player.getBukkitPlayer());
        }

        public CPlayer getPassenger() {
            Optional<ArmorStand> s = getStand();
            if (!s.isPresent()) {
                return null;
            }
            ArmorStand stand = s.get();
            if (stand == null || stand.getPassengers() == null || stand.getPassengers().isEmpty()) {
                return null;
            }
            Entity pass = stand.getPassengers().get(0);
            if (!(pass instanceof Player)) {
                return null;
            }
            return Core.getPlayerManager().getPlayer(pass.getUniqueId());
        }

        public void eject(boolean async) {
            flyingState = FlyingState.DESCENDING;
            Optional<ArmorStand> stand = getStand();
            CPlayer passenger = getPassenger();
            if (passenger == null) return;
            passenger.getScoreboard().toggleTags(false);
            stand.ifPresent(s -> s.removePassenger(passenger.getBukkitPlayer()));
            passenger.teleport(getExit());
            passenger.getInventory().setItem(4, ItemUtil.create(Material.THIN_GLASS, 1, ChatColor.GRAY +
                    "This Slot is Reserved for " + ChatColor.BLUE + "Ride Items", Arrays.asList(ChatColor.GRAY +
                    "This is for games such as " + ChatColor.GREEN + "Buzz", ChatColor.GREEN +
                    "Lightyear's Space Ranger Spin ", ChatColor.GRAY + "and " + ChatColor.YELLOW +
                    "Toy Story Midway Mania.")));
            removeFromOnRide(passenger.getUniqueId());
        }

        public void despawn() {
            eject(false);
            Optional<ArmorStand> s = getStand();
            Optional<ArmorStand> su = getSupport();
            s.ifPresent(Entity::remove);
            su.ifPresent(Entity::remove);
        }
    }

    public enum FlyingState {
        HOVERING, ASCENDING, DESCENDING
    }

    @Override
    public void onChunkLoad(Chunk chunk) {
    }

    @Override
    public void onChunkUnload(Chunk chunk) {
    }

    @Override
    public boolean isRideStand(ArmorStand stand) {
        return false;
    }

    @Override
    public boolean isRideStand(int id) {
        return false;
    }
}
