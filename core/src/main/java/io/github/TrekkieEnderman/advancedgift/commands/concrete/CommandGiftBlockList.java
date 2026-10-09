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

package io.github.TrekkieEnderman.advancedgift.commands.concrete;

import io.github.TrekkieEnderman.advancedgift.AdvancedGift;
import io.github.TrekkieEnderman.advancedgift.commands.SimpleCommand;
import io.github.TrekkieEnderman.advancedgift.locale.Message;
import io.github.TrekkieEnderman.advancedgift.util.ChatFormatUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Set;
import java.util.UUID;

public class CommandGiftBlockList extends SimpleCommand {
    public CommandGiftBlockList(final AdvancedGift plugin) {
        super(plugin, "giftblocklist", null);
    }

    @Override
    protected void showUsage(CommandSender sender) {
        sender.sendMessage(ChatFormatUtils.fromLegacyText(plugin.getPrefix() + Message.BLOCK_LIST_DESCRIPTION.translate()));
        sender.sendMessage(ChatFormatUtils.fromLegacyText(Message.BLOCK_LIST_USAGE.translate()));
    }

    @Override
    protected boolean run(@NotNull Player sender, @NotNull String label, @NotNull String[] args) {
        final UUID senderUUID = sender.getUniqueId();

        if (args.length == 0) {
            final Set<UUID> blockList = plugin.getPlayerDataManager().getBlockList(senderUUID);
            if (blockList == null || blockList.isEmpty()) {
                sender.sendMessage(ChatFormatUtils.fromLegacyText(plugin.getPrefix() + Message.BLOCK_LIST_EMPTY.translate()));
            } else {
                sender.sendMessage(ChatFormatUtils.fromLegacyText(plugin.getPrefix() + Message.BLOCK_LIST_SHOW.translate()));
                final TextComponent.Builder builder = Component.text(); //main builder for showing the list

                final HoverEvent<Component> hoverEvent = ChatFormatUtils.fromLegacyText(Message.TIP_CLICK_TO_UNBLOCK.translate()).asHoverEvent();

                boolean first = true;
                for (final UUID playerUUID : blockList) {
                    OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(playerUUID);
                    ClickEvent clickEvent = ClickEvent.runCommand("/giftunblock " + offlinePlayer.getName());

                    Component playerComp = offlinePlayer.isOnline()
                            ? offlinePlayer.getPlayer().displayName()
                            : Component.text(offlinePlayer.getName());

                    if (!first) {
                        builder.appendSpace();
                    }
                    first = false;
                    builder.append(playerComp.color(NamedTextColor.DARK_AQUA).hoverEvent(hoverEvent).clickEvent(clickEvent));
                }
                sender.sendMessage(builder.build());
                sender.sendMessage(ChatFormatUtils.fromLegacyText(Message.BLOCK_LIST_USAGE.translate()));
            }
            return true;
        }

        if (args[0].equalsIgnoreCase("clear")) {
            if (plugin.getPlayerDataManager().clearBlockList(senderUUID)) {
                sender.sendMessage(ChatFormatUtils.fromLegacyText(plugin.getPrefix() + Message.BLOCK_LIST_CLEARED.translate()));
            }
            else {
                sender.sendMessage(ChatFormatUtils.fromLegacyText(plugin.getPrefix() + Message.BLOCK_LIST_ALREADY_CLEARED.translate()));
            }
            return true;
        }

        sender.sendMessage(ChatFormatUtils.fromLegacyText(plugin.getPrefix() + Message.ARGUMENT_NOT_RECOGNIZED.translate(args[0])));
        return false;
    }
}
