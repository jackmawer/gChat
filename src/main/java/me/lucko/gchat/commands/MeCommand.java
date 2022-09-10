package me.lucko.gchat.commands;

import com.velocitypowered.api.command.SimpleCommand;
import me.lucko.gchat.GChatPlayer;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.minimessage.MiniMessage;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * The custom `me` command
 *
 * @author   Jelle De Loecker
 * @since    3.1.0
 */
public class MeCommand implements SimpleCommand {

    private final String format;

    public MeCommand(String format) {
        this.format = format;
    }

    @Override
    public void execute(Invocation invocation) {

        GChatPlayer player = GChatPlayer.get(invocation.source());

        if (player == null) {
            System.out.println("Unable to do MeCommand of format " + this.format + ", player is null");
            return;
        }

        // Get the arguments after the command alias
        String[] args = invocation.arguments();

        String message = String.join(" ", Arrays.copyOfRange(args, 0, args.length));

        Map<String, String> parameters = new HashMap<>();
        parameters.put("message", MiniMessage.miniMessage().escapeTags(message));

        TextComponent me_message = player.format(this.format, parameters);
        player.broadcast(me_message, true);
    }
}
