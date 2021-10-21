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

package me.lucko.gchat.hooks;

import com.velocitypowered.api.proxy.Player;
import me.lucko.gchat.api.Placeholder;
import me.lucko.luckperms.placeholders.LPPlaceholderProvider;
import me.lucko.luckperms.placeholders.PlaceholderPlatform;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;

public class LuckPermsHook implements Placeholder, PlaceholderPlatform {
    private final LuckPerms luckPerms;
    private final LPPlaceholderProvider provider;

    public LuckPermsHook() {
        this.luckPerms = LuckPermsProvider.get();
        this.provider = new LPPlaceholderProvider(this, this.luckPerms);
    }

    @Override
    public String getReplacement(Player player, String identifier) {
        if (identifier.startsWith("lp_")) {
            identifier = identifier.substring("lp_".length());
        } else if (identifier.startsWith("luckperms_")) {
            identifier = identifier.substring("luckperms_".length());
        } else {
            return null;
        }

        if (player == null || this.provider == null) {
            return "";
        }

        return this.provider.onPlaceholderRequest(player, player.getUniqueId(), identifier);
    }

    @Override
    public String formatBoolean(boolean b) {
        return b ? "yes" : "no";
    }

}
