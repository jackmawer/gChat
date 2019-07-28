package me.lucko.gchat.config.serializers;

import com.google.common.reflect.TypeToken;
import me.lucko.gchat.config.TypeTokens;
import net.kyori.text.Component;
import net.kyori.text.event.HoverEvent;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import ninja.leaping.configurate.objectmapping.serialize.TypeSerializer;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

public class HoverEventSerializer implements TypeSerializer<HoverEvent> {

    @Nullable
    @Override
    public HoverEvent deserialize(@NonNull TypeToken<?> type, @NonNull ConfigurationNode value) throws ObjectMappingException {
        final HoverEvent.Action action = value.getNode("action").getValue(TypeTokens.HOVER_ACTION);
        final Component eventValue = value.getNode("value").getValue(TypeTokens.COMPONENT);

        if (action == null || eventValue == null) throw new ObjectMappingException("Invalid hover event");
        return HoverEvent.of(action, eventValue);
    }

    @Override
    public void serialize(@NonNull TypeToken<?> type, @Nullable HoverEvent obj, @NonNull ConfigurationNode value) throws ObjectMappingException {
        if (obj == null) return;

        value.getNode("action").setValue(TypeTokens.HOVER_ACTION, obj.action());
        value.getNode("value").setValue(TypeTokens.COMPONENT, obj.value());
    }
}
