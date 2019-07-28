package me.lucko.gchat.config;

import com.google.common.reflect.TypeToken;
import net.kyori.text.BlockNbtComponent;
import net.kyori.text.Component;
import net.kyori.text.event.ClickEvent;
import net.kyori.text.event.HoverEvent;
import net.kyori.text.format.Style;

public class TypeTokens {
    public static final TypeToken<Component> COMPONENT = TypeToken.of(Component.class);
    public static final TypeToken<Style> STYLE = TypeToken.of(Style.class);
    public static final TypeToken<ClickEvent> CLICK_EVENT = TypeToken.of(ClickEvent.class);
    public static final TypeToken<ClickEvent.Action> CLICK_ACTION = TypeToken.of(ClickEvent.Action.class);
    public static final TypeToken<HoverEvent> HOVER_EVENT = TypeToken.of(HoverEvent.class);
    public static final TypeToken<HoverEvent.Action> HOVER_ACTION = TypeToken.of(HoverEvent.Action.class);
    public static final TypeToken<BlockNbtComponent.Pos> POS = TypeToken.of(BlockNbtComponent.Pos.class);
}
