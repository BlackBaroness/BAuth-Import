package ru.baronessdev.free.authmove;

import org.bukkit.command.CommandSender;
import ru.baronessdev.paid.auth.api.subcommands.AuthSubcommandExecutor;

public class Command implements AuthSubcommandExecutor {

    private final AuthImport plugin;

    public Command(AuthImport plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        AuthImport.DatabaseType type;
        try {
            type = AuthImport.DatabaseType.valueOf(args[0].toUpperCase());
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        plugin.reloadConfig();

        switch (type) {
            case SQLITE: {
                plugin.processSQLite(sender);
                return true;
            }
            case MYSQL: {
                plugin.processMySQL(sender);
                return true;
            }
        }
        return true;
    }
}
