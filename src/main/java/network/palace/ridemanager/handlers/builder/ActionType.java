package network.palace.ridemanager.handlers.builder;

import lombok.AllArgsConstructor;
import lombok.Getter;
import network.palace.core.utils.ItemUtil;
import network.palace.ridemanager.handlers.BuildSession;
import network.palace.ridemanager.handlers.actions.RideAction;
import network.palace.ridemanager.handlers.builder.actions.*;
import network.palace.ridemanager.handlers.ride.Ride;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

@AllArgsConstructor
@Getter
public enum ActionType {
    SPAWN(Material.LIME_TERRACOTTA, ChatColor.GREEN, FakeSpawnAction.class),
    STRAIGHT(Material.YELLOW_TERRACOTTA, ChatColor.YELLOW, FakeStraightAction.class),
    TURN(Material.RED_TERRACOTTA, ChatColor.RED, FakeTurnAction.class),
    //    NEWTURN(Material.STAINED_CLAY, (byte) 7, ChatColor.RED, FakeNewTurnAction.class),
    ROTATE(Material.ORANGE_TERRACOTTA, ChatColor.GOLD, FakeRotateAction.class),
    WAIT(Material.GREEN_TERRACOTTA, ChatColor.DARK_GREEN, FakeWaitAction.class),
    INCLINE(Material.LIGHT_BLUE_TERRACOTTA, ChatColor.AQUA, null),
    DECLINE(Material.BLUE_TERRACOTTA, ChatColor.BLUE, null),
    TELEPORT(Material.CYAN_TERRACOTTA, ChatColor.GRAY, FakeTeleportAction.class),
    LAUNCH(Material.MAGENTA_TERRACOTTA, ChatColor.DARK_PURPLE, null),
    STOP(Material.PINK_TERRACOTTA, ChatColor.LIGHT_PURPLE, null),
    EXIT(Material.BLACK_TERRACOTTA, ChatColor.DARK_GRAY, FakeExitAction.class);
    private final Material type;
    private final ChatColor color;
    private final Class clazz;

    /**
     * Create a new action
     *
     * @param loc location of the block placed to create this action
     * @return A RideAction
     */
    public FakeAction newAction(Location loc, BuildSession session) {
        if (clazz == null) return null;
        FakeAction action;
        try {
            action = (FakeAction) clazz.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            e.printStackTrace();
            return null;
        }
        if (action == null) return null;
        switch (this) {
            case SPAWN:
                ((FakeSpawnAction) action).setLoc(loc);
                break;
            case STRAIGHT:
                ((FakeStraightAction) action).setTo(loc);
                break;
            case TURN:
                ((FakeTurnAction) action).setTo(loc);
                Location last = session.getLastLocation();
                if (last == null) {
                    ((FakeTurnAction) action).setP0(loc);
                } else {
                    float yaw = last.getYaw();

                    Vector p0Vector = Ride.getRelativeVector(yaw, 3);
                    p0Vector.multiply(new Vector(-1, 1, 1));

                    Location p0 = last.clone().add(p0Vector);

                    ((FakeTurnAction) action).setP0(p0);
                    ((FakeTurnAction) action).setFrom(last);
                }
                break;
            case ROTATE:
                break;
            case WAIT:
                break;
            case INCLINE:
                break;
            case DECLINE:
                break;
            case TELEPORT:
                ((FakeTeleportAction) action).setTo(loc);
                break;
            case LAUNCH:
                break;
            case STOP:
                break;
            case EXIT:
                ((FakeExitAction) action).setTo(loc);
                break;
        }
        return action;
    }

    public String getName() {
        String name = name().toLowerCase();
        return name.substring(0, 1).toUpperCase() + name.substring(1);
    }

    public ItemStack getItem() {
        return ItemUtil.create(type, getColoredName());
    }

    public static ActionType fromBlock(Block b) {
        for (ActionType a : ActionType.values()) {
            if (a.type.equals(b.getType())) {
                return a;
            }
        }
        return null;
    }

    public static ActionType fromItem(ItemStack i) {
        for (ActionType a : ActionType.values()) {
            if (a.type.equals(i.getType())) {
                return a;
            }
        }
        return null;
    }

    public static ItemStack[] getItems() {
        ItemStack[] array = new ItemStack[]{};
        int i = 0;
        for (ActionType a : ActionType.values()) {
            if (a.equals(SPAWN)) continue;
            array[i] = a.getItem();
            i++;
        }
        return array;
    }

    public String getColoredName() {
        String s = name().toLowerCase();
        return getColor() + s.substring(0, 1).toUpperCase() + s.substring(1);
    }

    public static ActionType fromAction(Class<? extends RideAction> clazz) {
        for (ActionType action : values()) {
            if (action.getClazz().equals(clazz)) {
                return action;
            }
        }
        return null;
    }
}
