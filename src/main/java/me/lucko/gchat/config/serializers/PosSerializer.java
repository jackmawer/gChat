package me.lucko.gchat.config.serializers;

import com.google.common.reflect.TypeToken;
import net.kyori.text.BlockNbtComponent;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import ninja.leaping.configurate.objectmapping.serialize.TypeSerializer;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PosSerializer implements TypeSerializer<BlockNbtComponent.Pos> {

    // Deserialization code based on https://github.com/KyoriPowered/text/blob/5b0b01d44d910b62932e61065e97a30251feed5f/serializer-gson/src/main/java/net/kyori/text/serializer/gson/BlockNbtComponentPosSerializer.java#L41-L67
    private static final Pattern LOCAL_POS_PATTERN = Pattern.compile("^\\^(\\d+(\\.\\d+)?) \\^(\\d+(\\.\\d+)?) \\^(\\d+(\\.\\d+)?)$");
    private static final Pattern WORLD_POS_PATTERN = Pattern.compile("^(~?)(\\d+) (~?)(\\d+) (~?)(\\d+)$");

    @Override
    public BlockNbtComponent.Pos deserialize(@NonNull TypeToken<?> type, @NonNull ConfigurationNode value) throws ObjectMappingException {
        String str = value.getString();
        if (str == null) return null;

        Matcher localMatcher = LOCAL_POS_PATTERN.matcher(str);
        if (localMatcher.matches()) {
            return BlockNbtComponent.LocalPos.of(
                Double.parseDouble(localMatcher.group(1)),
                Double.parseDouble(localMatcher.group(3)),
                Double.parseDouble(localMatcher.group(5))
            );
        }

        Matcher worldMatcher = WORLD_POS_PATTERN.matcher(str);
        if (worldMatcher.matches()) {
            return BlockNbtComponent.WorldPos.of(
                deserializeCoordinate(worldMatcher.group(1), worldMatcher.group(2)),
                deserializeCoordinate(worldMatcher.group(3), worldMatcher.group(4)),
                deserializeCoordinate(worldMatcher.group(5), worldMatcher.group(6))
            );
        }

        throw new ObjectMappingException("Could not determine type of block position");
    }

    @Override
    public void serialize(@NonNull TypeToken<?> type, BlockNbtComponent.@Nullable Pos pos, @NonNull ConfigurationNode value) throws ObjectMappingException {
        String coords = "";
        if (pos instanceof BlockNbtComponent.LocalPos) {
            BlockNbtComponent.LocalPos localPos = (BlockNbtComponent.LocalPos) pos;
            coords = "^" + localPos.left() + " ^" + localPos.up() + " ^" + localPos.forwards();
        } else if (pos instanceof BlockNbtComponent.WorldPos) {
            coords = serializeWorldPos((BlockNbtComponent.WorldPos) pos);
        }

        value.setValue(coords);
    }

    private static String serializeWorldPos(BlockNbtComponent.WorldPos pos) {
        return serializeCoordinate(pos.x()) + " " + serializeCoordinate(pos.y()) + " " + serializeCoordinate(pos.z());
    }

    private static String serializeCoordinate(BlockNbtComponent.WorldPos.Coordinate coord) {
        String str = "";
        if (coord.type() == BlockNbtComponent.WorldPos.Coordinate.Type.RELATIVE) {
            str = "~";
        }
        str = str + coord.value();
        return str;
    }

    private static BlockNbtComponent.WorldPos.Coordinate deserializeCoordinate(final String prefix, final String value) {
        final int i = Integer.parseInt(value);
        if (prefix.isEmpty()) {
            return BlockNbtComponent.WorldPos.Coordinate.absolute(i);
        } else if (prefix.equals("~")) {
            return BlockNbtComponent.WorldPos.Coordinate.relative(i);
        } else {
            throw new AssertionError(); // regex does not allow any other value for prefix.
        }
    }
}
