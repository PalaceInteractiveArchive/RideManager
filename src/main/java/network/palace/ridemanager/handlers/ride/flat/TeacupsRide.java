package network.palace.ridemanager.handlers.ride.flat;

import lombok.Getter;
import lombok.Setter;
import network.palace.core.Core;
import network.palace.core.economy.CurrencyType;
import network.palace.core.player.CPlayer;
import network.palace.ridemanager.events.RideEndEvent;
import network.palace.ridemanager.events.RideStartEvent;
import network.palace.ridemanager.handlers.ride.ChunkStand;
import network.palace.ridemanager.utils.MovementUtil;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.ArmorStand;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import java.util.*;

public class TeacupsRide extends FlatRide {
    private final int tableRadius = 7;
    private final int cupRadius = 3;
    private final double riderRadius = 0.5;
    @Getter private Location center;
    private List<Table> tables = new ArrayList<>();

    public TeacupsRide(String name, String displayName, double delay, Location exit, Location center,
                       CurrencyType currencyType, int currencyAmount, int honorAmount, int achievementId) {
        super(name, displayName, 54, delay, exit, currencyType, currencyAmount, honorAmount, achievementId);
        this.center = center;
        spawn();
    }

    public void spawn() {
        if (isSpawned()) return;

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
        tables = new ArrayList<>(Arrays.asList(table1, table2, table3));
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
        Table t = getTable(table);
        for (CPlayer tp : new ArrayList<>(riders)) {
            while (t != null) {
                if (cup > 6) {
                    table++;
                    t = getTable(table);
                    cup = 1;
                }
                Cup c = t.getCups().get(cup - 1);
                if (c == null) {
                    tp.sendMessage(ChatColor.RED + "We ran out of seats, sorry!");
                    tp.teleport(getExit());
                    continue;
                }
                if (tp.getBukkitPlayer().isSneaking()) {
                    tp.sendMessage(ChatColor.RED + "You cannot board a ride while sneaking!");
                    tp.teleport(getExit());
                    continue;
                }
                if (c.addPassenger(tp)) {
                    getOnRide().add(tp.getUniqueId());
                    break;
                }
                cup++;
            }
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
    public void move() {
        if (isStarted()) {
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
                                c.spawn();
                            }
                        }
                        break;
                }
            }
            if (System.currentTimeMillis() - startTime >= 3000) {
                ticks++;
            }
        }

        if (!isSpawned()) return;

        Vector v = new Vector(0, MovementUtil.getYMin(), 0);
        if (speed == 0) {
            for (Table t : tables) {
                for (Cup c : t.getCups()) {
                    c.setVelocity(v);
                }
            }
            return;
        }

        double tableChange = 360 / (speed * 20 * 60);

        for (Table t : tables) {
            List<Cup> cups = t.getCups();

            double angle = (t.getAngle() + tableChange) % 360;
            t.setAngle(angle);

            Location next = getRelativeLocation(angle, tableRadius, center);
            t.setLocation(next);

            for (Cup c : cups) {
                double a = ((c.getAngle()) - (tableChange * 2)) % 360;
                c.setAngle(a);

                Location n = getRelativeLocation(a, cupRadius, next);
                n.setYaw((float) -a);

                c.setCenter(n);
                c.move(n, a);
                c.setVelocity(v);
            }
        }
    }

    @Override
    public void despawn() {
        if (!isSpawned()) return;
        spawned = false;
        for (Table t : tables) {
            t.despawn();
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
                Optional<ArmorStand> seat1 = c.getSeat1().getStand();
                Optional<ArmorStand> seat2 = c.getSeat2().getStand();
                Optional<ArmorStand> seat3 = c.getSeat3().getStand();

                if (((seat1.isPresent() && seat1.get().getUniqueId().equals(uuid)) ||
                        (seat2.isPresent() && seat2.get().getUniqueId().equals(uuid)) ||
                        (seat3.isPresent() && seat3.get().getUniqueId().equals(uuid)))
                        && c.addPassenger(player, uuid)) {
                    getOnRide().add(player.getUniqueId());
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public boolean handleEject(CPlayer player) {
        boolean removed = false;
        for (Table t : tables) {
            for (Cup c : t.getCups()) {
                if (c.eject(player)) {
                    removed = true;
                    if (!state.equals(FlatState.LOADING)) {
                        player.sendMessage(ChatColor.GREEN + "You were ejected from the ride!");
                    }
                    break;
                }
            }
        }
        return removed;
    }

    @Override
    public void handleEject(CPlayer player, boolean force) {
        handleEject(player);
    }

    public void ejectPlayers() {
        for (int i = 1; i <= 3; i++) {
            Table t = getTable(i);
            for (Cup c : t.getCups()) {
                c.eject();
            }
        }
    }

    @Override
    public void onChunkLoad(Chunk chunk) {
        if (!isSpawned()) return;
        for (int i = 1; i <= 3; i++) {
            Table t = getTable(i);
            t.chunkLoaded(chunk);
        }
    }

    @Override
    public void onChunkUnload(Chunk chunk) {
        if (!isSpawned()) return;
        for (int i = 1; i <= 3; i++) {
            Table t = getTable(i);
            t.chunkUnloaded(chunk);
        }
    }

    @Override
    public boolean isRideStand(ArmorStand stand) {
        UUID uuid = stand.getUniqueId();
        for (int i = 1; i <= 3; i++) {
            Table t = getTable(i);
            for (Cup c : t.getCups()) {
                if (c.isRideStand(uuid)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Get one of the three tables
     *
     * @param i the table number from 1 to 3
     * @return a Table object or null if none found
     */
    public Table getTable(int i) {
        return tables.get(i - 1);
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
            }
            cups.clear();
        }

        public void chunkLoaded(Chunk chunk) {
            for (Cup c : getCups()) {
                c.chunkLoaded(chunk);
            }
        }

        public void chunkUnloaded(Chunk chunk) {
            for (Cup c : getCups()) {
                c.chunkUnloaded(chunk);
            }
        }
    }

    private class Cup {
        @Getter @Setter private Location center;
        @Getter private ChunkStand seat1;
        @Getter private ChunkStand seat2;
        @Getter private ChunkStand seat3;
        @Getter private int table;
        @Getter @Setter private double angle;
        @Getter private Vector velocity = new Vector(0, MovementUtil.getYMin(), 0);
        private int num;

        public Cup(Location loc, int table, double angle, int num) {
            this.center = loc;
            this.table = table;
            this.angle = angle;
            this.num = num;

            Location l = getRelativeLocation(angle - 90, riderRadius, loc);
            l.setYaw((float) (270 - angle));
            l.setY(l.getY() + 0.2);

            Location l2 = getRelativeLocation(angle, riderRadius, loc);
            l2.setYaw((float) (180 - angle));
            l2.setY(l2.getY() + 0.2);

            Location l3 = getRelativeLocation(angle + 90, riderRadius, loc);
            l3.setYaw((float) (90 - angle));
            l3.setY(l3.getY() + 0.2);

            seat1 = new ChunkStand(l);
            seat2 = new ChunkStand(l2);
            seat3 = new ChunkStand(l3);
            spawn();
        }

        public void move(Location next, double angle) {
            Location l = getRelativeLocation(angle - 90, riderRadius, next);
            l.setYaw((float) (270 - this.angle));
            l.setY(l.getY() + 0.2);
            seat1.teleport(l);
//            seat1.setVelocity(velocity);

            Location l2 = getRelativeLocation(angle, riderRadius, next);
            l2.setYaw((float) (180 - this.angle));
            l2.setY(l2.getY() + 0.2);
            seat2.teleport(l2);
//            seat2.setVelocity(velocity);

            Location l3 = getRelativeLocation(angle + 90, riderRadius, next);
            l3.setYaw((float) (90 - this.angle));
            l3.setY(l3.getY() + 0.2);
            seat3.teleport(l3);
//            seat3.setVelocity(velocity);
        }

        public void chunkLoaded(Chunk c) {
            seat1.chunkLoaded(c);
            seat2.chunkLoaded(c);
            seat3.chunkLoaded(c);
        }

        public void chunkUnloaded(Chunk c) {
            seat1.chunkUnloaded(c);
            seat2.chunkUnloaded(c);
            seat3.chunkUnloaded(c);
        }

        public void setVelocity(Vector v) {
            this.velocity = v;
            seat1.setVelocity(v);
            seat2.setVelocity(v);
            seat3.setVelocity(v);
        }

        private void spawn() {
            seat1.spawn();
            seat1.setHelmet(new ItemStack(Material.SHEARS, 1, (short) (2 + num)));
            seat2.spawn();
            seat3.spawn();
        }

        public void despawn() {
            seat1.despawn();
            seat2.despawn();
            seat3.despawn();
        }

        public void removeExtraSeats() {
            Optional<ArmorStand> stand2 = seat2.getStand();
            Optional<ArmorStand> stand3 = seat3.getStand();

            if (stand2.isPresent() && stand2.get().getPassengers().isEmpty()) {
                seat2.despawn();
            }

            if (stand3.isPresent() && stand3.get().getPassengers().isEmpty()) {
                seat3.despawn();
            }
        }

        public boolean addPassenger(CPlayer player) {
            if (seat1.addPassenger(player)) return true;
            if (seat2.addPassenger(player)) return true;
            return seat3.addPassenger(player);
        }

        public boolean addPassenger(CPlayer player, UUID stand) {
            boolean added = false;
            if (seat1.getStand().isPresent() && seat1.getStand().get().getUniqueId().equals(stand) &&
                    seat1.addPassenger(player)) {
                added = true;
            } else if (seat2.getStand().isPresent() && seat2.getStand().get().getUniqueId().equals(stand) &&
                    seat2.addPassenger(player)) {
                added = true;
            } else if (seat3.getStand().isPresent() && seat3.getStand().get().getUniqueId().equals(stand) &&
                    seat3.addPassenger(player)) {
                added = true;
            }
            if (added) {
                player.getScoreboard().toggleTags(true);
                return true;
            }
            return false;
        }

        public List<UUID> getPassengers() {
            List<UUID> list = new ArrayList<>();
            list.add(seat1.getPassenger());
            list.add(seat2.getPassenger());
            list.add(seat3.getPassenger());
            return list;
        }

        public void eject() {
            if (seat1.getPassenger() != null) {
                getOnRide().remove(seat1.getPassenger());
                emptyStand(seat1.getStand().get());
                CPlayer p = Core.getPlayerManager().getPlayer(seat1.getPassenger());
                if (p != null) {
                    p.getScoreboard().toggleTags(false);
                }
            }
            if (seat2.getPassenger() != null) {
                getOnRide().remove(seat2.getPassenger());
                emptyStand(seat2.getStand().get());
                CPlayer p = Core.getPlayerManager().getPlayer(seat2.getPassenger());
                if (p != null) {
                    p.getScoreboard().toggleTags(false);
                }
            }
            if (seat3.getPassenger() != null) {
                getOnRide().remove(seat3.getPassenger());
                emptyStand(seat3.getStand().get());
                CPlayer p = Core.getPlayerManager().getPlayer(seat3.getPassenger());
                if (p != null) {
                    p.getScoreboard().toggleTags(false);
                }
            }
        }

        public boolean eject(CPlayer player) {
            if (player == null) return false;
            boolean ejected = false;
            if (seat1.getPassenger() != null && player.getUniqueId().equals(seat1.getPassenger())) {
                emptyStand(seat1.getStand().get());
                getOnRide().remove(player.getUniqueId());
                ejected = true;
            }
            if (seat2.getPassenger() != null && player.getUniqueId().equals(seat2.getPassenger())) {
                emptyStand(seat2.getStand().get());
                getOnRide().remove(player.getUniqueId());
                ejected = true;
            }
            if (seat3.getPassenger() != null && player.getUniqueId().equals(seat3.getPassenger())) {
                emptyStand(seat3.getStand().get());
                getOnRide().remove(player.getUniqueId());
                ejected = true;
            }
            if (ejected) {
                player.getScoreboard().toggleTags(false);
            }
            return ejected;
        }

        public boolean isRideStand(UUID uuid) {
            return (seat1.isSpawned() && seat1.getUniqueId().equals(uuid)) ||
                    (seat2.isSpawned() && seat2.getUniqueId().equals(uuid)) ||
                    (seat3.isSpawned() && seat3.getUniqueId().equals(uuid));
        }
    }
}
