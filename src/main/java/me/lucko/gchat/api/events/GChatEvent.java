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

package me.lucko.gchat.api.events;

import com.google.common.base.Objects;
import com.velocitypowered.api.event.ResultedEvent;
import com.velocitypowered.api.event.player.PlayerChatEvent;
import com.velocitypowered.api.proxy.Player;

/**
 * Called just before gChat is going to handle a chat event from a player.
 */
public class GChatEvent implements ResultedEvent<PlayerChatEvent.ChatResult> {

    private final Player sender;
    private final PlayerChatEvent chatEvent;

    private PlayerChatEvent.ChatResult result;

    public GChatEvent(Player sender, PlayerChatEvent chatEvent) {
        this.sender = sender;
        this.chatEvent = chatEvent;
        this.result = chatEvent.getResult();
    }

    public Player getSender() {
        return this.sender;
    }

    public PlayerChatEvent getChatEvent() {
        return this.chatEvent;
    }

    public PlayerChatEvent.ChatResult getResult() {
        return this.result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GChatEvent that = (GChatEvent) o;
        return Objects.equal(sender, that.sender) && Objects.equal(chatEvent, that.chatEvent) && Objects.equal(result, that.result);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(sender, chatEvent, result);
    }

    public String toString() {
        return "GChatEvent(sender=" + this.getSender() + ", chatEvent=" + this.getChatEvent() + ", result=" + this.getResult() + ")";
    }

    public void setResult(PlayerChatEvent.ChatResult result) {
        this.result = result;
    }
}
