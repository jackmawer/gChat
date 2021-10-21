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

import com.velocitypowered.api.event.PostOrder;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.event.connection.LoginEvent;
import com.velocitypowered.api.event.player.PlayerChatEvent;
import com.velocitypowered.api.event.player.ServerConnectedEvent;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.ServerConnection;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import com.velocitypowered.api.proxy.server.ServerInfo;
import me.lucko.gchat.api.ChatFormat;
import me.lucko.gchat.api.events.GChatEvent;
import me.lucko.gchat.api.events.GChatMessageFormedEvent;
import me.lucko.gchat.api.events.GChatMessageSendEvent;
import me.lucko.gchat.config.GChatConfig;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;

import java.util.regex.Pattern;

public class GChatListener {
    private static final Pattern STRIP_COLOR_PATTERN = Pattern.compile("(?i)([§&])[0-9A-FK-OR]");

    private final GChatPlugin plugin;
    private final LegacyComponentSerializer legacyLinkingSerializer;

    public GChatListener(GChatPlugin plugin) {
        this.plugin = plugin;
        this.legacyLinkingSerializer = LegacyComponentSerializer.builder()
                .character('&')
                .extractUrls(plugin.getConfig().getLinkStyle())
                .build();

    }

    /**
     * Someone has connected to the network
     */
    @Subscribe(order = PostOrder.NORMAL)
    public void onLogin(LoginEvent e) {
        Player player = e.getPlayer();

        ChatFormat format = plugin.getFormat(player, "login").orElse(null);

        if (format == null) {
            return;
        }

        TextComponent message = this.createFormattedText(player, format, "");

        this.broadcastMessage(message);
    }

    @Subscribe(order = PostOrder.NORMAL)
    public void onJoinServer(ServerConnectedEvent e) {
        Player player = e.getPlayer();
        RegisteredServer server = e.getServer();
        ServerInfo info = server.getServerInfo();

        ChatFormat format = plugin.getFormat(player, "join").orElse(null);

        if (format == null) {
            return;
        }

        TextComponent message = this.createFormattedText(player, server, format, "");

        this.broadcastMessage(message);
    }

    @Subscribe(order = PostOrder.NORMAL)
    public void onLogout(DisconnectEvent e) {
        Player player = e.getPlayer();

        ChatFormat format = plugin.getFormat(player, "logout").orElse(null);

        if (format == null) {
            return;
        }

        TextComponent message = this.createFormattedText(player, format, "");

        this.broadcastMessage(message);
    }

    /**
     * Listen for PlayerChat events and broadcast them to every server
     */
    @Subscribe(order = PostOrder.NORMAL)
    public void onChat(PlayerChatEvent e) {
        Player player = e.getPlayer();
        ProxyServer proxy = plugin.getProxy();

        GChatEvent gChatEvent = new GChatEvent(player, e);
        proxy.getEventManager().fire(gChatEvent).join();

        if (!gChatEvent.getResult().isAllowed()) {
            return;
        }

        GChatConfig config = plugin.getConfig();

        // are permissions required to send chat messages?
        // does the player have perms to send the message
        if (config.isRequireSendPermission() && !player.hasPermission("gchat.send")) {

            // if the message should be passed through when the player doesn't have the permission
            if (config.isRequirePermissionPassthrough()) {
                // just return. the default behaviour is for the message to be passed to the backend.
                return;
            }

            // they don't have permission, and the message shouldn't be passed to the backend.
            e.setResult(PlayerChatEvent.ChatResult.denied());

            Component failMessage = config.getRequireSendPermissionFailMessage();
            if (failMessage != null) {
                player.sendMessage(failMessage);
            }

            return;
        }

        ChatFormat format = plugin.getFormat(player).orElse(null);

        // couldn't find a format for the player
        if (format == null) {
            if (!config.isPassthrough()) {
                e.setResult(PlayerChatEvent.ChatResult.denied());
            }

            return;
        }

        // we have a format, so cancel the event.
        e.setResult(PlayerChatEvent.ChatResult.denied());

        // get the actual message format, and apply replacements.
        String formatText = format.getFormatText();
        formatText = plugin.replacePlaceholders(player, formatText);

        // get any hover text, and apply replacements.
        String hover = format.getHoverText();
        hover = plugin.replacePlaceholders(player, hover);

        // get the click event type, and the value if present.
        ClickEvent.Action clickType = format.getClickType();
        String clickValue = format.getClickValue();
        if (clickType != null) {
            clickValue = plugin.replacePlaceholders(player, clickValue);
        }

        // get the players message, and remove any color if they don't have permission for it.
        String playerMessage = e.getMessage();
        if (!player.hasPermission("gchat.color")) {
            playerMessage = STRIP_COLOR_PATTERN.matcher(playerMessage).replaceAll("");
        }

        // Create text to send to ourselves
        String self_text = formatText.replace("{message}", "&b" + playerMessage);

        // apply the players message to the chat format
        formatText = formatText.replace("{message}", playerMessage);

        // apply any hover events
        HoverEvent<Component> hoverEvent = hover == null ? null : HoverEvent.showText(LegacyComponentSerializer.legacyAmpersand().deserialize(hover));
        ClickEvent clickEvent = clickType == null ? null : ClickEvent.clickEvent(clickType, clickValue);

        // convert the format to a message
        TextComponent message = legacyLinkingSerializer
                .deserialize(formatText)
                .toBuilder()
                .applyDeep(m -> {
                    Component mComponent = m.build();

                    if (hoverEvent != null && mComponent.hoverEvent() == null) {
                        m.hoverEvent(hoverEvent);
                    }
                    if (clickEvent != null && mComponent.clickEvent() == null) {
                        m.clickEvent(clickEvent);
                    }
                })
                .build();

        // convert the format to a message
        TextComponent self_message = legacyLinkingSerializer
            .deserialize(self_text)
            .toBuilder()
                .applyDeep(m -> {
                    Component mComponent = m.build();

                    if (hoverEvent != null && mComponent.hoverEvent() == null) {
                        m.hoverEvent(hoverEvent);
                    }
                    if (clickEvent != null && mComponent.clickEvent() == null) {
                        m.clickEvent(clickEvent);
                    }
                })
                .build();

        GChatMessageFormedEvent formedEvent = new GChatMessageFormedEvent(player, format, playerMessage, message);
        proxy.getEventManager().fireAndForget(formedEvent);

        if (config.isLogChatGlobal()) {
            plugin.getLogger().info(PlainTextComponentSerializer.plainText().serialize(message));
        }

        // send the message to online players
        for (Player p : proxy.getAllPlayers()) {
            boolean cancelled = plugin.getConfig().isRequireReceivePermission() && !player.hasPermission("gchat.receive");
            GChatMessageSendEvent sendEvent = new GChatMessageSendEvent(player, p, format, playerMessage, cancelled);
            proxy.getEventManager().fire(sendEvent).join();

            if (!sendEvent.getResult().isAllowed()) {
                continue;
            }

            if (player.getUniqueId().equals(p.getUniqueId())) {
                p.sendMessage(player, self_message);
            } else {
                p.sendMessage(player, message);
            }
        }
    }

    /**
     * Send to all players
     */
    public void broadcastMessage(TextComponent message) {

        ProxyServer proxy = plugin.getProxy();

        for (Player p : proxy.getAllPlayers()) {
            p.sendMessage(message);
        }
    }

    /**
     * Create a formatted message
     */
    public TextComponent createFormattedText(Player player, ChatFormat format, String message) {
        return this.createFormattedText(player, null, format, message);
    }

    /**
     * Create a formatted message
     */
    public TextComponent createFormattedText(Player player, RegisteredServer server, ChatFormat format, String message) {

        // get the actual message format, and apply replacements.
        String formatText = format.getFormatText();
        formatText = plugin.replacePlaceholders(player, formatText);

        // get any hover text, and apply replacements.
        String hover = format.getHoverText();
        hover = plugin.replacePlaceholders(player, hover);

        // get the click event type, and the value if present.
        ClickEvent.Action clickType = format.getClickType();
        String clickValue = format.getClickValue();
        if (clickType != null) {
            clickValue = plugin.replacePlaceholders(player, clickValue);
        }

        // apply the players message to the chat format
        formatText = formatText.replace("{message}", message);

        if (server != null) {
            ServerInfo info = server.getServerInfo();

            if (info != null) {
                formatText = formatText.replace("{server}", info.getName());

                if (clickValue != null) {
                    clickValue = clickValue.replace("{server}", info.getName());
                }

                if (hover != null) {
                    hover = hover.replace("{server}", info.getName());
                }
            }
        }

        // apply any hover events
        HoverEvent<Component> hoverEvent = hover == null ? null : HoverEvent.showText(LegacyComponentSerializer.legacyAmpersand().deserialize(hover));
        ClickEvent clickEvent = clickType == null ? null : ClickEvent.clickEvent(clickType, clickValue);

        // convert the format to a message
        TextComponent result = legacyLinkingSerializer
            .deserialize(formatText)
            .toBuilder()
            .applyDeep(m -> {
                Component mComponent = m.build();

                if (hoverEvent != null && mComponent.hoverEvent() == null) {
                    m.hoverEvent(hoverEvent);
                }
                if (clickEvent != null && mComponent.clickEvent() == null) {
                    m.clickEvent(clickEvent);
                }
            })
            .build();

        return result;
    }

}
