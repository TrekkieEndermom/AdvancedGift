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

import io.github.TrekkieEnderman.advancedgift.AdvancedGift;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;

import java.util.HashMap;

public class CommandManager {
    private final AdvancedGift plugin;
    private final HashMap<String, PaperCommand> paperCommandMap = new HashMap<>();

    public CommandManager(AdvancedGift plugin) {
        this.plugin = plugin;
    }

    public void addCommand(SimpleCommand command, String... aliases) {
        paperCommandMap.put(command.getName(), PaperCommand.wrap(command.getName(), command));
        for (String alias : aliases) {
            paperCommandMap.put(alias, PaperCommand.wrap(alias, command));
        }
    }

    public void registerAll() {
        plugin.getLogger().info("Registering " + paperCommandMap.size() + " commands (This includes command aliases)");
        plugin.getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, event -> {
            for (PaperCommand command : paperCommandMap.values()) {
                event.registrar().register(command.getCommandLabel(), command);
            }
        });
    }

    public void shutdown() {
        paperCommandMap.clear();
    }
}
