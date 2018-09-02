package network.palace.ridemanager.handlers.builder;

import lombok.AllArgsConstructor;
import lombok.Getter;
import network.palace.core.utils.ItemUtil;
import network.palace.ridemanager.handlers.actions.RideAction;
import network.palace.ridemanager.handlers.builder.actions.*;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;

@AllArgsConstructor
@Getter
public enum ActionType {
    SPAWN(Material.STAINED_CLAY, (byte) 5, ChatColor.GREEN, FakeSpawnAction.class),
    STRAIGHT(Material.STAINED_CLAY, (byte) 4, ChatColor.YELLOW, FakeStraightAction.class),
    TURN(Material.STAINED_CLAY, (byte) 14, ChatColor.RED, FakeTurnAction.class),
    ROTATE(Material.STAINED_CLAY, (byte) 1, ChatColor.GOLD, FakeRotateAction.class),
    WAIT(Material.STAINED_CLAY, (byte) 13, ChatColor.DARK_GREEN, FakeWaitAction.class),
    INCLINE(Material.STAINED_CLAY, (byte) 3, ChatColor.AQUA, null),
    DECLINE(Material.STAINED_CLAY, (byte) 11, ChatColor.BLUE, null),
    TELEPORT(Material.STAINED_CLAY, (byte) 9, ChatColor.GRAY, FakeTeleportAction.class),
    LAUNCH(Material.STAINED_CLAY, (byte) 2, ChatColor.DARK_PURPLE, null),
    STOP(Material.STAINED_CLAY, (byte) 6, ChatColor.LIGHT_PURPLE, null),
    EXIT(Material.STAINED_CLAY, (byte) 15, ChatColor.DARK_GRAY, FakeExitAction.class);
    private final Material type;
    private final byte data;
    private final ChatColor color;
    private final Class clazz;

    /**
     * Create a new action
     *
     * @param loc location of the block placed to create this action
     * @return A RideAction
     */
    public FakeAction newAction(Location loc) {
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
                ((FakeTurnAction) action).setOrigin(loc);
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
        return ItemUtil.create(type, getColoredName(), data);
    }

    @SuppressWarnings("deprecation")
    public static ActionType fromBlock(Block b) {
        for (ActionType a : ActionType.values()) {
            if (a.type.equals(b.getType()) && a.data == b.getData()) {
                return a;
            }
        }
        return null;
    }

    @SuppressWarnings("deprecation")
    public static ActionType fromItem(ItemStack i) {
        for (ActionType a : ActionType.values()) {
            if (a.type.equals(i.getType()) && a.data == i.getData().getData()) {
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
