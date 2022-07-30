package me.lucko.gchat.tab;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.event.player.ServerConnectedEvent;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.ServerConnection;
import com.velocitypowered.api.proxy.player.TabList;
import com.velocitypowered.api.proxy.player.TabListEntry;
import me.lucko.gchat.GChatPlayer;
import me.lucko.gchat.GChatPlugin;
import me.lucko.gchat.config.GChatConfig;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

import java.util.*;

public class GChatTabList {

    public static GChatTabList instance = null;

    public final ProxyServer proxy_server;
    public final GChatPlugin plugin;

    private String tablist_header = null;
    private String tablist_footer = null;
    private int update_counter = 0;

    public GChatTabList(GChatPlugin plugin, ProxyServer proxy_server) {
        this.proxy_server = proxy_server;
        this.plugin = plugin;
        GChatTabList.instance = this;
    }

    @Subscribe
    public void connect(ServerConnectedEvent event) {
        update();
    }

    @Subscribe
    public void disconnect(DisconnectEvent event) {
        update();
    }

    /**
     * Update the Tablist's header and footer.
     * This does a first pass of replacing non-player placeholders
     */
    public void updateHeaderAndFooter() {

        GChatConfig config = this.plugin.getConfig();

        if (!config.hasTablistConfig()) {
            return;
        }

        this.tablist_header = config.getTablistHeader();
        this.tablist_footer = config.getTablistFooter();

        if (this.tablist_header != null) {
            this.tablist_header = plugin.replaceGenericPlaceholders(this.tablist_header);
        }

        if (this.tablist_footer != null) {
            this.tablist_footer = plugin.replaceGenericPlaceholders(this.tablist_footer);
        }
    }

    public void update() {
        this.updateHeaderAndFooter();
        this.updatePlayers();

        this.update_counter++;

        // Send the playerlist every 2 minutes
        if (this.update_counter > 120) {
            this.sendPlayerList();
            this.update_counter = 0;
        }
    }

    /**
     * Send the playerlist to the remote endpoint
     */
    public void sendPlayerList() {

        if (!GChatPlugin.shouldPushEvents()) {
            return;
        }

        JsonObject event_data = GChatPlugin.createObject("players");
        JsonArray event_list = new JsonArray();

        for (Player player : this.proxy_server.getAllPlayers()) {

            ServerConnection connection = player.getCurrentServer().orElse(null);


            JsonObject temp = GChatPlugin.createObject("list", player);
            event_list.add(temp.getAsJsonObject("player"));
        }

        event_data.add("players", event_list);
        GChatPlugin.pushEvent(event_data);
    }

    /**
     * Update the players in the tablist
     */
    public Boolean updatePlayers() {

        boolean players_changed = false;

        LegacyComponentSerializer legacy = LegacyComponentSerializer.legacyAmpersand();

        for (Player player : this.proxy_server.getAllPlayers()) {

            // Get this player's tablist
            TabList tablist = player.getTabList();

            // If a tablist header/footer is set, add it now
            if (this.tablist_header != null || this.tablist_footer != null) {

                String header = "";
                String footer = "";

                if (this.tablist_header != null) {
                    header = plugin.replacePlaceholders(player, this.tablist_header);
                }

                if (this.tablist_footer != null) {
                    footer = plugin.replacePlaceholders(player, this.tablist_footer);
                }

                tablist.setHeaderAndFooter(legacy.deserialize(header), legacy.deserialize(footer));
            }

            // Now iterate over all the other players
            for (Player other_player : this.proxy_server.getAllPlayers()) {

                // If this other_player is not in the tablist of the current player...
                if (!tablist.containsEntry(other_player.getUniqueId())) {
                    players_changed = true;

                    Component display_name = this.getPlayerTabDisplay(other_player, player);

                    TabListEntry entry = TabListEntry.builder()
                            // Setting a displayname here will only work if the players are on different servers
                            //.displayName(display_name)
                            .profile(other_player.getGameProfile())
                            .gameMode(0) // Impossible to get player game mode from proxy, always assume survival
                            .tabList(tablist)
                            .build();

                    entry.setDisplayName(display_name);
                    tablist.addEntry(entry);
                }
            }

            // Now iterate over all the tablist entries again!
            for (TabListEntry entry : tablist.getEntries()) {
                UUID uuid = entry.getProfile().getId();

                // See if this player is still online
                Optional<Player> playerOptional = proxy_server.getPlayer(uuid);

                // If the player is still present, update it
                if (playerOptional.isPresent()) {
                    Player other_player = playerOptional.get();

                    // Update ping
                    entry.setLatency((int) (other_player.getPing()));

                    Component display_name = this.getPlayerTabDisplay(other_player, player);
                    entry.setDisplayName(display_name);
                } else {
                    player.getTabList().removeEntry(uuid);
                    players_changed = true;
                }
            }
        }

        return players_changed;
    }

    /**
     * Construct a tablist Component entry for the given other_player
     * meant to be inserted in current_player's tablist
     *
     * @param   other_player     The other player to create the entry for
     * @param   current_player   The owner of the tablist
     *
     * @return  The constructed tablist entry
     */
    public Component getPlayerTabDisplay(Player other_player, Player current_player) {

        GChatPlayer gplayer = GChatPlayer.get(other_player);

        TextComponent display_name = gplayer.format("tab-entry", null);

        if (display_name == null) {
            return null;
        }

        ServerConnection other_connection = other_player.getCurrentServer().orElse(null);

        if (other_connection != null) {
            String server_name = other_connection.getServer().getServerInfo().getName();

            String current_server_name = null;
            ServerConnection current_connection = current_player.getCurrentServer().orElse(null);

            if (current_connection != null) {
                current_server_name = current_connection.getServer().getServerInfo().getName();
            }

            if (!server_name.equals(current_server_name)) {
                display_name = display_name.append(Component.text(" (" + server_name + ")").style(Style.style(NamedTextColor.GRAY)));
            }
        }

        return display_name;
    }

    public static void insertIntoTabListCleanly(TabList list, TabListEntry entry) {

        UUID entry_id = entry.getProfile().getId();
        List<UUID> containedUUIDs = new ArrayList<UUID>();
        Map<UUID, TabListEntry> cache = new HashMap<UUID, TabListEntry>();

        for (TabListEntry current : list.getEntries()) {
            UUID entry_profile_id = current.getProfile().getId();

            containedUUIDs.add(entry_profile_id);
            cache.put(entry_profile_id, current);
        }

        if (!containedUUIDs.contains(entry_id)) {
            list.addEntry(entry);
            return;
        } else {
            TabListEntry currentEntr = cache.get(entry_id);

            if (!currentEntr.getDisplayNameComponent().equals(entry.getDisplayNameComponent())) {
                list.removeEntry(entry_id);
                list.addEntry(entry);
            }
        }
    }

}
