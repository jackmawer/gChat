/*
 * This file is part of gChat, licensed under the MIT License.
 *
 *  Copyright (c) lucko (Luck) <luck@lucko.me>
 *  Copyright (c) contributors
 *
 *  Permission is hereby granted, free of charge, to any person obtaining a copy
 *  of this software and associated documentation files (the "Software"), to deal
 *  in the Software without restriction, including without limitation the rights
 *  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  copies of the Software, and to permit persons to whom the Software is
 *  furnished to do so, subject to the following conditions:
 *
 *  The above copyright notice and this permission notice shall be included in all
 *  copies or substantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 *  SOFTWARE.
 */

package me.lucko.gchat;

import com.velocitypowered.api.command.Command;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.CommandManager;
import com.velocitypowered.api.command.CommandMeta;
import com.velocitypowered.api.command.SimpleCommand;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;

import java.util.Collections;
import java.util.List;

public class GChatCommand implements SimpleCommand {
    private static final TextComponent PREFIX = Component.text("[").color(NamedTextColor.GRAY).decoration(TextDecoration.BOLD, true)
            .append(Component.text("gChat").color(NamedTextColor.DARK_RED).decoration(TextDecoration.BOLD, true))
            .append(Component.text("]").color(NamedTextColor.GRAY).decoration(TextDecoration.BOLD, true))
            .append(Component.text(" ").decoration(TextDecoration.BOLD, false));

    private final GChatPlugin plugin;

    public GChatCommand(GChatPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void execute(SimpleCommand.Invocation invocation) {
        CommandSource source = invocation.source();
        String[] args = invocation.arguments();

        if (args.length == 0) {
            TextComponent versionMsg = PREFIX.append(Component.text("Running gChat for Velocity ").color(NamedTextColor.RED).decoration(TextDecoration.BOLD, false))
                    .append(Component.text("v" + plugin.getDescription().getVersion().get()).color(NamedTextColor.WHITE).decoration(TextDecoration.BOLD, false))
                    .append(Component.text(".").color(NamedTextColor.RED).decoration(TextDecoration.BOLD, false));

            source.sendMessage(versionMsg);
            return;
        }

        String subCommand = args[0].toLowerCase();

        if (subCommand.equals("reload") && source.hasPermission("gchat.command.reload")) {
            boolean result = plugin.reloadConfig();

            TextComponent reloadMsg;
            if (result) {
                reloadMsg = PREFIX.append(Component.text("Reload successful.").color(NamedTextColor.GREEN).decoration(TextDecoration.BOLD, false));
            } else {
                reloadMsg = PREFIX.append(Component.text("Reload failed. Check the console for errors").color(NamedTextColor.RED).decoration(TextDecoration.BOLD, false));
            }

            source.sendMessage(reloadMsg);
            return;
        }

        TextComponent unknownCommand = PREFIX.append(Component.text("Unknown sub command.").color(NamedTextColor.WHITE).decoration(TextDecoration.BOLD, false));
        source.sendMessage(unknownCommand);
    }

    @Override
    public List<String> suggest(Invocation invocation) {
        return Collections.emptyList();
    }
}
