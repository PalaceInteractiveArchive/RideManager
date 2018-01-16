package network.palace.ridemanager.commands.ridebuilder;

import network.palace.core.command.CommandException;
import network.palace.core.command.CommandMeta;
import network.palace.core.command.CoreCommand;
import network.palace.core.player.CPlayer;
import network.palace.ridemanager.RideManager;
import network.palace.ridemanager.handlers.BuildSession;
import network.palace.ridemanager.utils.RideBuilderUtil;
import org.bukkit.ChatColor;

import java.io.File;

/**
 * @author Marc
 * @since 8/10/17
 */
@CommandMeta(description = "Load a ride from file")
public class LoadCommand extends CoreCommand {

    public LoadCommand() {
        super("load");
    }

    @Override
    protected void handleCommand(CPlayer player, String[] args) throws CommandException {
        if (args.length < 1) {
            player.sendMessage(ChatColor.RED + "/rb load [file]");
            return;
        }
        RideBuilderUtil rideBuilderUtil = RideManager.getRideBuilderUtil();
        BuildSession session = rideBuilderUtil.getSession(player.getUniqueId());
        if (session != null) {
            player.sendMessage(ChatColor.RED + "You're already in a build session! Save and exit to load another one.");
            return;
        }
        File file = new File("plugins/RideManager/rides/" + args[0] + ".ride");
        if (!file.exists()) {
            player.sendMessage(ChatColor.RED + "No file exists with the name " + ChatColor.GREEN + args[0] + ".ride!");
            return;
        }
        rideBuilderUtil.loadSession(player, file);
    }
}
