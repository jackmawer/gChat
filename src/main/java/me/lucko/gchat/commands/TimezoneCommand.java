package me.lucko.gchat.commands;

import com.google.common.collect.ImmutableList;
import com.google.gson.JsonObject;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import me.lucko.gchat.GChatPlayer;
import me.lucko.gchat.GChatPlugin;
import me.xdrop.fuzzywuzzy.FuzzySearch;
import me.xdrop.fuzzywuzzy.model.ExtractedResult;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.TimeZone;

public class TimezoneCommand implements SimpleCommand {

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

        String timezone = args[0];

        List<String> timezones = Arrays.asList(TimeZone.getAvailableIDs());

        if (!timezones.contains(timezone)) {
            source.sendMessage(Component.text("Failed to find that timezone!").color(NamedTextColor.RED));
            return;
        }

        GChatPlayer gChatPlayer = GChatPlayer.get(player);
        gChatPlayer.setTimezone(timezone);

        if (GChatPlugin.shouldPushEvents()) {
            JsonObject object = GChatPlugin.createObject("timezone", player);
            object.addProperty("timezone", timezone);
            GChatPlugin.pushEvent(object);
        }

        source.sendMessage(Component.text("Your timezone has been set to " + timezone).color(NamedTextColor.AQUA));
    }

    public List<String> suggest(final SimpleCommand.Invocation invocation) {
        List<String> args = Arrays.asList(invocation.arguments());
        List<String> timezones = Arrays.asList(TimeZone.getAvailableIDs());

        int size = args.size();

        if (size > 1) {
            return ImmutableList.of();
        }

        if (size > 0) {
            String query = args.get(0);
            List<ExtractedResult> filtered = FuzzySearch.extractSorted(query, timezones, 80);

            timezones = new ArrayList<>();

            if (filtered.size() > 0) {
                for (ExtractedResult entry : filtered) {
                    timezones.add(entry.getString());
                }
            }
        }

        return timezones;
    }
}
