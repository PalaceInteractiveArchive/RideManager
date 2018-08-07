package network.palace.ridemanager.utils;

import network.palace.core.Core;
import network.palace.core.player.CPlayer;
import network.palace.core.utils.ItemUtil;
import network.palace.ridemanager.handlers.BuildSession;
import network.palace.ridemanager.handlers.actions.*;
import network.palace.ridemanager.handlers.builder.PathDataTimer;
import network.palace.ridemanager.handlers.builder.actions.*;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.ArmorStand;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.util.Vector;

import java.io.File;
import java.util.*;

/**
 * @author Marc
 * @since 8/10/17
 */
public class RideBuilderUtil {
    private HashMap<UUID, BuildSession> sessions = new HashMap<>();
    private List<UUID> inventory = new ArrayList<>();

    public RideBuilderUtil() {
        /*Core.runTaskTimer(new Runnable() {
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
                    CPlayerParticlesManager particles = player.getParticles();
                    List<RideAction> actions = session.getActions();
                    RideAction current = session.getCurrentAction();
                    if (current != null) {
                        actions.add(current);
                    }
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

//                    player.sendMessage("A");
                    Location start;
                    if (actions.isEmpty() || !(actions.get(0) instanceof FakeSpawnAction)) {
//                        player.sendMessage("B");
                        continue;
                    }
//                    player.sendMessage("C");
                    start = ((FakeSpawnAction) actions.get(0)).getLocation();
//                    player.sendMessage("D " + start.getBlockX() + "," + start.getBlockY() + "," + start.getBlockZ());
                    if (start == null) continue;
//                    player.sendMessage("E");
//                    particle(particles, start, true);
//                    player.sendMessage("F");
                    for (int i = 1; i < actions.size(); i++) {
                        RideAction action = actions.get(i);
                        if (!(action instanceof MoveAction)) continue;
                        MoveAction move = (MoveAction) action;
                        Location finalLoc = move.getFinalLocation();

//                        particle(particles, start, true);

                        for (double n = 0; n < finalLoc.distance(start); n += 0.5) {
                            double dx = finalLoc.getX() - start.getX();
                            double dy = finalLoc.getY() - start.getY();
                            double dz = finalLoc.getZ() - start.getZ();
                            Vector v = new Vector(dx, dy, dz);
                            double dis = 2 * start.distance(finalLoc);
                            v.divide(new Vector(dis, dis, dis));
                            start.add(v);
                            particle(particles, start, false);
                        }

                        start = move.getFinalLocation();
                    }
                }
            }

            private void particle(CPlayerParticlesManager part, Location loc, boolean action) {
                if (action) {
                    part.send(loc, Particle.VILLAGER_HAPPY, 2);
                } else {
                    part.send(loc, Particle.REDSTONE, 1);
                }
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
        }, 0L, 20L);*/
        Core.runTaskTimer(new PathDataTimer(), 0L, 20L);
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
        switch (a.getActionType()) {
            case EXIT: {
                ExitAction act = (ExitAction) a;
                return new ExitAction(act.getTo().add(v), act.getAutoYaw());
            }
            case SPAWN: {
                SpawnAction act = (SpawnAction) a;
                return new SpawnAction(act.getLoc().add(v), act.getSpeed(), act.getYaw());
            }
            case STRAIGHT: {
                StraightAction act = (StraightAction) a;
                return new StraightAction(act.getTo().add(v), act.getAutoYaw());
            }
            case TELEPORT: {
                TeleportAction act = (TeleportAction) a;
                return new TeleportAction(act.getTo().add(v));
            }
            case TURN: {
                TurnAction act = (TurnAction) a;
                return new TurnAction(act.getOrigin().add(v), act.getAngle());
            }
        }
        return null;
    }

    public List<RideAction> getFakeActions(LinkedList<RideAction> list) {
        List<RideAction> finalList = new ArrayList<>();
        for (RideAction a : list) {
            switch (a.getActionType()) {
                case EXIT: {
                    finalList.add(new FakeExitAction(((ExitAction) a).getTo(), ((ExitAction) a).getAutoYaw()));
                    break;
                }
                case SPAWN: {
                    finalList.add(new FakeSpawnAction(((SpawnAction) a).getLoc(), ((SpawnAction) a).getSpeed(), ((SpawnAction) a).getYaw()));
                    break;
                }
                case STRAIGHT: {
                    finalList.add(new FakeStraightAction(((StraightAction) a).getTo(), ((StraightAction) a).getAutoYaw()));
                    break;
                }
                case TELEPORT: {
                    finalList.add(new FakeTeleportAction(((TeleportAction) a).getTo()));
                    break;
                }
                case TURN: {
                    finalList.add(new FakeTurnAction(((TurnAction) a).getOrigin(), ((TurnAction) a).getAngle()));
                    break;
                }
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
            inv.setItem(0, ItemUtil.create(Material.COMPASS));
            inv.setItem(1, ItemUtil.create(Material.WOOD_AXE));
            inv.setItem(2, ItemUtil.create(Material.STAINED_CLAY, ChatColor.GREEN + "Create an Action", (byte) 5));
            inv.setItem(3, ItemUtil.create(Material.STAINED_CLAY, ChatColor.AQUA + "Create a Sensor", (byte) 3));
            inv.setItem(4, ItemUtil.create(Material.STONE_SPADE, ChatColor.GRAY + "Edit Action/Sensor"));
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
}
