package network.palace.ridemanager.commands;

import network.palace.core.command.CommandMeta;
import network.palace.core.command.CoreCommand;
import network.palace.core.player.Rank;
import network.palace.ridemanager.commands.ridebuilder.*;

/**
 * @author Marc
 * @since 8/7/17
 */
@CommandMeta(description = "Default Ride Builder command", rank = Rank.DEVELOPER)
public class RideBuilderCommand extends CoreCommand {

    public RideBuilderCommand() {
        super("rb");
        registerSubCommand(new ActionCommand());
        registerSubCommand(new ConfirmCommand());
        registerSubCommand(new DenyCommand());
        registerSubCommand(new ExitCommand());
        registerSubCommand(new LoadCommand());
        registerSubCommand(new LockYCommand());
        registerSubCommand(new NewCommand());
        registerSubCommand(new PathCommand());
        registerSubCommand(new SaveCommand());
        registerSubCommand(new StandCommand());
        registerSubCommand(new YCommand());
    }

    @Override
    protected boolean isUsingSubCommandsOnly() {
        return true;
    }
}
