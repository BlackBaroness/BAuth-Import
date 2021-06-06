package ru.baronessdev.free.bauth_addons.bimport;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import ru.baronessdev.paid.auth.api.subcommands.AuthSubcommandExecutor;
import ru.baronessdev.paid.auth.lib.acf.core.annotation.*;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

@SuppressWarnings("unused")
@CommandAlias("auth")
@Subcommand("move")
public class Command extends AuthSubcommandExecutor {

    private final Import plugin;

    public Command(Import plugin) {
        this.plugin = plugin;
    }

    @CommandCompletion("@moveTips")
    @CatchUnknown
    @Default
    public void unknown(CommandSender sender) {
        help(sender);
    }

    @CommandCompletion("@nothing")
    @Subcommand("sqlite")
    public void sqlite(CommandSender sender) {
        try {
            Connection connection = DriverManager.getConnection("jdbc:sqlite://" + plugin.getDataFolder().getAbsolutePath() + File.separator + "sqlite.db");
            plugin.importData(sender, connection);
        } catch (SQLException e) {
            sender.sendMessage(ChatColor.YELLOW + "Не удалось подключиться к SQLite: " + e.getMessage());
        }
    }

    @CommandCompletion("@nothing")
    @Subcommand("mysql")
    public void mysql(CommandSender sender) {
        try {
            FileConfiguration cfg = plugin.getConfig();

            Connection connection = DriverManager.getConnection(
                    cfg.getString("url"),
                    cfg.getString("user"),
                    cfg.getString("password"));
            plugin.importData(sender, connection);
        } catch (SQLException e) {
            sender.sendMessage(ChatColor.YELLOW + "Не удалось подключиться к MySQL: " + e.getMessage());
        }
    }
}
