package ru.baronessdev.free.authmove;

import com.google.common.collect.ImmutableList;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;
import ru.baronessdev.paid.auth.api.AuthDataManagerAPI;
import ru.baronessdev.paid.auth.api.BaronessAuthAPI;
import ru.baronessdev.paid.auth.api.subcommands.AuthSubcommandBuilder;
import ru.baronessdev.paid.auth.pojo.PlayerProfile;

import java.sql.Connection;
import java.sql.ResultSet;
import java.util.UUID;

/**
 * AuthImport addon for BaronessAuth
 * @author BlackBaroness
 */
public final class AuthImport extends JavaPlugin {

    @Override
    public void onEnable() {
        saveDefaultConfig();

        BaronessAuthAPI.getPaperCommandManager().getCommandCompletions().registerAsyncCompletion("moveTips", c -> ImmutableList.of("sqlite", "mysql"));
        BaronessAuthAPI.getSubcommandManager().addSubcommand(new AuthSubcommandBuilder("move", new Command(this))
                .setDescription("[mysql/sqlite] - переезд на новую версию")
                .build());
    }

    public void importData(CommandSender s, Connection connection) {
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
                        (password.contains("$")) ? password + "$": "$MD5$$" + password + "$",
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
}
