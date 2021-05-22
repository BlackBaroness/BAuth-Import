package ru.baronessdev.free.bauth_addons.bimport;

import com.google.common.collect.ImmutableList;
import org.bstats.bukkit.Metrics;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;
import ru.baronessdev.free.bauth_addons.bimport.util.UpdateCheckerUtil;
import ru.baronessdev.free.bauth_addons.bimport.util.logging.LogType;
import ru.baronessdev.free.bauth_addons.bimport.util.logging.Logger;
import ru.baronessdev.paid.auth.api.AuthDataManagerAPI;
import ru.baronessdev.paid.auth.api.BaronessAuthAPI;
import ru.baronessdev.paid.auth.api.subcommands.AuthSubcommandBuilder;
import ru.baronessdev.paid.auth.pojo.PlayerProfile;

import java.sql.Connection;
import java.sql.ResultSet;
import java.util.UUID;

public final class Import extends JavaPlugin {

    @Override
    public void onEnable() {
        saveDefaultConfig();

        BaronessAuthAPI.getPaperCommandManager().getCommandCompletions().registerAsyncCompletion("moveTips", c -> ImmutableList.of("sqlite", "mysql"));
        BaronessAuthAPI.getSubcommandManager().addSubcommand(new AuthSubcommandBuilder("move", new Command(this))
                .setDescription("[mysql/sqlite] - переезд на новую версию")
                .build());

        new Metrics(this,11438);
        checkUpdates();
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

    private void checkUpdates() {
        try {
            int i = UpdateCheckerUtil.check(this);
            if (i != -1) {
                Logger.log(LogType.INFO, "New version found: v" + ChatColor.YELLOW + i + ChatColor.GRAY + " (Current: v" + getDescription().getVersion() + ")");
                Logger.log(LogType.INFO, "Update now: " + ChatColor.AQUA + "market.baronessdev.ru/shop/licenses/");
            }
        } catch (UpdateCheckerUtil.UpdateCheckException e) {
            Logger.log(LogType.ERROR, "Could not check for updates: " + e.getRootCause());
            Logger.log(LogType.ERROR, "Please contact Baroness's Dev if this isn't your mistake.");
        }
    }
}
