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

package me.lucko.gchat.placeholder;

import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ServerConnection;
import me.lucko.gchat.GChatPlayer;
import me.lucko.gchat.api.Placeholder;

public class StandardPlaceholders implements Placeholder {

    private static final String TPS_FORMAT = "%.1f";
    private static final String MSPT_FORMAT = "%.1f";

    @Override
    public String getReplacement(Player player, String definition) {

        // dynamic placeholders
        if (definition.startsWith("has_perm_") && definition.length() > "has_perm_".length()) {
            String perm = definition.substring("has_perm_".length());
            return Boolean.toString(player.hasPermission(perm));
        }

        String result = null;

        // static placeholders
        switch (definition.toLowerCase()) {
            case "username":
                return player.getUsername();
            case "name":
            case "display_username": // Velocity doesn't have display names
            case "display_name":
                result = GChatPlayer.get(player).getDisplayName();
                break;
            case "server_name":
                ServerConnection connection = player.getCurrentServer().orElse(null);

                if (connection == null) {
                    return "unknown";
                }

                return connection.getServerInfo().getName();
            case "uuid":
                return player.getUniqueId().toString();
            case "pronouns":
                result = GChatPlayer.get(player).getPronouns();
                break;
            case "pronouns_suffix":
                result = GChatPlayer.get(player).getPronouns();

                if (result != null && !result.isBlank()) {
                    result = " (" + result + ")";
                }

                break;
            case "timezone":
                result = GChatPlayer.get(player).getTimezone();
                break;
            case "now":
                result = GChatPlayer.get(player).getCurrentTime();
                break;
            case "mspt":
                result = String.format(MSPT_FORMAT, GChatPlayer.get(player).getMSPT());
                break;
            case "tps":
                result = String.format(TPS_FORMAT, GChatPlayer.get(player).getTPS());
                break;
            case "ping":
                result = GChatPlayer.get(player).getPingString();
                break;
            default:
                return null;
        }

        if (result == null) {
            result = "";
        }

        return result;
    }
}
