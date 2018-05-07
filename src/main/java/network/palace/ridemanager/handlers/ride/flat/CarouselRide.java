package network.palace.ridemanager.handlers.ride.flat;

import lombok.Getter;
import lombok.Setter;
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
import org.bukkit.util.EulerAngle;
import org.bukkit.util.Vector;

import java.util.*;

public class CarouselRide extends FlatRide {
    private final int horseRadius1 = 7;
    private final int horseRadius2 = 5;
    private final double riderRadius = 0.395;
    private final double poleAngle = 95;
    private final double poleY;
    private final double heightSpeed = 0.2;
    @Getter private Location center;
    @Getter private List<Horse> horses = new ArrayList<>();

    public CarouselRide(String name, String displayName, double delay, Location exit, Location center, CurrencyType currencyType, int currencyAmount) {
        super(name, displayName, 12, delay, exit, currencyType, currencyAmount);
        this.center = center;
        this.poleY = center.getY() + 2.5;
        spawn();
    }

    public void spawn() {
        if (isSpawned()) return;

        Location loc1 = getRelativeLocation(0.0, 7.0, this.center);
        Location loc2 = getRelativeLocation(30.0, 7.0, this.center);
        Location loc3 = getRelativeLocation(60.0, 7.0, this.center);
        Location loc4 = getRelativeLocation(90.0, 7.0, this.center);
        Location loc5 = getRelativeLocation(120.0, 7.0, this.center);
        Location loc6 = getRelativeLocation(150.0, 7.0, this.center);
        Location loc7 = getRelativeLocation(180.0, 7.0, this.center);
        Location loc8 = getRelativeLocation(210.0, 7.0, this.center);
        Location loc9 = getRelativeLocation(240.0, 7.0, this.center);
        Location loc10 = getRelativeLocation(270.0, 7.0, this.center);
        Location loc11 = getRelativeLocation(300.0, 7.0, this.center);
        Location loc12 = getRelativeLocation(330.0, 7.0, this.center);

        Location loc13 = getRelativeLocation(15.0, 5.0, this.center);
        Location loc14 = getRelativeLocation(45.0, 5.0, this.center);
        Location loc15 = getRelativeLocation(75.0, 5.0, this.center);
        Location loc16 = getRelativeLocation(105.0, 5.0, this.center);
        Location loc17 = getRelativeLocation(135.0, 5.0, this.center);
        Location loc18 = getRelativeLocation(165.0, 5.0, this.center);
        Location loc19 = getRelativeLocation(195.0, 5.0, this.center);
        Location loc20 = getRelativeLocation(225.0, 5.0, this.center);
        Location loc21 = getRelativeLocation(255.0, 5.0, this.center);
        Location loc22 = getRelativeLocation(285.0, 5.0, this.center);
        Location loc23 = getRelativeLocation(315.0, 5.0, this.center);
        Location loc24 = getRelativeLocation(345.0, 5.0, this.center);

        loc1.setYaw(270);
        loc2.setYaw(240);
        loc3.setYaw(210);
        loc4.setYaw(180);
        loc5.setYaw(150);
        loc6.setYaw(120);
        loc7.setYaw(90);
        loc8.setYaw(60);
        loc9.setYaw(30);
        loc10.setYaw(0);
        loc11.setYaw(-30);
        loc12.setYaw(-60);

        loc13.setYaw(255);
        loc14.setYaw(225);
        loc15.setYaw(195);
        loc16.setYaw(165);
        loc17.setYaw(135);
        loc18.setYaw(105);
        loc19.setYaw(75);
        loc20.setYaw(45);
        loc21.setYaw(15);
        loc22.setYaw(-15);
        loc23.setYaw(-45);
        loc24.setYaw(-75);

        ItemStack i1 = new ItemStack(Material.SHEARS, 1, (byte) 1);
        ItemStack i2 = new ItemStack(Material.SHEARS, 1, (byte) 1);
        ItemStack i3 = new ItemStack(Material.SHEARS, 1, (byte) 1);
        ItemStack i4 = new ItemStack(Material.SHEARS, 1, (byte) 1);
        ItemStack i5 = new ItemStack(Material.SHEARS, 1, (byte) 1);
        ItemStack i6 = new ItemStack(Material.SHEARS, 1, (byte) 1);

        Horse h1 = new Horse(loc1, 0, false, i1);
        Horse h2 = new Horse(loc2, 30, false, i2);
        Horse h3 = new Horse(loc3, 60, false, i3);
        Horse h4 = new Horse(loc4, 90, false, i4);
        Horse h5 = new Horse(loc5, 120, false, i5);
        Horse h6 = new Horse(loc6, 150, false, i6);
        Horse h7 = new Horse(loc7, 180, false, i1);
        Horse h8 = new Horse(loc8, 210, false, i2);
        Horse h9 = new Horse(loc9, 240, false, i3);
        Horse h10 = new Horse(loc10, 270, false, i4);
        Horse h11 = new Horse(loc11, 300, false, i5);
        Horse h12 = new Horse(loc12, 330, false, i6);

        Horse h13 = new Horse(loc13, 15, true, i1);
        Horse h14 = new Horse(loc14, 45, true, i2);
        Horse h15 = new Horse(loc15, 75, true, i3);
        Horse h16 = new Horse(loc16, 105, true, i4);
        Horse h17 = new Horse(loc17, 135, true, i5);
        Horse h18 = new Horse(loc18, 165, true, i6);
        Horse h19 = new Horse(loc19, 195, true, i1);
        Horse h20 = new Horse(loc20, 225, true, i2);
        Horse h21 = new Horse(loc21, 255, true, i3);
        Horse h22 = new Horse(loc22, 285, true, i4);
        Horse h23 = new Horse(loc23, 315, true, i5);
        Horse h24 = new Horse(loc24, 345, true, i6);

        this.horses = new LinkedList<>(Arrays.asList(h1, h2, h3, h4, h5, h6, h7, h8, h9, h10, h11, h12, h13, h14, h15, h16, h17, h18, h19, h20, h21, h22, h23, h24));
        this.spawned = true;
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
        int horseNumber = 1;
        Horse h = getHorse(horseNumber);
        for (CPlayer tp : new ArrayList<>(riders)) {
            while (h != null) {
                h = getHorse(horseNumber++);
                if (h.addPassenger(tp)) {
                    getOnRide().add(tp.getUniqueId());
                    break;
                }
            }
        }
        started = true;
        startTime = System.currentTimeMillis();
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
                    case 53:
                        speed = 0.3;
                        break;
                    case 54:
                        speed = 0.4;
                        break;
                    case 55:
                        speed = 0.5;
                        break;
                    case 56:
                        speed = 0.6;
                        break;
                    case 57:
                        speed = 0.7;
                        break;
                    case 58:
                        speed = 0.8;
                        break;
                    case 59:
                        speed = 0.9;
                        break;
                    case 60:
                        speed = 1;
                        break;
                    case 61:
                        speed = 1.5;
                        break;
                    case 63:
                        speed = 2;
                        break;
                    case 64:
                        speed = 0;
                        UUID[] arr = getOnRide().toArray(new UUID[]{});
                        try {
                            rewardCurrency(arr);
                        } catch (Exception ignored) {
                        }
                        new RideEndEvent(this, arr).call();
                        break;
                    case 67:
                        ejectPlayers();
                        ticks = -1;
                        started = false;
                        state = FlatState.LOADING;
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
            for (Horse h : horses) {
                h.setVelocity(v);
            }
            return;
        }

        double tableChange = 360 / (speed * 20 * 60);
        for (Horse c : getHorses()) {
            double a = (c.getAngle() + tableChange) % 360;
            c.setAngle(a);

            ChunkStand s = c.getHorse();

            final float old = s.getYaw();

            Location n;
            if (c.isInside()) {
                n = getRelativeLocation(a, horseRadius2, center);
            } else {
                n = getRelativeLocation(a, horseRadius1, center);
            }

            double height = getHeight(c.getTicks(), c.isPositive());
            n.setY(height);

            n.setYaw((float) (old - tableChange));

            Location p = n.clone();
            p.setY(height + 2.5);
            c.getPole().teleport(getRelativeLocation(a + poleAngle, riderRadius, p));

            s.teleport(n);
        }
    }

    @Override
    public void despawn() {
        for (Horse h : horses) {
            h.despawn();
        }
    }

    @Override
    public boolean sitDown(CPlayer player, ArmorStand stand) {
        if (!state.equals(FlatState.LOADING) || getOnRide().size() >= getRiders() || getOnRide().contains(player.getUniqueId())) {
            return false;
        }
        UUID uuid = stand.getUniqueId();
        for (Horse h : getHorses()) {
            ChunkStand horse = h.getHorse();
            if (!horse.getStand().isPresent()) continue;
            if (horse.getStand().get().getUniqueId().equals(uuid) && h.addPassenger(player)) {
                getOnRide().add(player.getUniqueId());
                return true;
            }
        }
        return false;
    }

    @Override
    public void onChunkLoad(Chunk chunk) {
        if (!isSpawned()) return;
        for (Horse h : horses) {
            h.chunkLoaded(chunk);
        }
    }

    @Override
    public void onChunkUnload(Chunk chunk) {
        if (!isSpawned()) return;
        for (Horse h : horses) {
            h.chunkUnloaded(chunk);
        }
    }

    public double getHeight(double ticks, boolean positive) {
        double time = ticks / 20;
        double h = 0.5 * Math.sin(0.5 * Math.PI * time * heightSpeed);
        if (!positive) {
            h *= -1;
        }
        return h + center.getY();
    }

    private Horse getHorse(int i) {
        return horses.get(i - 1);
    }

    @Override
    public boolean handleEject(CPlayer player) {
        for (Horse h : horses) {
            if (h.getPassenger() == null || !h.getPassenger().equals(player.getUniqueId())) continue;

            getOnRide().remove(player.getUniqueId());
            h.eject(player);

            if (!state.equals(FlatState.LOADING)) {
                player.sendMessage(ChatColor.GREEN + "You were ejected from the ride!");
            }
            return true;
        }
        return false;
    }

    private void ejectPlayers() {
        for (Horse c : getHorses()) {
            c.eject();
        }
        getOnRide().clear();
    }

    private class Horse {
        @Getter private boolean positive;
        @Getter private boolean inside;
        @Getter private ChunkStand horse;
        @Getter private ChunkStand pole;
        @Getter @Setter private double angle;
        @Getter private Vector velocity = new Vector(0, MovementUtil.getYMin(), 0);
        private double ticks;

        public Horse(Location horseLoc, double angle, boolean inside, ItemStack helmet) {
            this.horse = new ChunkStand(horseLoc, true, new EulerAngle(0, Math.toRadians(90), 0));
            this.horse.setHelmet(helmet);

            this.angle = angle;
            if (inside) {
                this.positive = (angle % 60) == 15;
            } else {
                this.positive = (angle % 60) == 0;
            }
            this.inside = inside;

            Location loc = getRelativeLocation(angle + poleAngle, riderRadius, horseLoc);
            loc.setY(poleY);
            this.pole = new ChunkStand(loc, false, new EulerAngle(0, Math.toRadians(90), 0));
            this.pole.setHelmet(new ItemStack(Material.SHEARS, 1, (byte) 2));

            this.horse.spawn();
            this.pole.spawn();
        }

        public double getTicks() {
            return ticks++;
        }

        public boolean addPassenger(CPlayer player) {
            return horse.addPassenger(player);
        }

        public UUID getPassenger() {
            return horse.getPassenger();
        }

        public void setVelocity(Vector v) {
            this.velocity = v;
            horse.setVelocity(v);
            pole.setVelocity(v);
        }

        public void eject() {
            if (getPassenger() != null) {
                getOnRide().remove(horse.getPassenger());
                emptyStand(horse.getStand().get());
            }
        }

        public void eject(CPlayer player) {
            if (getPassenger() != null && getPassenger().equals(player.getUniqueId())) {
                getOnRide().remove(player.getUniqueId());
                emptyStand(horse.getStand().get());
            }
        }

        public void despawn() {
            eject();
            horse.despawn();
            pole.despawn();
        }

        public void chunkLoaded(Chunk c) {
            horse.chunkLoaded(c);
            pole.chunkLoaded(c);
        }

        public void chunkUnloaded(Chunk c) {
            horse.chunkUnloaded(c);
            pole.chunkUnloaded(c);
        }
    }
}
