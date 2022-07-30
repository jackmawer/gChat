package me.lucko.gchat.hooks;

import com.google.common.io.ByteArrayDataInput;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.PluginMessageEvent;
import com.velocitypowered.api.proxy.ServerConnection;
import com.velocitypowered.api.proxy.messages.ChannelIdentifier;
import me.lucko.gchat.GChatPlugin;

import java.util.UUID;

public class PluginMessageHook {

    public final GChatPlugin plugin;

    public PluginMessageHook(GChatPlugin plugin) {
        this.plugin = plugin;
    }

    @Subscribe
    public void onPluginMessage(PluginMessageEvent e) {

        ChannelIdentifier identifier = e.getIdentifier();

        if (identifier.equals(GChatPlugin.GCHAT_CHANNEL)) {
            this.handleGchat(e);
            return;
        }

        if (identifier.equals(GChatPlugin.SERVER_MOVE_CHANNEL)) {
            this.handleServerMove(e);
            return;
        }
    }

    private void handleGchat(PluginMessageEvent e) {

        if (!(e.getSource() instanceof ServerConnection server)) {
            return;
        }

        byte[] data = e.getData();
        ByteArrayDataInput packet = e.dataAsDataStream();

        int version = packet.readInt();
        float mspt = packet.readFloat();
        float tps = packet.readFloat();
        int load;

        if (version > 0) {
            load = packet.readInt();
        } else {
            load = (int) ((mspt / 50) * 100);
        }

        GChatPlugin.instance.registerTicks(server, mspt, tps, load);
    }

    private void handleServerMove(PluginMessageEvent e) {

        if (!(e.getSource() instanceof ServerConnection server)) {
            return;
        }

        byte[] data = e.getData();
        ByteArrayDataInput packet = e.dataAsDataStream();

        int version = packet.readInt();
        long uuid_most = packet.readLong();
        long uuid_least = packet.readLong();
        UUID uuid = new UUID(uuid_most, uuid_least);

        int array_length = packet.readInt();
        String target_server = packet.readUTF();
        //String username = packet.readUTF();

        System.out.println("Server move: " + target_server + " for " + uuid);

        //((ServerConnection) e.getSource()).getPlayer().



    }

    private boolean isBlackblockMessage(ChannelIdentifier identifier) {

        if (identifier.equals(GChatPlugin.GCHAT_CHANNEL)) {
            return true;
        }

        if (identifier.equals(GChatPlugin.SERVER_MOVE_CHANNEL)) {
            return true;
        }

        return false;
    }
}
