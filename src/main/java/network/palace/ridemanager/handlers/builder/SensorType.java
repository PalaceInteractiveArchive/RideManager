package network.palace.ridemanager.handlers.builder;

import lombok.AllArgsConstructor;
import lombok.Getter;
import network.palace.core.utils.ItemUtil;
import network.palace.ridemanager.handlers.BuildSession;
import network.palace.ridemanager.handlers.actions.RideAction;
import network.palace.ridemanager.handlers.builder.sensors.FakeSensor;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;

@AllArgsConstructor
@Getter
public enum SensorType {
    BLOCK(Material.STAINED_CLAY, (byte) 5, ChatColor.GREEN, null),
    SHOW(Material.STAINED_CLAY, (byte) 4, ChatColor.YELLOW, null),
    SPEED(Material.STAINED_CLAY, (byte) 14, ChatColor.RED, null),
    TEXT(Material.STAINED_CLAY, (byte) 1, ChatColor.GOLD, null);
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
    public FakeSensor newSensor(Location loc, BuildSession session) {
        if (clazz == null) return null;
        FakeSensor sensor;
        try {
            sensor = (FakeSensor) clazz.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            e.printStackTrace();
            return null;
        }
        if (sensor == null) return null;
        switch (this) {
//            case SPAWN:
//                ((FakeSpawnAction) action).setLoc(loc);
//                break;
            case BLOCK:
                break;
            case SHOW:
                break;
            case SPEED:
                break;
            case TEXT:
                break;
        }
        return sensor;
    }

    public String getName() {
        String name = name().toLowerCase();
        return name.substring(0, 1).toUpperCase() + name.substring(1);
    }

    public ItemStack getItem() {
        return ItemUtil.create(type, getColoredName(), data);
    }

    @SuppressWarnings("deprecation")
    public static SensorType fromBlock(Block b) {
        for (SensorType a : SensorType.values()) {
            if (a.type.equals(b.getType()) && a.data == b.getData()) {
                return a;
            }
        }
        return null;
    }

    @SuppressWarnings("deprecation")
    public static SensorType fromItem(ItemStack i) {
        for (SensorType a : SensorType.values()) {
            if (a.type.equals(i.getType()) && a.data == i.getData().getData()) {
                return a;
            }
        }
        return null;
    }

    public static ItemStack[] getItems() {
        ItemStack[] array = new ItemStack[]{};
        int i = 0;
        for (SensorType a : SensorType.values()) {
//            if (a.equals(SPAWN)) continue;
            array[i] = a.getItem();
            i++;
        }
        return array;
    }

    public String getColoredName() {
        String s = name().toLowerCase();
        return getColor() + s.substring(0, 1).toUpperCase() + s.substring(1);
    }

    public static SensorType fromAction(Class<? extends RideAction> clazz) {
        for (SensorType action : values()) {
            if (action.getClazz().equals(clazz)) {
                return action;
            }
        }
        return null;
    }

    public static SensorType fromString(String s) {
        switch (s.toLowerCase()) {
            case "block":
                return BLOCK;
            case "show":
                return SHOW;
            case "speed":
                return SPEED;
            case "text":
                return TEXT;
        }
        return null;
    }
}
