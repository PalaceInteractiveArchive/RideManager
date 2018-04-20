package network.palace.ridemanager.utils;

import lombok.AllArgsConstructor;
import lombok.Getter;
import network.palace.core.Core;
import network.palace.core.player.CPlayer;
import network.palace.core.player.CPlayerParticlesManager;
import network.palace.core.utils.ItemUtil;
import network.palace.ridemanager.handlers.BuildSession;
import network.palace.ridemanager.handlers.actions.*;
import network.palace.ridemanager.handlers.builder.PathDataTimer;
import network.palace.ridemanager.handlers.builder.actions.*;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.entity.ArmorStand;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.util.Vector;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * @author Marc
 * @since 8/10/17
 */
public class RideBuilderUtil {
    private HashMap<UUID, BuildSession> sessions = new HashMap<>();
    private List<UUID> inventory = new ArrayList<>();

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
                    RideAction current = session.getCurrentAction();
                    if (current != null) {
                        actions.add(current);
                    }
                    System.out.println(actions.size());

                    if (session.isShowArmorStands()) {
                        HashMap<Location, ArmorStand> stands = session.getStands();
                        Optional<RideAction> optional = actions.stream().filter(a -> a instanceof MoveAction).findFirst();
                        if (!optional.isPresent()) continue;
                        MoveAction lastAction = (MoveAction) optional.get();
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

                    player.sendMessage("A");
                    Location start;
                    if (actions.isEmpty() || !(actions.get(0) instanceof FakeSpawnAction)) {
                        player.sendMessage("B");
                        continue;
                    }
                    player.sendMessage("C");
                    start = ((FakeSpawnAction) actions.get(0)).getLocation();
                    player.sendMessage("D " + start.getBlockX() + "," + start.getBlockY() + "," + start.getBlockZ());
                    if (start == null) continue;
                    player.sendMessage("E");
                    particle(player.getParticles(), start);
                    player.sendMessage("F");
                    for (int i = 1; i < actions.size(); i++) {
                        RideAction action = actions.get(i);
                        if (!(action instanceof MoveAction)) continue;
                        MoveAction move = (MoveAction) action;
                        Location finalLoc = move.getFinalLocation();

                        for (double n = 0; n < finalLoc.distance(start); n += 0.5) {
                            double dx = finalLoc.getX() - start.getX();
                            double dy = finalLoc.getY() - start.getY();
                            double dz = finalLoc.getZ() - start.getZ();
                            Vector v = new Vector(dx, dy, dz);
                            double dis = 2 * start.distance(finalLoc);
                            v.divide(new Vector(dis, dis, dis));
                            start.add(v);
                            particle(player.getParticles(), start);
                        }

//                        particle(player.getParticles(), start);
                        start = move.getFinalLocation();
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
        Core.runTaskTimer(new PathDataTimer(), 0L, 20L);
    }

    private void pathParticle(CPlayer player, Location loc) {
        if (player.getLocation().distance(loc) > 15) return;
        player.getParticles().send(loc, Particle.REDSTONE, 1);
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
        session.updateBossBar();
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
        session.removeBossBar();
        return session;
    }

    public void moveEvent(CPlayer player, Location from, Location to) {
        BuildSession session = getSession(player);
        if (session == null) return;
        RideAction a = session.getCurrentAction();
        if (!(a instanceof MoveAction) || a instanceof RotateAction) return;
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

    public List<RideAction> getFakeActions(LinkedList<RideAction> list) {
        List<RideAction> finalList = new ArrayList<>();
        for (RideAction a : list) {
            if (a instanceof ExitAction) {
                finalList.add(new FakeExitAction(((ExitAction) a).getTo()));
            } else if (a instanceof SpawnAction) {
                finalList.add(new FakeSpawnAction(((SpawnAction) a).getLoc(), ((SpawnAction) a).getSpeed(), ((SpawnAction) a).getYaw()));
            } else if (a instanceof StraightAction) {
                finalList.add(new FakeStraightAction(((StraightAction) a).getTo()));
            } else if (a instanceof TeleportAction) {
                finalList.add(new FakeTeleportAction(((TeleportAction) a).getTo()));
            } else if (a instanceof TurnAction) {
                finalList.add(new FakeTurnAction(((TurnAction) a).getOrigin(), ((TurnAction) a).getAngle()));
            } else if (a instanceof SpeedAction) {
                finalList.add(new FakeSpeedAction(((SpeedAction) a).getSpeed(), ((SpeedAction) a).getTicks()));
            }
        }
        return finalList;
    }

    public List<BuildSession> getSessions() {
        return new ArrayList<>(sessions.values());
    }

    public void setInventory(UUID uuid, boolean value) {
        CPlayer player = Core.getPlayerManager().getPlayer(uuid);
        if (value) {
            if (inventory.contains(uuid)) return;
            inventory.add(uuid);
            if (player == null) return;
            int i = 0;
            PlayerInventory inv = player.getInventory();
            for (BlockAction b : BlockAction.values()) {
                ItemStack item = b.getItem();
                inv.setItem(i++, item);
            }
        } else {
            if (!inventory.contains(uuid)) return;
            inventory.remove(uuid);
            if (player == null) return;
            PlayerInventory inv = player.getInventory();
            for (int i = 0; i < 9; i++) {
                inv.setItem(i, ItemUtil.create(Material.AIR));
            }
            inv.setItem(0, ItemUtil.create(Material.COMPASS));
            inv.setItem(1, ItemUtil.create(Material.WOOD_AXE));
        }
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
        SPAWN(Material.STAINED_CLAY, (byte) 5, ChatColor.GREEN, FakeSpawnAction.class),
        STRAIGHT(Material.STAINED_CLAY, (byte) 4, ChatColor.YELLOW, FakeStraightAction.class),
        TURN(Material.STAINED_CLAY, (byte) 14, ChatColor.RED, FakeTurnAction.class),
        ROTATE(Material.STAINED_CLAY, (byte) 1, ChatColor.GOLD, FakeRotateAction.class),
        WAIT(Material.STAINED_CLAY, (byte) 13, ChatColor.DARK_GREEN, FakeWaitAction.class),
        INCLINE(Material.STAINED_CLAY, (byte) 3, ChatColor.AQUA, null),
        DECLINE(Material.STAINED_CLAY, (byte) 11, ChatColor.BLUE, null),
        TELEPORT(Material.STAINED_CLAY, (byte) 9, ChatColor.GRAY, FakeTeleportAction.class),
        EXIT(Material.STAINED_CLAY, (byte) 15, ChatColor.DARK_GRAY, FakeExitAction.class);
        private final Material type;
        private final byte data;
        private final ChatColor color;
        private final Class clazz;

        public ItemStack getItem() {
            return ItemUtil.create(type, getName(), data);
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

        public String getName() {
            String s = name().toLowerCase();
            return getColor() + s.substring(0, 1).toUpperCase() + s.substring(1);
        }

        public RideAction newAction() {
            if (clazz == null) return null;
            try {
                return (RideAction) clazz.newInstance();
            } catch (InstantiationException | IllegalAccessException e) {
                e.printStackTrace();
                return null;
            }
        }
    }
}
