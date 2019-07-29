package me.lucko.gchat.config.serializers;

import com.google.common.reflect.TypeToken;
import net.kyori.text.event.ClickEvent;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import ninja.leaping.configurate.objectmapping.serialize.TypeSerializer;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import static me.lucko.gchat.config.TypeTokens.CLICK_ACTION;

public class ClickEventSerializer implements TypeSerializer<ClickEvent> {
    @Nullable
    @Override
    public ClickEvent deserialize(@NonNull TypeToken<?> type, @NonNull ConfigurationNode value) throws ObjectMappingException {
        final String actionString = value.getNode("action").getString(value.getNode("type").getString());
        if (actionString == null) {
            throw new ObjectMappingException("Click event action not specified");
        }

        ClickEvent.Action action;
        try {
            action = ClickEvent.Action.valueOf(actionString.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new ObjectMappingException("Invalid click event action", e);
        }

        final String eventValue = value.getNode("value").getString();

        if (eventValue == null) throw new ObjectMappingException("No click event value specified");
        return ClickEvent.of(action, eventValue);
    }

    @Override
    public void serialize(@NonNull TypeToken<?> type, @Nullable ClickEvent event, @NonNull ConfigurationNode value) throws ObjectMappingException {
        if (event == null) return;

        value.getNode("action").setValue(CLICK_ACTION, event.action());
        value.getNode("value").setValue(event.value());
    }
}
