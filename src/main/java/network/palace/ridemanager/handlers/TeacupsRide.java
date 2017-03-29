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
 * Created by Marc on 1/26/17.
 */
public class TeacupsRide extends Ride {
    private final int tableRadius = 7;
    private final int cupRadius = 3;
    private final double riderRadius = 0.5;
    @Getter private Location center;
    @Getter private boolean spawned = false;
    private List<Table> tables = new ArrayList<>();
    @Getter @Setter private double speed = 0; //Full speed is 0.3
    @Getter private boolean started = false;

    public TeacupsRide(String name, String displayName, double delay, Location exit, Location center) {
        super(name, displayName, 18, delay, exit);
        this.center = center;
        loadSurroundingChunks();
        spawn();
    }

    public void spawn() {
        if (isSpawned()) {
            return;
        }
        World w = center.getWorld();
        Location loc1 = getRelativeLocation(0, tableRadius, center);
        Location loc2 = getRelativeLocation(120, tableRadius, center);
        Location loc3 = getRelativeLocation(240, tableRadius, center);
        Location loc11 = getRelativeLocation(0, cupRadius, loc1);
        Location loc12 = getRelativeLocation(60, cupRadius, loc1);
        Location loc13 = getRelativeLocation(120, cupRadius, loc1);
        Location loc14 = getRelativeLocation(180, cupRadius, loc1);
        Location loc15 = getRelativeLocation(240, cupRadius, loc1);
        Location loc16 = getRelativeLocation(300, cupRadius, loc1);
        Location loc21 = getRelativeLocation(0, cupRadius, loc2);
        Location loc22 = getRelativeLocation(60, cupRadius, loc2);
        Location loc23 = getRelativeLocation(120, cupRadius, loc2);
        Location loc24 = getRelativeLocation(180, cupRadius, loc2);
        Location loc25 = getRelativeLocation(240, cupRadius, loc2);
        Location loc26 = getRelativeLocation(300, cupRadius, loc2);
        Location loc31 = getRelativeLocation(0, cupRadius, loc3);
        Location loc32 = getRelativeLocation(60, cupRadius, loc3);
        Location loc33 = getRelativeLocation(120, cupRadius, loc3);
        Location loc34 = getRelativeLocation(180, cupRadius, loc3);
        Location loc35 = getRelativeLocation(240, cupRadius, loc3);
        Location loc36 = getRelativeLocation(300, cupRadius, loc3);
        ItemStack h1 = new ItemStack(Material.DIAMOND_SWORD, 1, (byte) 3);
        ItemStack h2 = new ItemStack(Material.DIAMOND_SWORD, 1, (byte) 3);
        ItemStack h3 = new ItemStack(Material.DIAMOND_SWORD, 1, (byte) 3);
        ItemStack h4 = new ItemStack(Material.DIAMOND_SWORD, 1, (byte) 3);
        ItemStack h5 = new ItemStack(Material.DIAMOND_SWORD, 1, (byte) 3);
        ItemStack h6 = new ItemStack(Material.DIAMOND_SWORD, 1, (byte) 3);
        ArmorStand a11 = w.spawn(loc11, ArmorStand.class);
        ArmorStand a12 = w.spawn(loc12, ArmorStand.class);
        ArmorStand a13 = w.spawn(loc13, ArmorStand.class);
        ArmorStand a14 = w.spawn(loc14, ArmorStand.class);
        ArmorStand a15 = w.spawn(loc15, ArmorStand.class);
        ArmorStand a16 = w.spawn(loc16, ArmorStand.class);
        ArmorStand a21 = w.spawn(loc21, ArmorStand.class);
        ArmorStand a22 = w.spawn(loc22, ArmorStand.class);
        ArmorStand a23 = w.spawn(loc23, ArmorStand.class);
        ArmorStand a24 = w.spawn(loc24, ArmorStand.class);
        ArmorStand a25 = w.spawn(loc25, ArmorStand.class);
        ArmorStand a26 = w.spawn(loc26, ArmorStand.class);
        ArmorStand a31 = w.spawn(loc31, ArmorStand.class);
        ArmorStand a32 = w.spawn(loc32, ArmorStand.class);
        ArmorStand a33 = w.spawn(loc33, ArmorStand.class);
        ArmorStand a34 = w.spawn(loc34, ArmorStand.class);
        ArmorStand a35 = w.spawn(loc35, ArmorStand.class);
        ArmorStand a36 = w.spawn(loc36, ArmorStand.class);
        a11.setGravity(false);
        a12.setGravity(false);
        a13.setGravity(false);
        a14.setGravity(false);
        a15.setGravity(false);
        a16.setGravity(false);
        a21.setGravity(false);
        a22.setGravity(false);
        a23.setGravity(false);
        a24.setGravity(false);
        a25.setGravity(false);
        a26.setGravity(false);
        a31.setGravity(false);
        a32.setGravity(false);
        a33.setGravity(false);
        a34.setGravity(false);
        a35.setGravity(false);
        a36.setGravity(false);
        a11.setHelmet(h1);
        a12.setHelmet(h2);
        a13.setHelmet(h3);
        a14.setHelmet(h4);
        a15.setHelmet(h5);
        a16.setHelmet(h6);
        a21.setHelmet(h1);
        a22.setHelmet(h2);
        a23.setHelmet(h3);
        a24.setHelmet(h4);
        a25.setHelmet(h5);
        a26.setHelmet(h6);
        a31.setHelmet(h1);
        a32.setHelmet(h2);
        a33.setHelmet(h3);
        a34.setHelmet(h4);
        a35.setHelmet(h5);
        a36.setHelmet(h6);
        double d2 = -Math.toRadians(60);
        double d3 = -Math.toRadians(120);
        double d4 = -Math.toRadians(180);
        double d5 = -Math.toRadians(240);
        double d6 = -Math.toRadians(300);
        a11.setHeadPose(a11.getHeadPose().add(0, 0, 0));
        a12.setHeadPose(a12.getHeadPose().add(0, d2, 0));
        a13.setHeadPose(a13.getHeadPose().add(0, d3, 0));
        a14.setHeadPose(a14.getHeadPose().add(0, d4, 0));
        a15.setHeadPose(a15.getHeadPose().add(0, d5, 0));
        a16.setHeadPose(a16.getHeadPose().add(0, d6, 0));
        a21.setHeadPose(a21.getHeadPose().add(0, 0, 0));
        a22.setHeadPose(a22.getHeadPose().add(0, d2, 0));
        a23.setHeadPose(a23.getHeadPose().add(0, d3, 0));
        a24.setHeadPose(a24.getHeadPose().add(0, d4, 0));
        a25.setHeadPose(a25.getHeadPose().add(0, d5, 0));
        a26.setHeadPose(a26.getHeadPose().add(0, d6, 0));
        a31.setHeadPose(a31.getHeadPose().add(0, 0, 0));
        a32.setHeadPose(a32.getHeadPose().add(0, d2, 0));
        a33.setHeadPose(a33.getHeadPose().add(0, d3, 0));
        a34.setHeadPose(a34.getHeadPose().add(0, d4, 0));
        a35.setHeadPose(a35.getHeadPose().add(0, d5, 0));
        a36.setHeadPose(a36.getHeadPose().add(0, d6, 0));
        Table table1 = new Table(loc1, new LinkedList<>(Arrays.asList(new Cup(a11, 1, 0),
                new Cup(a12, 1, 60), new Cup(a13, 1, 120), new Cup(a14, 1, 180),
                new Cup(a15, 1, 240), new Cup(a16, 1, 300))), 0);
        Table table2 = new Table(loc2, new LinkedList<>(Arrays.asList(new Cup(a21, 2, 0),
                new Cup(a22, 2, 60), new Cup(a23, 2, 120), new Cup(a24, 2, 180),
                new Cup(a25, 2, 240), new Cup(a26, 2, 300))), 120);
        Table table3 = new Table(loc3, new LinkedList<>(Arrays.asList(new Cup(a31, 3, 0),
                new Cup(a32, 3, 60), new Cup(a33, 3, 120), new Cup(a34, 3, 180),
                new Cup(a35, 3, 240), new Cup(a36, 3, 300))), 240);
        tables = new LinkedList<>(Arrays.asList(table1, table2, table3));
        spawned = true;
    }

    @Override
    public void start() {
        List<UUID> queue = getQueue();
        List<UUID> riding = new ArrayList<>();
        if (queue.size() < getRiders()) {
            riding.addAll(queue);
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
        int table = 1;
        int cup = 1;
        Table t = getTable(1);
        for (Player tp : riders) {
            if (cup > 6) {
                table++;
                t = getTable(table);
            }
            Cup c = t.getCups().get(cup - 1);
            c.addPassenger(tp);
            cup++;
            tp.sendMessage(ChatColor.GREEN + "Ride starting in 3 seconds!");
            getOnRide().add(tp.getUniqueId());
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
        for (Table t : tables) {
            for (Cup c : t.getCups()) {
                Player p = c.getPassenger();
                if (p != null && p.getUniqueId().equals(player.getUniqueId())) {
                    getOnRide().remove(player.getUniqueId());
                    c.eject();
                    p.sendMessage(ChatColor.GREEN + "You were ejected from the ride!");
                    return true;
                }
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
        double head = Math.toRadians(tableChange * 2);
        for (Table t : tables) {
            Location loc = t.getLocation();
            List<Cup> cups = t.getCups();
            double angle = (t.getAngle() + tableChange) % 360;
            t.setAngle(angle);
            Location next = getRelativeLocation(angle, tableRadius, center);
            t.setLocation(next);
            for (Cup c : cups) {
                double a = ((c.getAngle()) - (tableChange * 2)) % 360;
                c.setAngle(a);
                ArmorStand s = c.getStand();
                Location n = getRelativeLocation(a, cupRadius, next);
                s.setHeadPose(s.getHeadPose().add(0, head, 0));
                ArmorStand rider = c.getRider();
                if (rider != null) {
                    Location l = getRelativeLocation(a, riderRadius, n);
                    l.setYaw((float) -a);
                    teleport(rider, l);
                }
                teleport(s, n);
            }
        }
    }

    @Override
    public void despawn() {
        if (!isSpawned()) {
            return;
        }
        spawned = false;
        for (Table t : tables) {
            t.despawn();
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

    private Table getTable(int i) {
        return tables.get(i - 1);
    }

    private void ejectPlayers() {
        for (Table t : tables) {
            for (Cup c : t.getCups()) {
                if (c.getRider() != null) {
                    c.eject();
                }
            }
        }
    }

    private class Table {
        @Getter @Setter private Location location;
        @Getter private List<Cup> cups = new ArrayList<>();
        @Getter @Setter private double angle;

        public Table(Location stand, List<Cup> cups, double angle) {
            this.location = stand;
            this.cups = cups;
            this.angle = angle;
        }

        public void despawn() {
            for (Cup c : cups) {
                c.getStand().remove();
                c.eject();
                if (c.getRider() != null) {
                    c.getRider().remove();
                }
            }
            cups.clear();
        }
    }

    private class Cup {
        @Getter private ArmorStand stand;
        @Getter private int table;
        @Getter @Setter private double angle;
        @Getter private ArmorStand rider;

        public Cup(ArmorStand stand, int table, double angle) {
            this.stand = stand;
            this.table = table;
            this.angle = angle;
        }

        public void addPassenger(Player player) {
            if (rider != null) {
                return;
            }
            Table t = TeacupsRide.this.getTable(table);
            Location l = getRelativeLocation(angle, riderRadius, stand.getLocation());
            l.setYaw((float) angle);
            rider = stand.getWorld().spawn(l, ArmorStand.class);
            rider.setGravity(false);
            rider.addPassenger(player);
        }

        public Player getPassenger() {
            if (rider == null || rider.getPassengers() == null || rider.getPassengers().isEmpty()) {
                return null;
            }
            Entity pass = rider.getPassengers().get(0);
            if (!(pass instanceof Player)) {
                return null;
            }
            return (Player) pass;
        }

        public void eject() {
            Player passenger = getPassenger();
            if (passenger != null) {
                rider.removePassenger(passenger);
                passenger.teleport(getExit());
                rider.remove();
                rider = null;
            }
        }
    }
}
