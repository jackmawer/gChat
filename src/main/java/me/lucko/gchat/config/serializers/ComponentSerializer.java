package me.lucko.gchat.config.serializers;

import com.google.common.reflect.TypeToken;
import me.lucko.gchat.config.TypeTokens;
import net.kyori.text.*;
import net.kyori.text.format.Style;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import ninja.leaping.configurate.objectmapping.serialize.TypeSerializer;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.List;
import java.util.Objects;

public class ComponentSerializer implements TypeSerializer<Component> {
    @Nullable
    @Override
    public Component deserialize(@NonNull TypeToken<?> type, @NonNull ConfigurationNode value) throws ObjectMappingException {
        Style style = value.getValue(TypeToken.of(Style.class));
        List<Component> extras = value.getNode("extra").getList(TypeTokens.COMPONENT);

        Component component = null;
        if (!value.getNode("text").isVirtual()) {
            component = TextComponent.of(value.getNode("text").getString(""));
        } else if (!value.getNode("translate").isVirtual()) {
            component = TranslatableComponent.of(value.getNode("translate").getString(""), value.getNode("with").getList(TypeTokens.COMPONENT));
        } else if (!value.getNode("keybind").isVirtual()) {
            component = KeybindComponent.of(value.getNode("keybind").getString(""));
        } else if (!value.getNode("score").isVirtual()) {
            String scoreName = Objects.requireNonNull(value.getNode("score", "name").getString(), "Score entity name missing");
            String scoreObjective = Objects.requireNonNull(value.getNode("score", "objective").getString(), "Score objective missing");
            String scoreValue = value.getNode("score", "value").getString();
            if (scoreValue != null) {
                component = ScoreComponent.of(scoreName, scoreObjective, scoreValue);
            } else {
                component = ScoreComponent.of(scoreName, scoreObjective);
            }
        } else if (!value.getNode("selector").isVirtual()) {
            component = SelectorComponent.of(value.getNode("selector").getString(""));
        } else if (!value.getNode("nbt").isVirtual() && !value.getNode("block").isVirtual()) {
            component = BlockNbtComponent.of(value.getNode("nbt").getString(""), value.getNode("block").getValue(TypeTokens.POS));
        } else if (!value.getNode("nbt").isVirtual() && !value.getNode("entity").isVirtual()) {
            component = EntityNbtComponent.of(value.getNode("nbt").getString(""), value.getNode("entity").getString(""));
        } else {
            throw new ObjectMappingException("Could not determine component type");
        }

        if (style != null) {
            component = component.style(style);
        }

        for (Component extra : extras) {
            component = component.append(extra);
        }

        return component;
    }

    @Override
    public void serialize(@NonNull TypeToken<?> type, @Nullable Component component, @NonNull ConfigurationNode value) throws ObjectMappingException {
        if (component == null) return;

        value.setValue(TypeTokens.STYLE, component.style());
        value.getNode("extras").setValue(component.children());

        if (component instanceof TextComponent) {
            value.getNode("text").setValue(((TextComponent) component).content());
        } else if (component instanceof TranslatableComponent) {
            value.getNode("translate").setValue(((TranslatableComponent) component).key());
            value.getNode("with").setValue(((TranslatableComponent) component).args());
        } else if (component instanceof KeybindComponent) {
            value.getNode("keybind").setValue(((KeybindComponent) component).keybind());
        } else if (component instanceof ScoreComponent) {
            ScoreComponent scoreComponent = (ScoreComponent) component;
            value.getNode("score", "name").setValue(scoreComponent.name());
            value.getNode("score", "objective").setValue(scoreComponent.objective());
            if (scoreComponent.value() != null) {
                value.getNode("score", "value").setValue(scoreComponent.value());
            }
        } else if (component instanceof SelectorComponent) {
            value.getNode("selector").setValue(((SelectorComponent) component).pattern());
        } else if (component instanceof BlockNbtComponent) {
            value.getNode("nbt").setValue(((BlockNbtComponent) component).nbtPath());
            value.getNode("block").setValue(((BlockNbtComponent) component).pos());
        } else if (component instanceof EntityNbtComponent) {
            value.getNode("nbt").setValue(((EntityNbtComponent) component).nbtPath());
            value.getNode("entity").setValue(((EntityNbtComponent) component).selector());
        }
    }
}
