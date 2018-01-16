package network.palace.ridemanager.commands.ridebuilder;

import network.palace.core.command.CommandException;
import network.palace.core.command.CommandMeta;
import network.palace.core.command.CoreCommand;
import network.palace.core.player.CPlayer;
import network.palace.ridemanager.RideManager;
import network.palace.ridemanager.handlers.BuildSession;
import network.palace.ridemanager.utils.RideBuilderUtil;
import org.bukkit.ChatColor;

/**
 * @author Marc
 * @since 8/10/17
 */
@CommandMeta(description = "Lock all actions to a specific y-value")
public class LockYCommand extends CoreCommand {

    public LockYCommand() {
        super("locky");
    }

    @Override
    protected void handleCommand(CPlayer player, String[] args) throws CommandException {
        RideBuilderUtil rideBuilderUtil = RideManager.getRideBuilderUtil();
        BuildSession session = rideBuilderUtil.getSession(player.getUniqueId());
        if (session == null) {
            player.sendMessage(ChatColor.RED + "You aren't in a build session!");
            return;
        }
        double y;
        if (args.length == 0) {
            y = player.getLocation().getY();
        } else {
            if (args[0].equalsIgnoreCase("off")) {
                session.setLockY(0);
                session.updateBossBar();
                player.sendMessage(ChatColor.RED + "Your session's Y values are no longer locked");
                return;
            }
            try {
                y = Double.parseDouble(args[0]);
            } catch (NumberFormatException e) {
                player.sendMessage(ChatColor.RED + "'" + args[0] + "' isn't a number!");
                return;
            }
        }
        session.setLockY(y);
        session.updateBossBar();
        player.sendMessage(ChatColor.GREEN + "Your session's Y values are now locked at y=" + y);
    }
}
