package network.palace.ridemanager.handlers.ride.flat;

import lombok.Getter;
import lombok.Setter;
import network.palace.core.Core;
import network.palace.core.economy.CurrencyType;
import network.palace.core.player.CPlayer;
import network.palace.ridemanager.events.RideEndEvent;
import network.palace.ridemanager.events.RideStartEvent;
import network.palace.ridemanager.handlers.ride.Ride;
import network.palace.ridemanager.utils.MovementUtil;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import java.util.*;

/**
 * Created by Marc on 1/26/17.
 */
public class TeacupsRide extends Ride {
    private final int tableRadius = 7;
    private final int cupRadius = 3;
    private final double riderRadius = 0.5;
    @Getter private FlatState state = FlatState.LOADING;
    @Getter private Location center;
    @Getter private boolean spawned = false;
    private List<Table> tables = new ArrayList<>();
    @Getter @Setter private double speed = 0; //Full speed is 0.3
    @Getter private boolean started = false;
    private long startTime = 0;
    private long ticks = 0;

    public TeacupsRide(String name, String displayName, double delay, Location exit, Location center, CurrencyType currencyType, int currencyAmount) {
        super(name, displayName, 54, delay, exit, currencyType, currencyAmount);
        this.center = center;
        loadSurroundingChunks(center);
        spawn();
    }

    public void spawn() {
        if (isSpawned()) {
            return;
        }
//        World w = center.getWorld();
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

        loc11.setYaw(360);
        loc12.setYaw(300);
        loc13.setYaw(240);
        loc14.setYaw(180);
        loc15.setYaw(120);
        loc16.setYaw(60);

        loc21.setYaw(360);
        loc22.setYaw(300);
        loc23.setYaw(240);
        loc24.setYaw(180);
        loc25.setYaw(120);
        loc26.setYaw(60);

        loc31.setYaw(360);
        loc32.setYaw(300);
        loc33.setYaw(240);
        loc34.setYaw(180);
        loc35.setYaw(120);
        loc36.setYaw(60);

        /*a11.setGravity(false);
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
        a36.setGravity(false);*/

//        a11.setHelmet(h1);
//        a12.setHelmet(h2);
//        a13.setHelmet(h3);
//        a14.setHelmet(h4);
//        a15.setHelmet(h5);
//        a16.setHelmet(h6);
//        a21.setHelmet(h1);
//        a22.setHelmet(h2);
//        a23.setHelmet(h3);
//        a24.setHelmet(h4);
//        a25.setHelmet(h5);
//        a26.setHelmet(h6);
//        a31.setHelmet(h1);
//        a32.setHelmet(h2);
//        a33.setHelmet(h3);
//        a34.setHelmet(h4);
//        a35.setHelmet(h5);
//        a36.setHelmet(h6);
//        double d1 = -Math.toRadians(-90);
//        double d2 = -Math.toRadians(-30);
//        double d3 = -Math.toRadians(30);
//        double d4 = -Math.toRadians(90);
//        double d5 = -Math.toRadians(150);
//        double d6 = -Math.toRadians(210);
//        a11.setHeadPose(a11.getHeadPose().add(0, d1, 0));
//        a12.setHeadPose(a12.getHeadPose().add(0, d2, 0));
//        a13.setHeadPose(a13.getHeadPose().add(0, d3, 0));
//        a14.setHeadPose(a14.getHeadPose().add(0, d4, 0));
//        a15.setHeadPose(a15.getHeadPose().add(0, d5, 0));
//        a16.setHeadPose(a16.getHeadPose().add(0, d6, 0));
//        a21.setHeadPose(a21.getHeadPose().add(0, d1, 0));
//        a22.setHeadPose(a22.getHeadPose().add(0, d2, 0));
//        a23.setHeadPose(a23.getHeadPose().add(0, d3, 0));
//        a24.setHeadPose(a24.getHeadPose().add(0, d4, 0));
//        a25.setHeadPose(a25.getHeadPose().add(0, d5, 0));
//        a26.setHeadPose(a26.getHeadPose().add(0, d6, 0));
//        a31.setHeadPose(a31.getHeadPose().add(0, d1, 0));
//        a32.setHeadPose(a32.getHeadPose().add(0, d2, 0));
//        a33.setHeadPose(a33.getHeadPose().add(0, d3, 0));
//        a34.setHeadPose(a34.getHeadPose().add(0, d4, 0));
//        a35.setHeadPose(a35.getHeadPose().add(0, d5, 0));
//        a36.setHeadPose(a36.getHeadPose().add(0, d6, 0));
        Table table1 = new Table(loc1, new LinkedList<>(Arrays.asList(new Cup(loc11, 1, 0, 1),
                new Cup(loc12, 1, 60, 2), new Cup(loc13, 1, 120, 3),
                new Cup(loc14, 1, 180, 4), new Cup(loc15, 1, 240, 5),
                new Cup(loc16, 1, 300, 6))), 0);
        Table table2 = new Table(loc2, new LinkedList<>(Arrays.asList(new Cup(loc21, 2, 0, 1),
                new Cup(loc22, 2, 60, 2), new Cup(loc23, 2, 120, 3),
                new Cup(loc24, 2, 180, 4), new Cup(loc25, 2, 240, 5),
                new Cup(loc26, 2, 300, 6))), 120);
        Table table3 = new Table(loc3, new LinkedList<>(Arrays.asList(new Cup(loc31, 3, 0, 1),
                new Cup(loc32, 3, 60, 2), new Cup(loc33, 3, 120, 3),
                new Cup(loc34, 3, 180, 4), new Cup(loc35, 3, 240, 5),
                new Cup(loc36, 3, 300, 6))), 240);
        tables = new LinkedList<>(Arrays.asList(table1, table2, table3));
        spawned = true;
    }

    @Override
    public void start(List<CPlayer> riders) {
        if (started) return;
        new RideStartEvent(this).call();
        state = FlatState.RUNNING;
        for (CPlayer player : new ArrayList<>(riders)) {
            if (player == null) continue;
            if (getOnRide().contains(player.getUniqueId())) {
                riders.remove(player);
            }
        }
        int table = 1;
        int cup = 1;
        Table t = getTable(1);
        for (CPlayer tp : new ArrayList<>(riders)) {
            if (cup > 6) {
                table++;
                t = getTable(table);
            }
            Cup c = t.getCups().get(cup - 1);
            if (c.addPassenger(tp)) {
                getOnRide().add(tp.getUniqueId());
            }
            cup++;
        }
        started = true;
        startTime = System.currentTimeMillis();
        for (Table tab : tables) {
            for (Cup c : tab.getCups()) {
                c.removeExtraSeats();
            }
        }
    }

    @Override
    public boolean sitDown(CPlayer player, ArmorStand stand) {
        if (!state.equals(FlatState.LOADING) || getOnRide().size() >= getRiders() || getOnRide().contains(player.getUniqueId())) {
            return false;
        }
        UUID uuid = stand.getUniqueId();
        for (int i = 1; i <= 3; i++) {
            Table t = getTable(i);
            for (Cup c : t.getCups()) {

                if (c.getStand().isPresent() && c.getStand().get().getUniqueId().equals(uuid) ||
                        c.getStand().isPresent() && c.getSeat2().get().getUniqueId().equals(uuid) ||
                        c.getSeat3().isPresent() && c.getSeat3().get().getUniqueId().equals(uuid)) {
                    if (c.addPassenger(player, uuid)) {
                        getOnRide().add(player.getUniqueId());
                        return true;
                    }
                }
            }
        }
        return false;
    }

    @Override
    public boolean handleEject(CPlayer player) {
        for (Table t : tables) {
            for (Cup c : t.getCups()) {
                if (!c.getPassengers().contains(player.getUniqueId())) {
                    continue;
                }
                getOnRide().remove(player.getUniqueId());
                c.eject(player);
                if (!state.equals(FlatState.LOADING)) {
                    player.sendMessage(ChatColor.GREEN + "You were ejected from the ride!");
                }
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
                        UUID[] arr = getOnRide().toArray(new UUID[]{});
                        rewardCurrency(arr);
                        new RideEndEvent(this, arr).call();
                        break;
                    case 66:
                        ejectPlayers();
                        ticks = -1;
                        started = false;
                        state = FlatState.LOADING;
                        for (Table t : tables) {
                            for (Cup c : t.getCups()) {
                                c.spawnSeats();
                            }
                        }
                        break;
                }
            }
            if (System.currentTimeMillis() - startTime >= 3000) {
                ticks++;
            }
        }
        if (isSpawned() && speed == 0) {
            Vector v = new Vector(0, MovementUtil.getYMin(), 0);
            for (Table t : tables) {
                for (Cup c : t.getCups()) {
                    c.setVelocity(v);
//                    c.getStand().setVelocity(v);
//                    if (c.getSeat2() != null) c.getSeat2().setVelocity(v);
//                    if (c.getSeat3() != null) c.getSeat3().setVelocity(v);
                }
            }
        }
        if (!isSpawned() || speed == 0) {
            return;
        }
        double tableChange = 360 / (speed * 20 * 60);
//        double head = Math.toRadians(tableChange * 2);
        for (Table t : tables) {
            List<Cup> cups = t.getCups();
            double angle = (t.getAngle() + tableChange) % 360;
            t.setAngle(angle);
            Location next = getRelativeLocation(angle, tableRadius, center);
            t.setLocation(next);
            for (Cup c : cups) {
                double a = ((c.getAngle()) - (tableChange * 2)) % 360;
                c.setAngle(a);
//                final Location s = c.getCenter();
                Location n = getRelativeLocation(a, cupRadius, next);
//                s.setHeadPose(s.getHeadPose().add(0, head, 0));
                n.setYaw((float) -a);
                c.setCenter(n);
                c.move(n, a);
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

    private Table getTable(int i) {
        return tables.get(i - 1);
    }

    private void ejectPlayers() {
        for (Table t : tables) {
            for (Cup c : t.getCups()) {
                c.eject();
            }
        }
        getOnRide().clear();
    }

    private class Table {

        @Getter @Setter private Location location;
        @Getter private List<Cup> cups;
        @Getter @Setter private double angle;

        public Table(Location stand, List<Cup> cups, double angle) {
            this.location = stand;
            this.cups = cups;
            this.angle = angle;
        }

        public void despawn() {
            for (Cup c : cups) {
                c.eject();
                c.despawn();
//                c.getStand().remove();
//                if (c.getSeat2() != null) c.getSeat2().remove();
//                if (c.getSeat3() != null) c.getSeat3().remove();
            }
            cups.clear();
        }

    }

    private class Cup {

        @Getter @Setter private Location center;
        @Getter private Optional<ArmorStand> stand = Optional.empty();
        @Getter private Optional<ArmorStand> seat2 = Optional.empty();
        @Getter private Optional<ArmorStand> seat3 = Optional.empty();
        @Getter @Setter private boolean standSpawned = false;
        @Getter @Setter private boolean seat2Spawned = false;
        @Getter @Setter private boolean seat3Spawned = false;
        @Getter private int table;
        @Getter @Setter private double angle;
        @Getter private Vector velocity;
        private int num;

        public Cup(Location loc, int table, double angle, int num) {
            this.center = loc;
            this.table = table;
            this.angle = angle;
            this.num = num;
//            Location l = getRelativeLocation(angle - 90, riderRadius, loc);
//            l.setYaw((float) (270 - angle));
//            l.setY(loc.getY() + 0.2);
//            stand = lock(loc.getWorld().spawn(l, ArmorStand.class));
//            stand.setVisible(false);
//            stand.setHelmet(new ItemStack(Material.SHEARS, 1, (short) (2 + num)));
            spawnSeats();
        }

        public void chunkLoaded(Chunk c) {
            if (!c.isLoaded()) return;
            if (standSpawned && !stand.isPresent() && c.equals(getChunk(stand))) {
                Location l = getRelativeLocation(angle - 90, riderRadius, this.center);
                l.setYaw((float) (270 - angle));
                l.setY(this.center.getY() + 0.2);
                ArmorStand stand = lock(center.getWorld().spawn(l, ArmorStand.class));
                stand.setVelocity(velocity);
                stand.setVisible(false);
                stand.setHelmet(new ItemStack(Material.SHEARS, 1, (short) (2 + num)));
                this.stand = Optional.of(stand);
            }
            if (seat2Spawned && !seat2.isPresent() && c.equals(getChunk(seat2))) {
                Location l2 = getRelativeLocation(angle, riderRadius, center);
                l2.setYaw((float) (180 - angle));
                l2.setY(center.getY() + 0.2);
                ArmorStand seat2 = lock(center.getWorld().spawn(l2, ArmorStand.class));
                seat2.setVelocity(velocity);
                seat2.setVisible(false);
                this.seat2 = Optional.of(seat2);
            }
            if (seat3Spawned && !seat3.isPresent() && c.equals(getChunk(seat3))) {
                Location l3 = getRelativeLocation(angle + 90, riderRadius, center);
                l3.setYaw((float) (90 - angle));
                l3.setY(center.getY() + 0.2);
                ArmorStand seat3 = lock(center.getWorld().spawn(l3, ArmorStand.class));
                seat3.setVelocity(velocity);
                seat3.setVisible(false);
                this.seat3 = Optional.of(seat3);
            }
        }

        public void setVelocity(Vector v) {
            this.velocity = v;
            stand.ifPresent(s -> s.setVelocity(v));
            seat2.ifPresent(s -> s.setVelocity(v));
            seat3.ifPresent(s -> s.setVelocity(v));
        }

        public void move(Location n, double a) {
            if (getStand().isPresent()) {
                Location l = getRelativeLocation(a - 90, riderRadius, n);
                l.setYaw((float) -a + 270);
                l.setY(l.getY() + 0.2);
                teleport(stand.get(), l);
            }
            if (getSeat2().isPresent()) {
                Location l2 = getRelativeLocation(a, riderRadius, n);
                l2.setYaw((float) -a + 180);
                l2.setY(l2.getY() + 0.2);
                teleport(getSeat2().get(), l2);
            }
            if (getSeat3().isPresent()) {
                Location l3 = getRelativeLocation(a + 90, riderRadius, n);
                l3.setYaw((float) -a + 90);
                l3.setY(l3.getY() + 0.2);
                teleport(getSeat3().get(), l3);
            }
            setVelocity(new Vector(0, MovementUtil.getYMin(), 0));
        }

        private Chunk getChunk(Optional<ArmorStand> stand) {
            return stand.map(armorStand -> armorStand.getLocation().getChunk()).orElse(null);
        }

        public void spawnSeats() {
            standSpawned = true;
            seat2Spawned = true;
            seat3Spawned = true;
            if (standSpawned && !stand.isPresent()) {
                Location l = getRelativeLocation(angle - 90, riderRadius, this.center);
                l.setYaw((float) (270 - angle));
                l.setY(this.center.getY() + 0.2);
                ArmorStand stand = lock(center.getWorld().spawn(l, ArmorStand.class));
                stand.setVelocity(velocity);
                stand.setVisible(false);
                stand.setHelmet(new ItemStack(Material.SHEARS, 1, (short) (2 + num)));
                this.stand = Optional.of(stand);
            }
            if (seat2Spawned && !seat2.isPresent()) {
                Location l2 = getRelativeLocation(angle, riderRadius, center);
                l2.setYaw((float) (180 - angle));
                l2.setY(center.getY() + 0.2);
                ArmorStand seat2 = lock(center.getWorld().spawn(l2, ArmorStand.class));
                seat2.setVelocity(velocity);
                seat2.setVisible(false);
                this.seat2 = Optional.of(seat2);
            }
            if (seat3Spawned && !seat3.isPresent()) {
                Location l3 = getRelativeLocation(angle + 90, riderRadius, center);
                l3.setYaw((float) (90 - angle));
                l3.setY(center.getY() + 0.2);
                ArmorStand seat3 = lock(center.getWorld().spawn(l3, ArmorStand.class));
                seat3.setVelocity(velocity);
                seat3.setVisible(false);
                this.seat3 = Optional.of(seat3);
            }
        }

        public void despawn() {
            standSpawned = false;
            seat2Spawned = false;
            seat3Spawned = false;
            stand.ifPresent(Entity::remove);
            seat2.ifPresent(Entity::remove);
            seat3.ifPresent(Entity::remove);
            stand = Optional.empty();
            seat2 = Optional.empty();
            seat3 = Optional.empty();
        }

        public void removeExtraSeats() {
            if (seat2.isPresent() && seat2.get().getPassengers().isEmpty()) {
                seat2Spawned = false;
                seat2.get().remove();
            }
            if (seat3.isPresent() && seat3.get().getPassengers().isEmpty()) {
                seat3Spawned = false;
                seat3.get().remove();
            }
        }

        public boolean addPassenger(CPlayer player) {
            if (stand.isPresent() && stand.get().getPassengers().isEmpty()) {
                stand.get().addPassenger(player.getBukkitPlayer());
                return true;
            } else if (seat2.isPresent() && seat2.get().getPassengers().isEmpty()) {
                seat2.get().addPassenger(player.getBukkitPlayer());
                return true;
            } else if (seat3.isPresent() && seat3.get().getPassengers().isEmpty()) {
                seat3.get().addPassenger(player.getBukkitPlayer());
                return true;
            }
            return false;
        }

        public boolean addPassenger(CPlayer player, UUID stand) {
            if (this.stand.isPresent() && this.stand.get().getUniqueId().equals(stand) && this.stand.get().getPassengers().isEmpty()) {
                this.stand.get().addPassenger(player.getBukkitPlayer());
                return true;
            } else if (seat2.isPresent() && seat2.get().getUniqueId().equals(stand) && seat2.get().getPassengers().isEmpty()) {
                seat2.get().addPassenger(player.getBukkitPlayer());
                return true;
            } else if (seat3.isPresent() && seat3.get().getUniqueId().equals(stand) && seat3.get().getPassengers().isEmpty()) {
                seat3.get().addPassenger(player.getBukkitPlayer());
                return true;
            }
            return false;
        }

        public List<UUID> getPassengers() {
            List<UUID> list = new ArrayList<>();
            if (stand.isPresent() && !stand.get().getPassengers().isEmpty()) {
                list.add(stand.get().getPassengers().get(0).getUniqueId());
            }
            if (seat2.isPresent() && !seat2.get().getPassengers().isEmpty()) {
                list.add(seat2.get().getPassengers().get(0).getUniqueId());
            }
            if (seat3.isPresent() && !seat3.get().getPassengers().isEmpty()) {
                list.add(seat3.get().getPassengers().get(0).getUniqueId());
            }
            return list;
        }

        public void eject() {
            if (stand.isPresent() && !stand.get().getPassengers().isEmpty()) {
                emptyStand(stand.get());
            }
            if (seat2.isPresent() && !seat2.get().getPassengers().isEmpty()) {
                emptyStand(seat2.get());
            }
            if (seat3.isPresent() && !seat3.get().getPassengers().isEmpty()) {
                emptyStand(seat3.get());
            }
//            CPlayer passenger = getPassenger();
//            if (passenger != null) {
//                final Location playerLoc = passenger.getLocation();
//                stand.removePassenger(passenger.getBukkitPlayer());
//                Location loc = getExit();
//                if (state.equals(FlatState.LOADING)) {
//                    loc = stand.getLocation().add(0, 2, 0);
//                    loc.setYaw(playerLoc.getYaw());
//                    loc.setPitch(playerLoc.getPitch());
//                }
//                passenger.teleport(loc);
//            }
        }

        private void emptyStand(ArmorStand stand) {
            CPlayer p = Core.getPlayerManager().getPlayer(stand.getPassengers().get(0).getUniqueId());
            final Location pLoc = p.getLocation();
            stand.removePassenger(p.getBukkitPlayer());
            Location loc = getExit();
            if (state.equals(FlatState.LOADING)) {
                loc = stand.getLocation().add(0, 2, 0);
                loc.setYaw(pLoc.getYaw());
                loc.setPitch(pLoc.getPitch());
            }
            p.teleport(loc);
        }

        public void eject(CPlayer player) {
            if (stand.isPresent() && !stand.get().getPassengers().isEmpty() && stand.get().getPassengers().contains(player.getBukkitPlayer())) {
                emptyStand(stand.get());
            }
            if (seat2.isPresent() && !seat2.get().getPassengers().isEmpty() && seat2.get().getPassengers().contains(player.getBukkitPlayer())) {
                emptyStand(seat2.get());
            }
            if (seat3.isPresent() && !seat3.get().getPassengers().isEmpty() && seat3.get().getPassengers().contains(player.getBukkitPlayer())) {
                emptyStand(seat3.get());
            }
        }

    }

    @Override
    public void onChunkLoad(Chunk chunk) {
    }

    @Override
    public void onChunkUnload(Chunk chunk) {
    }
}
