/*
 * Copyright (c) 2026 TrekkieEnderman
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

package io.github.TrekkieEnderman.advancedgift.commands;

import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.Collection;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Adapts SimpleCommand to the {@link BasicCommand} structure
 */
public class PaperCommand implements BasicCommand {
    final SimpleCommand wrapped;
    @Getter
    final String commandLabel;

    private PaperCommand(SimpleCommand wrapped, String commandLabel) {
        this.wrapped = wrapped;
        this.commandLabel = commandLabel;
    }

    public static PaperCommand wrap(String commandLabel, SimpleCommand commandToWrap) {
        return new PaperCommand(commandToWrap, commandLabel);
    }

    @Override
    public void execute(CommandSourceStack commandSourceStack, String @NonNull [] args) {
        wrapped.run(commandSourceStack.getSender(), commandLabel, args);
    }

    @Override
    public @NonNull Collection<String> suggest(@NonNull CommandSourceStack commandSourceStack, String[] args) {
        // Tries to recreate the suggestion behavior Spigot had. I CBA to add proper suggestions when this isn't a rewrite.
        if (args.length == 0) {
            return Bukkit.getOnlinePlayers().stream().map(Player::getName).collect(Collectors.toList());
        }
        Stream<? extends Player> stream = Bukkit.getOnlinePlayers().stream();
        if (commandSourceStack.getSender() instanceof Player) {
            Player sender = (Player) commandSourceStack.getSender();
            stream = stream.filter(sender::canSee);
        }
        return stream.map(Player::getName)
                .filter(name -> name.toLowerCase().startsWith(args[args.length -1].toLowerCase()))
                .collect(Collectors.toList());
    }

    @Override
    public boolean canUse(@NonNull CommandSender sender) {
        return wrapped.isAuthorized(sender);
    }

    @Override
    public @Nullable String permission() {
        return wrapped.getPermission();
    }
}
