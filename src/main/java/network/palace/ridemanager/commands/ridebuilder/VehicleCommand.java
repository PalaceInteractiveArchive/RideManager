package network.palace.ridemanager.commands.ridebuilder;

import network.palace.core.command.CommandException;
import network.palace.core.command.CommandMeta;
import network.palace.core.command.CoreCommand;
import network.palace.core.player.CPlayer;
import network.palace.ridemanager.RideManager;
import network.palace.ridemanager.handlers.BuildSession;
import network.palace.ridemanager.utils.RideBuilderUtil;
import org.bukkit.ChatColor;

@CommandMeta(description = "Toggle a display vehicle in the station")
public class VehicleCommand extends CoreCommand {

    public VehicleCommand() {
        super("vehicle");
    }

    @Override
    protected void handleCommand(CPlayer player, String[] args) throws CommandException {
        RideBuilderUtil rideBuilderUtil = RideManager.getRideBuilderUtil();
        BuildSession session = rideBuilderUtil.getSession(player.getUniqueId());
        if (session == null) {
            player.sendMessage(ChatColor.RED + "You aren't in a build session!");
            return;
        }
        if (session.toggleDisplayVehicle()) {
            player.sendMessage(ChatColor.GREEN + "A display vehicle is now visible at the station.");
        } else {
            player.sendMessage(ChatColor.RED + "The display vehicle is no longer at the station.");
        }
        session.updateBossBar();
    }
}
