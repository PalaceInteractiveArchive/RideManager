package network.palace.ridemanager.commands;

import network.palace.core.command.CommandException;
import network.palace.core.command.CoreCommand;
import network.palace.core.player.CPlayer;
import network.palace.ridemanager.commands.ridebuilder.*;

/**
 * @author Marc
 * @since 8/7/17
 */
public class CommandRideBuilder extends CoreCommand {

    public CommandRideBuilder() {
        super("rb");
        registerSubCommand(new CommandAction());
        registerSubCommand(new CommandConfirm());
        registerSubCommand(new CommandDeny());
        registerSubCommand(new CommandExit());
        registerSubCommand(new CommandLoad());
        registerSubCommand(new CommandLockY());
        registerSubCommand(new CommandNew());
        registerSubCommand(new CommandPath());
        registerSubCommand(new CommandSave());
        registerSubCommand(new CommandY());
    }

    @Override
    protected void handleCommand(CPlayer player, String[] args) throws CommandException {
    }
}
