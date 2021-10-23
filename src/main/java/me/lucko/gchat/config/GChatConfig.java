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

package me.lucko.gchat.config;

import com.google.common.collect.ImmutableList;
import com.google.common.reflect.TypeToken;
import me.lucko.gchat.GChatPlugin;
import me.lucko.gchat.api.ChatFormat;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextDecoration;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GChatConfig {
    private static final Style DEFAULT_LINK_STYLE = Style.style(NamedTextColor.WHITE, TextDecoration.UNDERLINED);
    private final boolean passthrough;
    private final boolean requireSendPermission;
    private final Component requireSendPermissionFailMessage;
    private final boolean requireReceivePermission;
    private final boolean requirePermissionPassthrough;
    private final boolean logChatGlobal;
    private final List<ChatFormat> formats;
    private final Style linkStyle;
    private final String tablist_header;
    private final String tablist_footer;
    private final Boolean has_tablist_config;

    public GChatConfig(ConfigurationNode c) {
        this.passthrough = c.getNode("passthrough").getBoolean(true);

        this.logChatGlobal = c.getNode("log-chat-global").getBoolean(true);

        ConfigurationNode requirePermission = c.getNode("require-permission");
        if (requirePermission.isVirtual()) {
            throw new IllegalArgumentException("Missing section: require-permission");
        }

        ConfigurationNode tablist = c.getNode("tablist");

        if (!tablist.isEmpty()) {

            this.tablist_header = this.getLinesAsString(tablist.getNode("header"));
            this.tablist_footer = this.getLinesAsString(tablist.getNode("footer"));

            if (this.tablist_header != null || this.tablist_footer != null) {
                this.has_tablist_config = true;
            } else {
                this.has_tablist_config = false;
            }
        } else {
            this.tablist_header = null;
            this.tablist_footer = null;
            this.has_tablist_config = false;
        }

        this.requireSendPermission = requirePermission.getNode("send").getBoolean(false);

        String failMsg = getStringNonNull(requirePermission, "send-fail");
        if (failMsg.isEmpty()) {
            requireSendPermissionFailMessage = null;
        } else {
            requireSendPermissionFailMessage = GChatPlugin.LEGACY_LINKING_SERIALIZER.deserialize(failMsg);
        }

        this.requireReceivePermission = requirePermission.getNode("receive").getBoolean(false);
        this.requirePermissionPassthrough = requirePermission.getNode("passthrough").getBoolean(true);

        ConfigurationNode formatsSection = c.getNode("formats");
        if (formatsSection.isVirtual()) {
            throw new IllegalArgumentException("Missing section: formats");
        }

        Map<String, ChatFormat> currentFormats = new HashMap<>();
        for (Object id : formatsSection.getChildrenMap().keySet()) {
            ConfigurationNode formatSection = formatsSection.getNode(id);
            if (formatSection.isVirtual() || !(id instanceof String)) {
                continue;
            }

            String key = (String) id;

            currentFormats.put(key.toLowerCase(), new ChatFormat(key.toLowerCase(), formatSection));
        }

        List<ChatFormat> formatsList = new ArrayList<>(currentFormats.values());
        formatsList.sort(Comparator.comparingInt(ChatFormat::getPriority).reversed());

        this.formats = ImmutableList.copyOf(formatsList);

        Style currentLinkStyle;
        try {
            //noinspection UnstableApiUsage
            currentLinkStyle = c.getNode("link-style").getValue(TypeTokens.STYLE, DEFAULT_LINK_STYLE);
        } catch (ObjectMappingException e) {
            currentLinkStyle = DEFAULT_LINK_STYLE;
        }

        this.linkStyle = currentLinkStyle;
    }

    public static String getStringNonNull(ConfigurationNode configuration, String path) throws IllegalArgumentException {
        String ret = configuration.getNode(path).getString();
        if (ret == null) {
            throw new IllegalArgumentException("Missing string value at '" + path + "'");
        }

        return ret;
    }

    private String getLinesAsString(ConfigurationNode node) {

        if (node.isEmpty()) {
            return null;
        }

        if (node.isList()) {

            try {
                List<String> list = node.getList(new TypeToken<String>() {});
                return String.join("\n", list);
            } catch (Exception e) {
                // Ignore
                return null;
            }

        } else {
            return node.getString("");
        }
    }

    public boolean hasTablistConfig() {
        return this.has_tablist_config;
    }

    public String getTablistHeader() {
        return this.tablist_header;
    }

    public String getTablistFooter() {
        return this.tablist_footer;
    }

    public boolean isPassthrough() {
        return this.passthrough;
    }

    public boolean isRequireSendPermission() {
        return this.requireSendPermission;
    }

    public Component getRequireSendPermissionFailMessage() {
        return this.requireSendPermissionFailMessage;
    }

    public boolean isRequireReceivePermission() {
        return this.requireReceivePermission;
    }

    public boolean isRequirePermissionPassthrough() {
        return this.requirePermissionPassthrough;
    }

    public boolean isLogChatGlobal() {
        return this.logChatGlobal;
    }

    public List<ChatFormat> getFormats() {
        return this.formats;
    }

    public Style getLinkStyle() {
        return this.linkStyle;
    }

    public String toString() {
        return "GChatConfig(passthrough=" + this.isPassthrough() + ", requireSendPermission=" + this.isRequireSendPermission() + ", requireSendPermissionFailMessage=" + this.getRequireSendPermissionFailMessage() + ", requireReceivePermission=" + this.isRequireReceivePermission() + ", requirePermissionPassthrough=" + this.isRequirePermissionPassthrough() + ", logChatGlobal=" + this.isLogChatGlobal() + ", formats=" + this.getFormats() + ", linkStyle=" + this.getLinkStyle() + ")";
    }
}
