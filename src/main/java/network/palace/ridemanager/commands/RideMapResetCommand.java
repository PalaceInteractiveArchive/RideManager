package network.palace.ridemanager.commands;

import network.palace.core.command.CommandException;
import network.palace.core.command.CommandMeta;
import network.palace.core.command.CoreCommand;
import network.palace.core.player.Rank;
import network.palace.ridemanager.RideManager;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

@CommandMeta(description = "Reset ride seat maps", rank = Rank.MOD)
public class RideMapResetCommand extends CoreCommand {

    public RideMapResetCommand() {
        super("ridemapreset");
    }

    @Override
    protected void handleCommandUnspecific(CommandSender sender, String[] args) throws CommandException {
        sender.sendMessage(ChatColor.GREEN + "Resetting ride seat maps...");
        RideManager.getMappingUtil().reset();
        sender.sendMessage(ChatColor.GREEN + "Ride seat maps reset!");
    }
}
