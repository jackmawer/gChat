package me.lucko.gchat.tab;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.event.player.ServerConnectedEvent;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.player.TabList;
import com.velocitypowered.api.proxy.player.TabListEntry;
import me.lucko.gchat.GChatPlugin;
import me.lucko.gchat.config.GChatConfig;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

import java.util.*;

public class GChatTabList {

    public static GChatTabList instance = null;

    public final ProxyServer proxy_server;
    public final GChatPlugin plugin;

    private String tablist_header = null;
    private String tablist_footer = null;

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
    }

    public void updatePlayers() {

        LegacyComponentSerializer legacy = LegacyComponentSerializer.legacyAmpersand();

        for (Player player : this.proxy_server.getAllPlayers()) {

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

                    player.getTabList().addEntry(
                            TabListEntry.builder()
                                    .displayName(Component.text(player1.getUsername() + "»«_"))
                                    .profile(player1.getGameProfile())
                                    .gameMode(0) // Impossible to get player game mode from proxy, always assume survival
                                    .tabList(player.getTabList())
                                    .build()
                    );
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
                }
            }
        }
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
