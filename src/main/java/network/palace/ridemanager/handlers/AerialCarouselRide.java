package network.palace.ridemanager.handlers;

import lombok.Getter;
import lombok.Setter;
import network.palace.core.player.CPlayer;
import network.palace.ridemanager.RideManager;
import org.bukkit.*;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.*;

/**
 * Created by Marc on 3/10/17.
 */
public class AerialCarouselRide extends Ride {
    private double aerialRadius = 6.5;
    private double supportRadius = 4.5;
    private final boolean small;
    @Getter private Location center;
    @Getter private boolean spawned = false;
    @Getter private List<Vehicle> vehicles = new ArrayList<>();
    @Getter @Setter private double speed = 0; //Full speed is 0.2
    @Getter @Setter private double heightSpeed = 0;
    @Getter private double height = 3;
    @Getter private double supportAngle = 45;
    @Getter private boolean started = false;

    public AerialCarouselRide(String name, String displayName, double delay, Location exit, Location center) {
        this(name, displayName, delay, exit, center, 6.5, 4.5);
    }

    public AerialCarouselRide(String name, String displayName, double delay, Location exit, Location center, double aerialRadius, double supportRadius) {
        this(name, displayName, delay, exit, center, aerialRadius, supportRadius, false);
    }

    public AerialCarouselRide(String name, String displayName, double delay, Location exit, Location center, double aerialRadius, double supportRadius, boolean small) {
        this(name, displayName, delay, exit, center, aerialRadius, supportRadius, 45, 3, small);
    }

    public AerialCarouselRide(String name, String displayName, double delay, Location exit, Location center, double aerialRadius, double supportRadius, double angle, double height, boolean small) {
        super(name, displayName, 16, delay, exit);
        this.center = center;
        this.aerialRadius = aerialRadius;
        this.supportRadius = supportRadius;
        this.supportAngle = angle;
        this.height = height;
        this.small = small;
        loadSurroundingChunks();
        spawn();
    }

    public void spawn() {
        if (isSpawned()) {
            return;
        }
        World w = this.center.getWorld();
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

        ItemStack i1 = new ItemStack(Material.STONE, 1);
        ItemStack i2 = new ItemStack(Material.DIRT, 1);
        ItemStack i3 = new ItemStack(Material.GRASS, 1);
        ItemStack i4 = new ItemStack(Material.BRICK, 1);
        ItemStack i5 = new ItemStack(Material.WOOL, 1);
        ItemStack i6 = new ItemStack(Material.CLAY, 1);


        ArmorStand a1 = w.spawn(loc1, ArmorStand.class);
        ArmorStand a2 = w.spawn(loc2, ArmorStand.class);
        ArmorStand a3 = w.spawn(loc3, ArmorStand.class);
        ArmorStand a4 = w.spawn(loc4, ArmorStand.class);
        ArmorStand a5 = w.spawn(loc5, ArmorStand.class);
        ArmorStand a6 = w.spawn(loc6, ArmorStand.class);
        ArmorStand a7 = w.spawn(loc7, ArmorStand.class);
        ArmorStand a8 = w.spawn(loc8, ArmorStand.class);
        ArmorStand a9 = w.spawn(loc9, ArmorStand.class);
        ArmorStand a10 = w.spawn(loc10, ArmorStand.class);
        ArmorStand a11 = w.spawn(loc11, ArmorStand.class);
        ArmorStand a12 = w.spawn(loc12, ArmorStand.class);

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

        a1.setHelmet(i1);
        a2.setHelmet(i2);
        a3.setHelmet(i3);
        a4.setHelmet(i4);
        a5.setHelmet(i5);
        a6.setHelmet(i6);
        a7.setHelmet(i2);
        a8.setHelmet(i2);
        a9.setHelmet(i3);
        a10.setHelmet(i4);
        a11.setHelmet(i5);
        a12.setHelmet(i6);

        double d2 = -Math.toRadians(an);
        double d3 = -Math.toRadians(an * 2);
        double d4 = -Math.toRadians(an * 3);
        double d5 = -Math.toRadians(an * 4);
        double d6 = -Math.toRadians(an * 5);
        double d7 = -Math.toRadians(an * 6);
        double d8 = -Math.toRadians(an * 7);
        double d9 = -Math.toRadians(an * 8);
        double d10 = -Math.toRadians(an * 9);
        double d11 = -Math.toRadians(an * 10);
        double d12 = -Math.toRadians(an * 11);
        double d13 = -Math.toRadians(an * 12);
        double d14 = -Math.toRadians(an * 13);
        double d15 = -Math.toRadians(an * 14);
        double d16 = -Math.toRadians(an * 15);

        a2.setHeadPose(a2.getHeadPose().add(0, d2, 0));
        a3.setHeadPose(a3.getHeadPose().add(0, d3, 0));
        a4.setHeadPose(a4.getHeadPose().add(0, d4, 0));
        a5.setHeadPose(a5.getHeadPose().add(0, d5, 0));
        a6.setHeadPose(a6.getHeadPose().add(0, d6, 0));
        a7.setHeadPose(a7.getHeadPose().add(0, d7, 0));
        a8.setHeadPose(a8.getHeadPose().add(0, d8, 0));
        a9.setHeadPose(a9.getHeadPose().add(0, d9, 0));
        a10.setHeadPose(a10.getHeadPose().add(0, d10, 0));
        a11.setHeadPose(a11.getHeadPose().add(0, d11, 0));
        a12.setHeadPose(a12.getHeadPose().add(0, d12, 0));

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
            ArmorStand a13 = w.spawn(loc13, ArmorStand.class);
            ArmorStand a14 = w.spawn(loc14, ArmorStand.class);
            ArmorStand a15 = w.spawn(loc15, ArmorStand.class);
            ArmorStand a16 = w.spawn(loc16, ArmorStand.class);
            a13.setGravity(false);
            a14.setGravity(false);
            a15.setGravity(false);
            a16.setGravity(false);
            a13.setHelmet(i3);
            a14.setHelmet(i4);
            a15.setHelmet(i5);
            a16.setHelmet(i6);
            a13.setHeadPose(a13.getHeadPose().add(0, d13, 0));
            a14.setHeadPose(a14.getHeadPose().add(0, d14, 0));
            a15.setHeadPose(a15.getHeadPose().add(0, d15, 0));
            a16.setHeadPose(a16.getHeadPose().add(0, d16, 0));
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
        List<Player> riders = new ArrayList<>();
        for (UUID uuid : riding) {
            Player tp = Bukkit.getPlayer(uuid);
            if (tp == null) {
                continue;
            }
            riders.add(tp);
        }
        int hc = 1;
        Vehicle h = getHorse(hc);
        for (Player tp : riders) {
            h.addPassenger(tp);
            tp.sendMessage(ChatColor.GREEN + "Ride starting in 3 seconds!");
            getOnRide().add(tp.getUniqueId());
            h = getHorse(hc++);
        }
        int taskID = Bukkit.getScheduler().runTaskTimer(RideManager.getInstance(), new Runnable() {
            int time = 0;

            @Override
            public void run() {
                switch (time) {
                    case 0:
                        speed = 1.5;
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
                        speed = 0.7;
                        break;
                    case 5:
                        speed = 0.6;
                        break;
                    case 6:
                        speed = 0.5;
                        break;
                    case 7:
                        speed = 0.4;
                        break;
                    case 8:
                        speed = 0.3;
                        break;
                    case 9:
                        speed = 0.2;
                        break;
                    case 52:
                        speed = 0.3;
                        break;
                    case 53:
                        speed = 0.4;
                        break;
                    case 54:
                        speed = 0.5;
                        break;
                    case 55:
                        speed = 0.6;
                        break;
                    case 56:
                        speed = 0.7;
                        break;
                    case 57:
                        speed = 0.8;
                        break;
                    case 58:
                        speed = 0.9;
                        break;
                    case 59:
                        speed = 1;
                        break;
                    case 60:
                        speed = 1.5;
                        break;
                    case 62:
                        speed = 2;
                        break;
                    case 63:
                        speed = 0;
                        break;
                    case 66:
                        ejectPlayers();
                        break;
                }
                time++;
            }
        }, 60L, 20L).getTaskId();
        Bukkit.getScheduler().runTaskLater(RideManager.getInstance(), new Runnable() {
            @Override
            public void run() {
                Bukkit.getScheduler().cancelTask(taskID);
            }
        }, 1400L);
    }

    @Override
    public boolean handleEject(CPlayer player) {
        for (Vehicle c : getVehicles()) {
            Player p = c.getPassenger();
            if (p != null && p.getUniqueId().equals(player.getUniqueId())) {
                getOnRide().remove(player.getUniqueId());
                c.eject();
                p.sendMessage(ChatColor.GREEN + "You were ejected from the ride!");
                return true;
            }
        }
        return false;
    }

    @Override
    public void move() {
        if (!isSpawned() || speed == 0) {
            return;
        }
        double tableChange = 360 / (speed * 20 * 60);
        double head = -Math.toRadians(tableChange);
        for (Vehicle c : getVehicles()) {
            double a = (c.getAngle() + tableChange) % 360;
            c.setAngle(a);
            ArmorStand v = c.getStand();
            ArmorStand s = c.getSupport();
            Location n = getRelativeLocation(a, aerialRadius, center);
            Location n2 = getRelativeLocation(a, supportRadius, center).add(0, height / 4, 0);
            teleport(v, n);
            teleport(s, n2);
            v.setHeadPose(v.getHeadPose().add(0, head, 0));
            s.setHeadPose(s.getHeadPose().add(0, head, 0));
        }
    }

    @Override
    public void despawn() {
        if (!isSpawned()) {
            return;
        }
        spawned = false;
        for (Vehicle h : getVehicles()) {
            h.despawn();
        }
    }

    public void loadSurroundingChunks() {
        Chunk c = center.getChunk();
        World w = c.getWorld();
        for (int x = c.getX() - 2; x < c.getX() + 4; x++) {
            for (int z = c.getZ() - 2; z < c.getZ() + 4; z++) {
                Chunk at = w.getChunkAt(x, z);
                if (!at.isLoaded()) {
                    at.load();
                }
            }
        }
    }

    private Vehicle getHorse(int i) {
        return vehicles.get(i - 1);
    }

    private void ejectPlayers() {
        for (Vehicle c : getVehicles()) {
            c.eject();
        }
    }

    private class Vehicle {
        @Getter private ArmorStand stand;
        @Getter @Setter private double angle;
        private double ticks;
        @Getter private ArmorStand support;

        public Vehicle(ArmorStand stand, double angle) {
            this.stand = stand;
            this.angle = angle;
            this.support = stand.getWorld().spawn(getRelativeLocation(angle, supportRadius, center).add(0, height / 4, 0), ArmorStand.class);
            support.setGravity(false);
            support.setHeadPose(support.getHeadPose().add(Math.toRadians(supportAngle), Math.toRadians(360 - angle), 0));
        }

        public double getTicks() {
            return ticks++;
        }

        public void addPassenger(Player player) {
            if (!stand.getPassengers().isEmpty()) {
                return;
            }
            stand.addPassenger(player);
        }

        public Player getPassenger() {
            if (stand == null || stand.getPassengers() == null || stand.getPassengers().isEmpty()) {
                return null;
            }
            Entity pass = stand.getPassengers().get(0);
            if (!(pass instanceof Player)) {
                return null;
            }
            return (Player) pass;
        }

        public void eject() {
            Player passenger = getPassenger();
            if (passenger != null) {
                stand.removePassenger(passenger);
                passenger.teleport(getExit());
            }
        }

        public void despawn() {
            eject();
            stand.remove();
            support.remove();
        }
    }
}
