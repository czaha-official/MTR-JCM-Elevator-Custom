package top.czaha.mtrjcmev;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import org.mtr.mapping.holder.Identifier;
import org.mtr.mapping.holder.OrderedText;
import org.mtr.mapping.holder.Style;
import org.mtr.mapping.mapper.GraphicsHolder;
import org.mtr.mapping.mapper.TextHelper;

import java.util.Map;
import java.util.regex.Pattern;

/**
 * Immutable per-lift configuration prepared when settings are loaded or changed.
 * Renderer and tick callbacks use this without locking the JSON settings store.
 * Only the last formatted floor glyph is cached, avoiding an unbounded font cache.
 */
public final class LiftMediaRuntime {
    private static final Pattern RESOURCE_PATH = Pattern.compile("[a-z0-9_./-]+");
    private static final Pattern NAMESPACE = Pattern.compile("[a-z0-9_.-]+");
    private static final String DEFAULT_UP_ARROW = "yte:block/otis_gen3_arrow_up";
    private static final String DEFAULT_DOWN_ARROW = "yte:block/otis_gen3_arrow_down";

    private final boolean hasVisualSettings;
    private final boolean hasArrowSettings;
    private final boolean configuredUpArrow;
    private final boolean configuredDownArrow;
    private final boolean scriptedOverlay;
    private final boolean floorEnabled;
    private final boolean customFont;
    private final Identifier upArrow;
    private final Identifier downArrow;
    private final Identifier floorFont;
    private final ChimeTiming chimeTiming;
    private final boolean hasChimeSettings;
    private final SoundEvent upChime;
    private final SoundEvent downChime;
    private volatile FloorGlyph cachedFloor;

    public LiftMediaRuntime(String id, Map<String, String> settings) {
        configuredUpArrow = settings.containsKey("arrow_up") || settings.containsKey("yte_up");
        configuredDownArrow = settings.containsKey("arrow_down") || settings.containsKey("yte_down");
        hasArrowSettings = configuredUpArrow || configuredDownArrow;
        hasVisualSettings = hasArrowSettings || settings.containsKey("floor_font")
                || settings.containsKey("font_custom") || "false".equals(settings.get("floor_enabled"));
        // JCM scripted lift models draw their own panels. Replacing them with
        // an MTR-size display is unsafe; allow only explicit arrow overlay opt-in.
        scriptedOverlay = "true".equals(settings.get("scripted_overlay"));
        floorEnabled = !"false".equals(settings.get("floor_enabled"));
        customFont = settings.containsKey("floor_font") || settings.containsKey("font_custom");
        upArrow = parseTexture(settings.getOrDefault("arrow_up",
                settings.getOrDefault("yte_up", DEFAULT_UP_ARROW)));
        downArrow = parseTexture(settings.getOrDefault("arrow_down",
                settings.getOrDefault("yte_down", DEFAULT_DOWN_ARROW)));
        floorFont = customFont ? new Identifier("mtr_jcm_ev", "lift_" + id) : null;
        chimeTiming = ChimeTiming.fromSetting(settings.get("chime_trigger"));
        hasChimeSettings = settings.containsKey("chime_up") || settings.containsKey("chime_down");
        upChime = parseSound(settings.get("chime_up"));
        downChime = parseSound(settings.get("chime_down"));
    }

    public boolean hasVisualSettings() {
        return hasVisualSettings;
    }

    public boolean hasArrowSettings() {
        return hasArrowSettings;
    }

    public boolean hasArrowFor(org.mtr.core.data.LiftDirection direction) {
        return direction == org.mtr.core.data.LiftDirection.UP ? configuredUpArrow
                : direction == org.mtr.core.data.LiftDirection.DOWN && configuredDownArrow;
    }

    public boolean scriptedOverlay() {
        return scriptedOverlay;
    }

    public boolean floorEnabled() {
        return floorEnabled;
    }

    public boolean customFont() {
        return customFont;
    }

    public Identifier upArrow() {
        return upArrow;
    }

    public Identifier downArrow() {
        return downArrow;
    }

    public enum ChimeTiming {
        ARRIVAL, DECELERATION, DOOR_OPEN;

        public static ChimeTiming fromSetting(String value) {
            if ("deceleration".equals(value)) return DECELERATION;
            if ("door_open".equals(value)) return DOOR_OPEN;
            return ARRIVAL;
        }
    }

    public ChimeTiming chimeTiming() {
        return chimeTiming;
    }

    public boolean hasChimeSettings() {
        return hasChimeSettings;
    }

    public SoundEvent upChime() {
        return upChime;
    }

    public SoundEvent downChime() {
        return downChime;
    }

    public FloorGlyph glyph(String number) {
        FloorGlyph found = cachedFloor;
        if (found != null && found.value().equals(number)) return found;
        if (floorFont == null) return null;

        OrderedText formatted = TextHelper.mutableTextToOrderedText(
                TextHelper.setStyle(TextHelper.literal(number),
                        Style.getEmptyMapped().withFont(floorFont)));
        FloorGlyph next = new FloorGlyph(number, formatted,
                Math.max(1, GraphicsHolder.getTextWidth(formatted)));
        cachedFloor = next;
        return next;
    }

    private static Identifier parseTexture(String selected) {
        if (selected == null) return null;
        int split = selected.indexOf(':');
        if (split < 1 || split == selected.length() - 1) return null;
        String ns = selected.substring(0, split);
        String path = selected.substring(split + 1);
        if (!NAMESPACE.matcher(ns).matches() || !RESOURCE_PATH.matcher(path).matches()) return null;
        if (path.startsWith("textures/")) {
            return new Identifier(ns, path.endsWith(".png") ? path : path + ".png");
        }
        if (path.startsWith("block/") || path.startsWith("lift/")) {
            return new Identifier(ns, "textures/" + path + ".png");
        }
        return null;
    }

    private static SoundEvent parseSound(String id) {
        if (id == null) return null;
        ResourceLocation location = ResourceLocation.tryParse(id);
        return location == null ? null : SoundEvent.createFixedRangeEvent(location, 32f);
    }

    public record FloorGlyph(String value, OrderedText text, int width) { }
}
