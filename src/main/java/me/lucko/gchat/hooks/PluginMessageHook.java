package me.lucko.gchat.hooks;

import com.google.common.io.ByteArrayDataInput;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.PluginMessageEvent;
import com.velocitypowered.api.proxy.ServerConnection;
import me.lucko.gchat.GChatPlayer;
import me.lucko.gchat.GChatPlugin;

public class PluginMessageHook {

    public final GChatPlugin plugin;

    public PluginMessageHook(GChatPlugin plugin) {
        this.plugin = plugin;
    }

    @Subscribe
    public void onPluginMessage(PluginMessageEvent e) {

        if (!e.getIdentifier().equals(GChatPlugin.GCHAT_CHANNEL)) {
            return;
        }

        if (!(e.getSource() instanceof ServerConnection server)) {
            return;
        }

        byte[] data = e.getData();
        ByteArrayDataInput packet = e.dataAsDataStream();

        int version = packet.readInt();
        float mspt = packet.readFloat();
        float tps = packet.readFloat();

        GChatPlugin.instance.registerTicks(server, mspt, tps);
    }
}
