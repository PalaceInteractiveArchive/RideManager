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
import java.io.IOException;

/**
 * @author Marc
 * @since 8/10/17
 */
@CommandMeta(description = "Create a new ride")
public class NewCommand extends CoreCommand {

    public NewCommand() {
        super("new");
    }

    @Override
    protected void handleCommand(CPlayer player, String[] args) throws CommandException {
        if (args.length < 2) {
            player.sendMessage(ChatColor.RED + "/rb new [file name] [name]");
            return;
        }
        RideBuilderUtil util = RideManager.getRideBuilderUtil();
        if (util.getSession(player.getUniqueId()) != null) {
            player.sendMessage(ChatColor.RED + "You're already in a build session! Save or exit to load another one.");
            return;
        }
        String fileName = args[0];
        StringBuilder name = new StringBuilder();
        for (int i = 1; i < args.length; i++) {
            name.append(args[i]).append(" ");
        }
        BuildSession session = util.newSession(player);
        session.setName(name.toString().trim());
        session.setFileName(fileName + ".ride");
        session.updateBossBar();
        util.setInventory(player.getUniqueId(), true);

        File mapFile = new File("plugins/RideManager/maps/" + fileName + ".map");
        if (!mapFile.exists()) {
            try {
                mapFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
            player.sendMessage(ChatColor.YELLOW + "" + ChatColor.BOLD + "Note: " + ChatColor.YELLOW + "Created a blank seat map file, edit it to change seat organization");
        }

        try {
            session.save();
            player.sendMessage(ChatColor.GREEN + "New ride session created!");
        } catch (IOException e) {
            e.printStackTrace();
            player.sendMessage(ChatColor.RED + "There was an error saving your new session file: " + e.getMessage());
        }
    }
}
