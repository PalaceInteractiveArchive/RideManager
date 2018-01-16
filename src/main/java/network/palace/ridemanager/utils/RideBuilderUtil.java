package network.palace.ridemanager.utils;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import network.palace.core.Core;
import network.palace.core.player.CPlayer;
import network.palace.core.player.CPlayerParticlesManager;
import network.palace.core.utils.ItemUtil;
import network.palace.ridemanager.handlers.BuildSession;
import network.palace.ridemanager.handlers.actions.*;
import network.palace.ridemanager.handlers.ride.file.Cart;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.entity.ArmorStand;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

/**
 * @author Marc
 * @since 8/10/17
 */
public class RideBuilderUtil {
    private HashMap<UUID, BuildSession> sessions = new HashMap<>();

    public RideBuilderUtil() {
        Core.runTaskTimer(new Runnable() {
            @Override
            public void run() {
                for (BuildSession session : new ArrayList<>(sessions.values())) {
                    CPlayer player = Core.getPlayerManager().getPlayer(session.getUuid());
                    if (player == null) {
                        try {
                            session.save();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        removeSession(session.getUuid());
                        continue;
                    }
                    CPlayerParticlesManager part = player.getParticles();
                    List<RideAction> actions = session.getActions();
                    if (session.isShowArmorStands()) {
                        HashMap<Location, ArmorStand> stands = session.getStands();
                        MoveAction lastAction = (MoveAction) actions.stream().filter(a -> a instanceof MoveAction).findFirst().get();
                        for (RideAction action : actions) {
                            if (action instanceof FakeExitAction) {
                                FakeExitAction act = (FakeExitAction) action;
                                Location loc = lastAction.getFinalLocation();
                                ArmorStand stand = getStand(stands, loc);
                                stand.setCustomName(ChatColor.GREEN + "Exit");
                            } else if (action instanceof InclineAction) {
                                InclineAction act = (InclineAction) action;
                            } else if (action instanceof FakeRotateAction) {
                                FakeRotateAction act = (FakeRotateAction) action;
                                Location loc = lastAction.getFinalLocation();
                                ArmorStand stand = getStand(stands, loc);
                                stand.setCustomName(ChatColor.GREEN + "Rotate to " + act.getAngle() + " degrees over " + act.getTicks() + " ticks");
                            } else if (action instanceof FakeSpawnAction) {
                                FakeSpawnAction act = (FakeSpawnAction) action;

                            } else if (action instanceof SpeedAction) {
                                SpeedAction act = (SpeedAction) action;

                            } else if (action instanceof FakeStraightAction) {
                                FakeStraightAction act = (FakeStraightAction) action;

                            } else if (action instanceof FakeTeleportAction) {
                                FakeTeleportAction act = (FakeTeleportAction) action;

                            } else if (action instanceof FakeTurnAction) {
                                FakeTurnAction act = (FakeTurnAction) action;

                            } else if (action instanceof FakeWaitAction) {
                                FakeWaitAction act = (FakeWaitAction) action;
                            }
                            if (action instanceof MoveAction) {
                                lastAction = (MoveAction) action;
                            }
                        }
                    }
                    for (RideAction action : actions) {
                        if (action instanceof FakeWaitAction) {
                            continue;
                        }
                    }
                }
            }

            private void particle(CPlayerParticlesManager part, Location loc) {
                part.send(loc, Particle.REDSTONE, 1);
            }

            private ArmorStand getStand(HashMap<Location, ArmorStand> stands, Location loc) {
                ArmorStand stand;
                if (stands.get(loc) == null) {
                    stand = loc.getWorld().spawn(loc.add(0, -1.87, 0), ArmorStand.class);
                    stand.setCustomNameVisible(true);
                    stand.setGravity(false);
                    stand.setVisible(true);
                    stand.setArms(false);
                    stand.setBasePlate(false);
                    stands.put(loc, stand);
                } else {
                    stand = stands.get(loc);
                }
                return stand;
            }
        }, 0L, 20L);
    }

    public BuildSession getSession(CPlayer player) {
        return sessions.get(player.getUniqueId());
    }

    public BuildSession getSession(UUID uuid) {
        return sessions.get(uuid);
    }

    public BuildSession newSession(CPlayer player) {
        BuildSession session = new BuildSession(player.getUniqueId());
        sessions.put(player.getUniqueId(), session);
        return session;
    }

    public BuildSession loadSession(CPlayer player, File file) {
        BuildSession session = newSession(player);
        player.sendMessage(ChatColor.GREEN + "Session created, loading actions now...");
        session.load(file);
        return session;
    }

    public BuildSession removeSession(UUID uuid) {
        BuildSession session = sessions.remove(uuid);
        List<ArmorStand> stands = new ArrayList<>(session.getStands().values());
        for (ArmorStand stand : stands) {
            stand.remove();
        }
        return session;
    }

    public void moveEvent(CPlayer player, Location from, Location to) {
        BuildSession session = getSession(player);
        if (session == null) return;
        RideAction a = session.getCurrentAction();
        if (a == null || !(a instanceof MoveAction) || a instanceof RotateAction) return;
        MoveAction m = (MoveAction) a;
        Vector diff;
        if (session.isChangeY()) {
            diff = new Vector(0, to.getY() - from.getY(), 0);
        } else if (session.isSneaking()) {
            diff = to.toVector().subtract(from.toVector());
        } else {
            return;
        }
        MoveAction newAction = changeLocation(m, diff);
        if (newAction == null) return;
        session.setCurrentAction(newAction);
    }

    public void toggleShift(CPlayer player, boolean sneak) {
        BuildSession session = getSession(player);
        if (session == null) return;
        session.setSneaking(sneak);
    }

    public MoveAction changeLocation(MoveAction a, Vector v) {
        if (a instanceof ExitAction) {
            ExitAction act = (ExitAction) a;
            return new ExitAction(act.getTo().add(v));
        } else if (a instanceof SpawnAction) {
            SpawnAction act = (SpawnAction) a;
            return new SpawnAction(act.getLoc().add(v), act.getSpeed(), act.getYaw());
        } else if (a instanceof StraightAction) {
            StraightAction act = (StraightAction) a;
            return new StraightAction(act.getTo().add(v));
        } else if (a instanceof TeleportAction) {
            TeleportAction act = (TeleportAction) a;
            return new TeleportAction(act.getTo().add(v));
        } else if (a instanceof TurnAction) {
            TurnAction act = (TurnAction) a;
            return new TurnAction(act.getOrigin().add(v), act.getAngle());
        }
        return null;
    }

    public interface ConfirmCallback {

        /**
         * Called when the confirm is executed.
         *
         * @param uuid the uuid
         */
        void done(UUID uuid);
    }

    @AllArgsConstructor
    @Getter
    public enum BlockAction {
        SPAWN(Material.STAINED_CLAY, (byte) 5), STRAIGHT(Material.STAINED_CLAY, (byte) 4), TURN(Material.STAINED_CLAY, (byte) 14),
        ROTATE(Material.STAINED_CLAY, (byte) 1), WAIT(Material.STAINED_CLAY, (byte) 13), INCLINE(Material.STAINED_CLAY, (byte) 3),
        DECLINE(Material.STAINED_CLAY, (byte) 11), TELEPORT(Material.STAINED_CLAY, (byte) 9), EXIT(Material.STAINED_CLAY, (byte) 15);
        private final Material type;

        private final byte data;

        public ItemStack getItem() {
            return ItemUtil.create(type, 1, data);
        }

        @SuppressWarnings("deprecation")
        public static BlockAction fromBlock(Block b) {
            for (BlockAction a : BlockAction.values()) {
                if (a.type.equals(b.getType()) && a.data == b.getData()) {
                    return a;
                }
            }
            return null;
        }

        public static ItemStack[] getItems() {
            ItemStack[] array = new ItemStack[]{};
            int i = 0;
            for (BlockAction a : BlockAction.values()) {
                if (a.equals(SPAWN)) continue;
                array[i] = a.getItem();
                i++;
            }
            return array;
        }

        public RideAction newAction() {
            switch (this) {
                case SPAWN:
                    return new FakeSpawnAction();
                case STRAIGHT:
                    return new FakeStraightAction();
                case TURN:
                    return new FakeTurnAction();
                case ROTATE:
                    return new FakeRotateAction();
                case WAIT:
                    return new FakeWaitAction();
                case INCLINE:
                    return null;
                case DECLINE:
                    return null;
                case TELEPORT:
                    return new FakeTeleportAction();
                case EXIT:
                    return new FakeExitAction();
            }
            return null;
        }
    }

    @Getter
    @Setter
    public static class FakeSpawnAction extends FakeAction {
        private Location location;
        private double speed;
        private float yaw;

        public FakeSpawnAction() {
            super(true);
        }

        @Override
        public RideAction duplicate() {
            return new SpawnAction(location, speed, yaw);
        }

        @Override
        public String toString() {
            return "";
        }
    }

    @Getter
    @Setter
    public static class FakeStraightAction extends FakeAction {
        private Location to;

        public FakeStraightAction() {
            super(true);
        }

        @Override
        public RideAction duplicate() {
            return new network.palace.ridemanager.handlers.actions.StraightAction(to);
        }

        @Override
        public String toString() {
            return "";
        }
    }

    @Getter
    @Setter
    public static class FakeTurnAction extends FakeAction {
        private Location origin;
        private int angle;

        public FakeTurnAction() {
            super(true);
        }

        @Override
        public RideAction duplicate() {
            return new network.palace.ridemanager.handlers.actions.TurnAction(origin, angle);
        }

        @Override
        public String toString() {
            return "";
        }
    }

    @Getter
    @Setter
    public static class FakeRotateAction extends FakeAction {
        private int angle;
        private boolean rightTurn;
        private long ticks;

        public FakeRotateAction() {
            super(true);
        }

        @Override
        public RideAction duplicate() {
            return new network.palace.ridemanager.handlers.actions.RotateAction(angle, rightTurn, ticks);
        }

        @Override
        public String toString() {
            return "";
        }
    }

    @Getter
    @Setter
    public static class FakeWaitAction extends FakeAction {
        private long ticks;

        public FakeWaitAction() {
            super(true);
        }

        @Override
        public RideAction duplicate() {
            return new network.palace.ridemanager.handlers.actions.WaitAction(ticks);
        }

        @Override
        public String toString() {
            return "";
        }
    }

    @Getter
    @Setter
    public static class FakeTeleportAction extends FakeAction {
        private Location to;

        public FakeTeleportAction() {
            super(true);
        }

        @Override
        public RideAction duplicate() {
            return new network.palace.ridemanager.handlers.actions.TeleportAction(to);
        }

        @Override
        public String toString() {
            return "";
        }
    }

    @Getter
    @Setter
    public static class FakeExitAction extends FakeAction {
        private Location to;

        public FakeExitAction() {
            super(true);
        }

        @Override
        public RideAction duplicate() {
            return new network.palace.ridemanager.handlers.actions.ExitAction(to.clone());
        }

        @Override
        public String toString() {
            return "";
        }
    }

    private static abstract class FakeAction extends RideAction {

        public FakeAction(boolean movementAction) {
            super(movementAction);
        }

        @Override
        public void execute() {
        }

        @Override
        public boolean isFinished() {
            return true;
        }

        @Override
        public RideAction load(Cart cart) {
            this.cart = cart;
            return this;
        }
    }
}
