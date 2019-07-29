package me.lucko.gchat.config.serializers;

import com.google.common.reflect.TypeToken;
import me.lucko.gchat.config.TypeTokens;
import net.kyori.text.Component;
import net.kyori.text.event.ClickEvent;
import net.kyori.text.event.HoverEvent;
import net.kyori.text.serializer.legacy.LegacyComponentSerializer;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import ninja.leaping.configurate.objectmapping.serialize.TypeSerializer;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

public class HoverEventSerializer implements TypeSerializer<HoverEvent> {

    @Nullable
    @Override
    public HoverEvent deserialize(@NonNull TypeToken<?> type, @NonNull ConfigurationNode value) throws ObjectMappingException {
        final String actionString = value.getNode("action").getString(value.getNode("type").getString());
        if (actionString == null) {
            throw new ObjectMappingException("Hover event action not specified");
        }

        HoverEvent.Action action;
        try {
            action = HoverEvent.Action.valueOf(actionString.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new ObjectMappingException("Invalid hover event action", e);
        }

        Component eventValue = value.getNode("value").getValue(TypeTokens.COMPONENT);

        if (eventValue == null && !value.getNode("text").isVirtual()) {
            eventValue = LegacyComponentSerializer.legacy().deserialize(value.getNode("text").getString(""));
        }

        if (eventValue == null) throw new ObjectMappingException("No hover component specified");
        return HoverEvent.of(action, eventValue);
    }

    @Override
    public void serialize(@NonNull TypeToken<?> type, @Nullable HoverEvent obj, @NonNull ConfigurationNode value) throws ObjectMappingException {
        if (obj == null) return;

        value.getNode("action").setValue(TypeTokens.HOVER_ACTION, obj.action());
        value.getNode("value").setValue(TypeTokens.COMPONENT, obj.value());
    }
}
