package me.lucko.gchat.commands;

import com.google.common.collect.ImmutableList;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.event.player.PlayerChatEvent;
import com.velocitypowered.api.proxy.Player;
import me.lucko.gchat.GChat;
import me.lucko.gchat.GChatPlayer;
import me.lucko.gchat.GChatPlugin;
import me.lucko.gchat.api.ChatFormat;
import me.lucko.gchat.config.GChatConfig;
import me.xdrop.fuzzywuzzy.FuzzySearch;
import me.xdrop.fuzzywuzzy.model.ExtractedResult;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

import java.util.*;

/**
 * The custom whisper/msg command
 *
 * @author   Jelle De Loecker
 * @since    3.1.0
 */
public class WhisperCommand implements SimpleCommand  {

    private final GChatPlugin plugin;
    private final LegacyComponentSerializer legacyLinkingSerializer;

    public WhisperCommand() {
        this.plugin = GChatPlugin.instance;
        this.legacyLinkingSerializer = LegacyComponentSerializer.builder()
                .character('&')
                .extractUrls(plugin.getConfig().getLinkStyle())
                .build();
    }

    /**
     * Execute a whisper command
     *
     * @author   Jelle De Loecker
     * @since    3.1.0
     */
    @Override
    public void execute(Invocation invocation) {

        CommandSource source = invocation.source();

        if (!(source instanceof Player player)) {
            return;
        }

        // Get the arguments after the command alias
        String[] args = invocation.arguments();

        if (args.length == 0) {
            return;
        }

        if (args.length == 1) {
            source.sendMessage(Component.text("Your message did not contain any text").color(NamedTextColor.RED));
            return;
        }

        String name = args[0];
        GChatPlayer receiver = GChatPlayer.getByDisplayName(name);

        if (receiver == null) {
            source.sendMessage(Component.text("Failed to find that player!").color(NamedTextColor.RED));
            return;
        }

        GChatPlayer sender = GChatPlayer.get(player);
        String message = String.join(" ", Arrays.copyOfRange(args, 1, args.length));

        Map<String, String> parameters = new HashMap<>();
        parameters.put("message", MiniMessage.miniMessage().escapeTags(message));
        parameters.put("receiver", receiver.getDisplayName());
        parameters.put("sender", sender.getDisplayName());

        TextComponent outgoing_message = sender.format("whisper-out", parameters);
        TextComponent incoming_message = receiver.format("whisper", parameters);

        if (outgoing_message == null) {
            source.sendMessage(Component.text("You can't whisper!").color(NamedTextColor.RED));
            return;
        }

        if (incoming_message == null) {
            source.sendMessage(Component.text(receiver.getDisplayName() + " can't receive whispers!").color(NamedTextColor.RED));
            return;
        }

        source.sendMessage(outgoing_message);
        receiver.sendMessage(incoming_message);
    }

    /**
     * Suggest (nick)names to whisper to
     *
     * @author   Jelle De Loecker
     * @since    3.1.0
     */
    public List<String> suggest(final SimpleCommand.Invocation invocation) {

        List<String> args = Arrays.asList(invocation.arguments());

        int argument_count = args.size();

        if (argument_count == 0) {
            return this.getNames();
        }

        if (argument_count > 1) {
            return ImmutableList.of();
        }

        String query = args.get(0);
        List<String> results = GChat.searchList(query, this.getNames());

        return results;
    }

    /**
     * Get all (nick)names of players
     *
     * @author   Jelle De Loecker
     * @since    3.1.0
     */
    public List<String> getNames() {
        List<String> names = new ArrayList<>();

        for (Player player : GChatPlugin.instance.getProxy().getAllPlayers()) {
            GChatPlayer gChatPlayer = GChatPlayer.get(player);

            if (gChatPlayer.getNickname() != null) {
                names.add(gChatPlayer.getNickname());
            } else {
                names.add(player.getUsername());
            }
        }

        return names;
    }
}
