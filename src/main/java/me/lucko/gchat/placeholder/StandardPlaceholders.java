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
import me.lucko.gchat.api.Placeholder;

public class StandardPlaceholders implements Placeholder {

    @Override
    public String getReplacement(Player player, String definition) {

        // dynamic placeholders
        if (definition.startsWith("has_perm_") && definition.length() > "has_perm_".length()) {
            String perm = definition.substring("has_perm_".length());
            return Boolean.toString(player.hasPermission(perm));
        }

        // static placeholders
        switch (definition.toLowerCase()) {
            case "username":
            case "name":
            case "display_username": // Velocity doesn't have display names
            case "display_name":
                return player.getUsername();
            case "server_name":
                return player.getCurrentServer().get().getServerInfo().getName();
            case "uuid":
                return player.getUniqueId().toString();
            default:
                return null;
        }
    }
}
