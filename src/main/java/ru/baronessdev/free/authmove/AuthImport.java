package ru.baronessdev.free.authmove;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;
import ru.baronessdev.paid.auth.api.AuthDataManagerAPI;
import ru.baronessdev.paid.auth.api.BaronessAuthAPI;
import ru.baronessdev.paid.auth.api.subcommands.AuthSubcommandBuilder;
import ru.baronessdev.paid.auth.pojo.PlayerProfile;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public final class AuthImport extends JavaPlugin {

    @Override
    public void onEnable() {
        saveDefaultConfig();

        BaronessAuthAPI.getSubcommandManager().addSubcommand(new AuthSubcommandBuilder("move", new Command(this))
                .setDescription("[mysql/sqlite] - переезд на новую версию")
                .build());
    }

    protected void processSQLite(CommandSender s) {
        try {
            Connection connection = DriverManager.getConnection("jdbc:sqlite://" + getDataFolder().getAbsolutePath() + File.separator + "old.db");
            importData(s, connection);
        } catch (SQLException e) {
            s.sendMessage("Не удалось подключиться к SQLite: " + e.getMessage());
        }
    }

    protected void processMySQL(CommandSender s) {
        try {
            Connection connection = DriverManager.getConnection(
                    getConfig().getString("url"),
                    getConfig().getString("user"),
                    getConfig().getString("password"));
            importData(s, connection);
        } catch (SQLException e) {
            s.sendMessage("Не удалось подключиться к MySQL: " + e.getMessage());
        }
    }

    private void importData(CommandSender s, Connection connection) {
        try {
            ResultSet resultSet = connection.createStatement().executeQuery("SELECT * FROM " + getConfig().getString("table"));

            AuthDataManagerAPI dataManager = BaronessAuthAPI.getDataManager();
            int i = 0;
            while (resultSet.next()) {
                String nick = resultSet.getString("nickname");
                String ip = resultSet.getString("registrationIP");
                String password = resultSet.getString("password");

                dataManager.saveProfile(new PlayerProfile(
                        nick,
                        nick.toLowerCase(),
                        (password.contains("$")) ? password : "$MD5$$" + password + "$",
                        ip,
                        ip,
                        Long.parseLong(resultSet.getString("registrationDate")),
                        Long.parseLong(resultSet.getString("lastLoginDate")),
                        UUID.fromString(resultSet.getString("uuid")),
                        false
                ));

                i++;
                s.sendMessage("Импортирован: " + ChatColor.GOLD + nick + ChatColor.WHITE + ". Всего: " + ChatColor.GOLD + i);
            }
            s.sendMessage(ChatColor.GREEN + "Импортирование завершено. Всего: " + i);
            connection.close();
        } catch (Exception e) {
            s.sendMessage(ChatColor.YELLOW + "Произошла ошибка при импортировании: " + e.getMessage());
        }
    }

    enum DatabaseType {
        MYSQL,
        SQLITE
    }
}
