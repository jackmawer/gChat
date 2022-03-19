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
        this.updatePlayers();
        this.updateHeaderAndFooter();

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

        Boolean players_changed = false;
        JsonObject event_data = null;
        JsonArray event_list = null;

        LegacyComponentSerializer legacy = LegacyComponentSerializer.legacyAmpersand();

        for (Player player : this.proxy_server.getAllPlayers()) {

            String current_server_name = null;

            ServerConnection connection = player.getCurrentServer().orElse(null);

            if (connection != null) {
                current_server_name = connection.getServer().getServerInfo().getName();
            }

            TabList tablist = player.getTabList();

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

            for (Player player1 : this.proxy_server.getAllPlayers()) {
                if (!player.getTabList().containsEntry(player1.getUniqueId())) {
                    players_changed = true;

                    Component display_name = Component.text(GChatPlayer.get(player).getDisplayName());
                    ServerConnection connection1 = player1.getCurrentServer().orElse(null);

                    if (connection1 != null) {
                        String server_name = connection1.getServer().getServerInfo().getName();

                        if (!server_name.equals(current_server_name)) {
                            display_name = display_name.append(Component.text(" (" + server_name + ")").style(Style.style(NamedTextColor.GRAY)));
                        }
                    }

                    TabListEntry entry = TabListEntry.builder()
                            // Setting a displayname here will only work if the players are on different servers
                            //.displayName(display_name)
                            .profile(player1.getGameProfile())
                            .gameMode(0) // Impossible to get player game mode from proxy, always assume survival
                            .tabList(player.getTabList())
                            .build();

                    entry.setDisplayName(display_name);

                    player.getTabList().addEntry(entry);
                }

                /*
                TabListEntry entry =         TabListEntry.builder()
                                .displayName(Component.text(player1.getUsername() + "»«_"))
                                .profile(player1.getGameProfile())
                                .gameMode(0) // Impossible to get player game mode from proxy, always assume survival
                                .tabList(player.getTabList())
                                .build();

                entry.setDisplayName(Component.text("Awel?"));

                insertIntoTabListCleanly(player.getTabList(), entry);
                */

            }

            for (TabListEntry entry : player.getTabList().getEntries()) {
                UUID uuid = entry.getProfile().getId();
                Optional<Player> playerOptional = proxy_server.getPlayer(uuid);
                if (playerOptional.isPresent()) {
                    // Update ping
                    entry.setLatency((int) (player.getPing()));
                } else {
                    player.getTabList().removeEntry(uuid);
                    players_changed = true;
                }
            }
        }

        return players_changed;
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
