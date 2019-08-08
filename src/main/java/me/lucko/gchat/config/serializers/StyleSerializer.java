package me.lucko.gchat.config.serializers;

import com.google.common.reflect.TypeToken;
import net.kyori.text.event.ClickEvent;
import net.kyori.text.event.HoverEvent;
import net.kyori.text.format.Style;
import net.kyori.text.format.TextColor;
import net.kyori.text.format.TextDecoration;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import ninja.leaping.configurate.objectmapping.serialize.TypeSerializer;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

public class StyleSerializer implements TypeSerializer<Style> {
    @NonNull
    @Override
    public Style deserialize(@NonNull TypeToken<?> type, @NonNull ConfigurationNode value) throws ObjectMappingException {
        return Style.builder()
            .color(value.getNode("color").getValue(TypeToken.of(TextColor.class)))
            .decoration(TextDecoration.OBFUSCATED, value.getNode("obfuscated").getBoolean())
            .decoration(TextDecoration.BOLD, value.getNode("bold").getBoolean())
            .decoration(TextDecoration.STRIKETHROUGH, value.getNode("strikethrough").getBoolean())
            .decoration(TextDecoration.UNDERLINED, value.getNode("underlined").getBoolean())
            .decoration(TextDecoration.ITALIC, value.getNode("italic").getBoolean())
            .clickEvent(value.getNode("clickEvent").getValue(TypeToken.of(ClickEvent.class)))
            .hoverEvent(value.getNode("hoverEvent").getValue(TypeToken.of(HoverEvent.class)))
            .insertion(value.getNode("insertion").getString())
            .build();
    }

    @Override
    public void serialize(@NonNull TypeToken<?> type, @Nullable Style style, @NonNull ConfigurationNode value) throws ObjectMappingException {
        if (style == null) return;

        value.getNode("color").setValue(style.color());
        value.getNode("obfuscated").setValue(style.decoration(TextDecoration.OBFUSCATED).toString());
        value.getNode("bold").setValue(style.decoration(TextDecoration.BOLD).toString());
        value.getNode("strikethrough").setValue(style.decoration(TextDecoration.STRIKETHROUGH).toString());
        value.getNode("underlined").setValue(style.decoration(TextDecoration.UNDERLINED).toString());
        value.getNode("italic").setValue(style.decoration(TextDecoration.ITALIC).toString());
        value.getNode("clickEvent").setValue(style.clickEvent());
        value.getNode("hoverEvent").setValue(style.hoverEvent());
        value.getNode("insertion").setValue(style.insertion());
    }
}
