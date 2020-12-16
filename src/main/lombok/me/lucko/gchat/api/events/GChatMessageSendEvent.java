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

import com.velocitypowered.api.event.ResultedEvent;
import com.velocitypowered.api.event.player.PlayerChatEvent;
import com.velocitypowered.api.proxy.Player;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import me.lucko.gchat.api.ChatFormat;

/**
 * Called when an individual message is about to be sent to a recipient.
 */
@Getter
@EqualsAndHashCode(callSuper = false)
@ToString
public class GChatMessageSendEvent implements ResultedEvent<PlayerChatEvent.ChatResult> {

    private final Player sender;
    private final Player recipient;
    private final ChatFormat format;
    private final String rawMessage;

    @Setter
    private PlayerChatEvent.ChatResult result;

    public GChatMessageSendEvent(Player sender, Player recipient, ChatFormat format, String rawMessage, boolean cancelled) {
        this.sender = sender;
        this.recipient = recipient;
        this.format = format;
        this.rawMessage = rawMessage;
        this.result = cancelled ? PlayerChatEvent.ChatResult.denied() : PlayerChatEvent.ChatResult.allowed();
    }
}
