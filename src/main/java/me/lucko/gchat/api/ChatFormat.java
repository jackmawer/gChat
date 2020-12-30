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

package me.lucko.gchat.api;

import com.velocitypowered.api.proxy.Player;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;
import net.kyori.adventure.text.event.ClickEvent;
import ninja.leaping.configurate.ConfigurationNode;

import static me.lucko.gchat.config.GChatConfig.getStringNonNull;

/**
 * Represents a chat format
 */
@Getter
@ToString
@AllArgsConstructor
public class ChatFormat {

    private final String id;
    private final int priority;
    private final boolean checkPermission;
    private final String formatText;
    private final String hoverText;
    private final ClickEvent.Action clickType;
    private final String clickValue;

    public ChatFormat(String id, ConfigurationNode c) {
        this.id = id;
        this.priority = c.getNode("priority").getInt(0);
        this.checkPermission = c.getNode("check-permission").getBoolean(true);
        this.formatText = getStringNonNull(c, "format");

        String hoverText = null;
        ClickEvent.Action clickType = null;
        String clickValue = null;

        ConfigurationNode extra = c.getNode("format-extra");
        if (!extra.isVirtual()) {
            String hover = extra.getNode("hover").getString();
            if (hover != null && !hover.isEmpty()) {
                hoverText = hover;
            }

            ConfigurationNode click = extra.getNode("click");
            if (!click.isVirtual()) {
                String type = click.getNode("type").getString("none").toLowerCase();
                String value = click.getNode("value").getString();

                if (!type.equals("none") && value != null) {
                    if (!type.equals("suggest_command") && !type.equals("run_command") && !type.equals("open_url")) {
                        throw new IllegalArgumentException("Invalid click type: " + type);
                    }

                    clickType = ClickEvent.Action.valueOf(type.toUpperCase());
                    clickValue = value;
                }
            }
        }

        this.hoverText = hoverText;
        this.clickType = clickType;
        this.clickValue = clickValue;
    }

    public boolean canUse(Player player) {
        return !checkPermission || player.hasPermission("gchat.format." + id);
    }

}
