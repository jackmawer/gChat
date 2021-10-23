package me.lucko.gchat;

import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ServerConnection;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.user.User;
import net.luckperms.api.node.NodeType;
import net.luckperms.api.node.types.MetaNode;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.TimeZone;
import java.util.UUID;

public class GChatPlayer {

    public final Player player;
    public final LuckPerms luckperms;
    public final User user;

    protected String pronouns = null;
    protected String timezone = null;
    protected String nickname = null;
    protected String nickname_color = null;
    protected DateFormat date_format = null;

    public static final HashMap<UUID, GChatPlayer> CACHE = new HashMap<>();

    public GChatPlayer(Player player) {
        this.player = player;
        this.luckperms = LuckPermsProvider.get();
        this.user = luckperms.getPlayerAdapter(Player.class).getUser(player);
        this.init();
    }

    private void init() {
        this.pronouns = this.getMetaValue("pronouns");
        this.timezone = this.getMetaValue("timezone");
        this.nickname = this.getMetaValue("nickname");
        this.nickname_color = this.getMetaValue("nickname_color");
    }

    public String getDisplayName() {

        if (this.nickname == null || this.nickname.isBlank()) {
            return this.player.getUsername();
        }

        String nickname = this.nickname;

        if (this.nickname_color != null) {
            nickname = this.nickname_color + nickname;
        }

        return nickname;
    }

    public String getNicknameColor() {
        return this.nickname_color;
    }

    public void setNicknameColor(String color_code) {
        this.nickname_color = color_code;
        this.setMetaNode("nickname_color", color_code);
    }

    public String getNickname() {
        return this.nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
        this.setMetaNode("nickname", nickname);
    }

    public String getPronouns() {
        return this.pronouns;
    }

    public void setPronouns(String pronouns) {
        this.pronouns = pronouns;
        this.setMetaNode("pronouns", pronouns);
    }

    public String getTimezone() {
        return this.timezone;
    }

    public void setTimezone(String timezone) {
        this.timezone = timezone;
        this.setMetaNode("timezone", timezone);
        this.updateTimeZone();
    }

    protected void updateTimeZone() {
        if (this.timezone != null) {
            try {
                this.date_format.setTimeZone(TimeZone.getTimeZone(this.timezone));
            } catch (Exception e) {
                // Ignore
            }
        }
    }

    public DateFormat getDateFormat() {

        if (this.date_format == null) {
            this.date_format = new SimpleDateFormat("HH:mm");
            this.updateTimeZone();
        }

        return this.date_format;
    }

    public String getCurrentTime() {
        Date now = new Date();
        return this.getDateFormat().format(now);
    }

    /**
     * Get a metadata node
     * @param   name   The name of the value to get
     */
    public String getMetaValue(String name) {
        return luckperms.getPlayerAdapter(Player.class).getMetaData(this.player).getMetaValue(name);
    }

    /**
     * Remove a meta node (always needed when a value should be set)
     * @param   name   The name of the value to remove
     */
    protected void removeMetaNode(String name) {
        // clear any existing meta nodes with the same key - we want to override
        user.data().clear(NodeType.META.predicate(mn -> mn.getMetaKey().equals(name)));
    }

    /**
     * Set a meta node value
     *
     * @param name   The name of the node to set
     * @param value  The actual value
     */
    public void setMetaNode(String name, String value) {
        this.removeMetaNode(name);

        if (value != null) {
            MetaNode value_node = MetaNode.builder(name, value).build();
            user.data().add(value_node);
        }

        luckperms.getUserManager().saveUser(user);
    }

    /**
     * Get this player's server
     */
    public ServerConnection getServer() {
        return this.player.getCurrentServer().orElse(null);
    }

    /**
     * Get the MSPT of the server this player is on
     */
    public float getMSPT() {

        ServerConnection server = this.getServer();

        if (server == null) {
            return 0f;
        }

        return GChatPlugin.instance.getServerMSPT(server);
    }

    /**
     * Get the TPS of the server this player is on
     */
    public float getTPS() {

        ServerConnection server = this.getServer();

        if (server == null) {
            return 20f;
        }

        return GChatPlugin.instance.getServerTPS(server);
    }

    /**
     * Get the ping of this player as a string
     */
    public String getPingString() {
        return "" + this.player.getPing();
    }

    public static GChatPlayer get(Player player) {

        GChatPlayer result = GChatPlayer.get(player.getUniqueId());

        if (result == null) {
            result = new GChatPlayer(player);
            CACHE.put(player.getUniqueId(), result);
        }

        return result;
    }

    public static GChatPlayer get(UUID player_uuid) {

        if (CACHE.containsKey(player_uuid)) {
            return CACHE.get(player_uuid);
        }

        return null;
    }

}
