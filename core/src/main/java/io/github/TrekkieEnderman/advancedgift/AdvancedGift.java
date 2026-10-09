/*
 * Copyright (c) 2025 TrekkieEnderman
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package io.github.TrekkieEnderman.advancedgift;

import io.github.TrekkieEnderman.advancedgift.commands.CommandManager;
import io.github.TrekkieEnderman.advancedgift.commands.concrete.*;
import io.github.TrekkieEnderman.advancedgift.data.PlayerDataManager;
import io.github.TrekkieEnderman.advancedgift.data.StandardDataManager;
import io.github.TrekkieEnderman.advancedgift.listener.PlayerJoinListener;
import io.github.TrekkieEnderman.advancedgift.locale.Message;
import io.github.TrekkieEnderman.advancedgift.locale.Translation;
import io.github.TrekkieEnderman.advancedgift.metrics.GiftCounter;
import io.github.TrekkieEnderman.advancedgift.util.ChatFormatUtils;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bstats.bukkit.Metrics;
import org.bstats.charts.SingleLineChart;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.logging.Level;

public class AdvancedGift extends JavaPlugin {
    private final File configFile = new File(getDataFolder(),"config.yml");
    private final HashMap<Integer, ArrayList<String>> worldList = new HashMap<>();
    @Getter
    private String prefix;
    @Getter
    private Component prefixComp = Component.text("[AdvancedGift]").color(NamedTextColor.GOLD);
    @Getter
    private boolean textTooltipEnabled;
    private boolean hasArtMap = false;
    @Getter
    private final GiftCounter giftCounter = new GiftCounter();
    @Getter
    private PlayerDataManager playerDataManager;
    private final CommandManager commandManager = new CommandManager(this);

    @Override
    public void onEnable() {
        getLogger().info("===================================================");
        getLogger().info("Loading files  --------------------");
        loadFiles();
        getLogger().info("");

        textTooltipEnabled = getConfigFile().getBoolean("enable-tooltip", true);
        commandManager.addCommand(new CommandGift(this), "sendgift", "giftsend");
        commandManager.addCommand(new CommandGiftToggle(this), "togglegift", "tg", "gt");
        commandManager.addCommand(new CommandGiftBlock(this), "blockgift", "gblock");
        commandManager.addCommand(new CommandGiftUnblock(this), "unblockgift", "gunblock", "ungblock");
        commandManager.addCommand(new CommandGiftBlockList(this), "gblocklist", "gblist");
        commandManager.addCommand(new CommandReload(this), "agr");
        commandManager.addCommand(new CommandSpy(this), "gspy");
        commandManager.addCommand(new CommandTranslate(this));
        commandManager.registerAll();
        getLogger().info("===================================================");
        if (Bukkit.getPluginManager().getPlugin("ArtMap") != null) hasArtMap = true;
        startMetrics();
        Bukkit.getPluginManager().registerEvents(new PlayerJoinListener(this), this);
    }

    private void loadFiles() {
        if(!getDataFolder().exists()) {
            getDataFolder().mkdirs();
        }
        Translation.init(this);
        loadConfigFile();
        if (isConfigOutdated()) {
            getLogger().warning(ChatFormatUtils.stripFormatting(Message.OUTDATED_CONFIG.translate()));
        }

        playerDataManager = new StandardDataManager(this);

        this.getPlayerDataManager().load();
    }

    public FileConfiguration getConfigFile() {
        return getConfig();
    }

    public boolean loadConfigFile() {
        // Moved config creation to here so the plugin doesn't run into issues when reloading it on command later
        if (!configFile.exists()) {
            getLogger().info(ChatFormatUtils.stripFormatting(Message.CONFIG_NOT_FOUND.translate()));
            saveDefaultConfig();
        }
        reloadConfig();
        Translation.updateLocale(getConfigFile().getString("locale"));
        loadWorldGroupList();
        prefix = this.getConfigFile().getString("prefix") + " ";
        prefixComp = ChatFormatUtils.fromLegacyText(prefix);
        getLogger().log(Level.INFO, ChatFormatUtils.stripFormatting(Message.CONFIG_LOADED.translate()));
        return true;
    }

    public boolean isConfigOutdated() {
        return !this.getConfig().isSet("locale");
    }

    @Override
    public void onDisable() {
        this.getPlayerDataManager().save();
        this.commandManager.shutdown();
    }

    private void loadWorldGroupList() {
        worldList.clear();
        int key = 0;
        for (String world : getConfig().getStringList("world-group-list")) {
            String[] array = world.split(", ");
            ArrayList<String> list = new ArrayList<>(Arrays.asList(array));
            worldList.put(key, list);
            key += 1;
        }
    }

    public int getPlayerWorldGroup(Player player) {
        for (int key : worldList.keySet()) {
            ArrayList<String> values = worldList.get(key);
            for (String w : values) {
                if (player.getWorld().getName().equalsIgnoreCase(w)) {
                    return key;
                }
            }
        }
        return -1;
    }

    private void startMetrics() {
        Metrics metrics = new Metrics(this, 13627);
        metrics.addCustomChart(new SingleLineChart("gifts_sent", giftCounter::collect));
    }

    public boolean hasArtMap() {
        return hasArtMap;
    }
}