package top.czaha.mtrjcmev;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.server.packs.repository.PackRepository;

import javax.imageio.ImageIO;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

public final class ElevatorMediaStore {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Type DATA_TYPE = new TypeToken<Map<String, Map<String, String>>>() { }.getType();
    private static final Pattern SAFE_LIFT_ID = Pattern.compile("[a-zA-Z0-9_-]{1,64}");
    private static final String PACK_NAME = "MTR_JCM_Elevator_Custom";
    private static final long MAX_MEDIA_BYTES = 32L * 1024 * 1024;
    private static final int MAX_STORED_ARROW_PIXELS = 512;
    private static final byte[] PNG_HEADER = {(byte) 137, 80, 78, 71, 13, 10, 26, 10};
    private static final Map<String, Map<String, String>> DATA = new LinkedHashMap<>();
    private static volatile boolean loaded;
    private static volatile Map<Long, LiftMediaRuntime> runtimeSnapshot = Map.of();
    private static String loadError;
    private static String lastError;

    private ElevatorMediaStore() { }

    public static void openSettings(String liftId) {
        Minecraft minecraft = Minecraft.getInstance();
        minecraft.setScreen(new ElevatorMediaSettingsScreen(minecraft.screen, liftId, 0));
    }

    public static void choose(String liftId, String key) {
        Minecraft minecraft = Minecraft.getInstance();
        minecraft.setScreen(new MediaFileSelectionScreen(minecraft.screen, liftId, key));
    }

    /**
     * Initialize settings on the client tick, never as part of a rendering callback.
     */
    public static void preload() {
        if (!loaded) {
            synchronized (ElevatorMediaStore.class) {
                load();
            }
        }
    }

    /**
     * Lock-free lookup on the hot render/tick path.
     * The read-only snapshot is replaced when the user changes settings.
     */
    public static LiftMediaRuntime runtimeFor(long liftId) {
        return runtimeSnapshot.get(liftId);
    }

    private static void refreshRuntime(String liftId) {
        final long numericId;
        try {
            numericId = Long.parseLong(liftId);
        } catch (NumberFormatException exception) {
            return;
        }
        Map<Long, LiftMediaRuntime> next = new LinkedHashMap<>(runtimeSnapshot);
        Map<String, String> settings = DATA.get(liftId);
        if (settings == null || (settings.size() == 1 && settings.containsKey("media_revision"))) {
            next.remove(numericId);
        } else {
            next.put(numericId, new LiftMediaRuntime(liftId, settings));
        }
        runtimeSnapshot = Map.copyOf(next);
    }

    public static synchronized String get(String liftId, String key) {
        load();
        Map<String, String> values = DATA.get(liftId);
        return values == null ? null : values.get(key);
    }

    public static synchronized String getLastError() {
        load();
        return loadError != null ? loadError : lastError;
    }

    public static synchronized void set(String liftId, String key, String value) {
        load();
        if (loadError != null) return;
        if (!validLiftId(liftId)) {
            lastError = "Invalid lift ID.";
            return;
        }
        Map<String, String> before = new LinkedHashMap<>(DATA.getOrDefault(liftId, Map.of()));
        Map<String, String> after = new LinkedHashMap<>(before);
        if (value == null || value.isBlank()) {
            after.remove(key);
        } else {
            after.put(key, value);
        }
        after.remove("media_revision");
        Map<String, String> comparison = new LinkedHashMap<>(before);
        comparison.remove("media_revision");
        if (after.equals(comparison)) return;
        if (!after.isEmpty()) {
            after.put("media_revision", nextRevision(before));
            DATA.put(liftId, after);
        } else {
            DATA.remove(liftId);
        }
        try {
            writeConfig(gameDir());
            refreshRuntime(liftId);
            lastError = null;
        } catch (IOException | RuntimeException exception) {
            restore(liftId, before);
            lastError = "Could not save settings: " + exception.getMessage();
        }
    }

    static synchronized String save(String liftId, String key, Path selected) {
        load();
        if (loadError != null) return loadError;
        if (!validLiftId(liftId)) return "Invalid lift ID.";
        if (selected == null || selected.getFileName() == null) return "Choose a file.";
        try {
            String extension = extensionFor(key, selected);
            String name = selected.getFileName().toString().toLowerCase(Locale.ROOT);
            if (!name.endsWith("." + extension)) return "Choose a ." + extension + " file.";
            if (!Files.isRegularFile(selected) || !Files.isReadable(selected)) return "File cannot be read.";
            long size = Files.size(selected);
            if (size <= 0 || size > MAX_MEDIA_BYTES) return "File must be 1 byte to 32 MiB.";
            if (!validMediaHeader(selected, extension)) return "Unsupported or invalid ." + extension + " media file.";

            Path gameDir = gameDir();
            Path assetRoot = gameDir.resolve("resourcepacks").resolve(PACK_NAME).resolve("assets/mtr_jcm_ev");
            Path output;
            String resourceId;
            switch (key) {
                case "arrow_up", "arrow_down" -> {
                    String direction = key.equals("arrow_up") ? "up" : "down";
                    output = assetRoot.resolve("textures/lift").resolve(liftId).resolve(direction + ".png");
                    resourceId = "mtr_jcm_ev:lift/" + liftId + "/" + direction;
                }
                case "font_custom" -> {
                    output = assetRoot.resolve("font").resolve(liftId).resolve("digits." + extension);
                    resourceId = "mtr_jcm_ev:font/" + liftId + "/digits." + extension;
                }
                case "chime_up", "chime_down" -> {
                    String direction = key.equals("chime_up") ? "up" : "down";
                    output = assetRoot.resolve("sounds/lift").resolve(liftId).resolve("chime_" + direction + ".ogg");
                    resourceId = soundEventId(liftId, direction);
                }
                default -> { return "Unknown media type."; }
            }
            Files.createDirectories(output.getParent());
            // Preserve the previously imported file until the config is committed.
            // Failed imports must not replace a working resource with bad media.
            Path backup = null;
            if (Files.isRegularFile(output)) {
                backup = Files.createTempFile(output.getParent(), ".mtr_jcm_ev-backup-", ".tmp");
                Files.copy(output, backup, StandardCopyOption.REPLACE_EXISTING);
            }
            boolean committed = false;
            try {
                if (key.equals("arrow_up") || key.equals("arrow_down")) {
                    importArrow(selected, output);
                } else {
                    atomicCopy(selected, output);
                }
                Map<String, String> before = new LinkedHashMap<>(DATA.getOrDefault(liftId, Map.of()));
                Map<String, String> after = new LinkedHashMap<>(before);
                after.put(key, resourceId);
                after.put("media_revision", nextRevision(before));
                DATA.put(liftId, after);
                try {
                    writeConfig(gameDir);
                    refreshRuntime(liftId);
                    committed = true;
                    lastError = null;
                    return null;
                } catch (IOException | RuntimeException exception) {
                    restore(liftId, before);
                    lastError = "Could not write media configuration: " + exception.getMessage();
                    return lastError;
                }
            } finally {
                if (!committed) {
                    // Best-effort rollback, including the first-ever import.
                    try {
                        if (backup == null) Files.deleteIfExists(output);
                        else Files.copy(backup, output, StandardCopyOption.REPLACE_EXISTING);
                    } catch (IOException rollbackError) {
                        lastError = "Import failed; media rollback also failed: " + rollbackError.getMessage();
                    }
                }
                if (backup != null) Files.deleteIfExists(backup);
            }
        } catch (IOException | RuntimeException exception) {
            lastError = "Could not import file: " + exception.getMessage();
            return lastError;
        }
    }

    private static String extensionFor(String key, Path selected) {
        return switch (key) {
            case "arrow_up", "arrow_down" -> "png";
            case "chime_up", "chime_down" -> "ogg";
            case "font_custom" -> {
                String name = selected.getFileName().toString().toLowerCase(Locale.ROOT);
                yield name.endsWith(".otf") ? "otf" : "ttf";
            }
            default -> throw new IllegalArgumentException("Unknown media type: " + key);
        };
    }

    private static boolean validMediaHeader(Path file, String extension) throws IOException {
        try (InputStream stream = Files.newInputStream(file)) {
            byte[] header = stream.readNBytes(320);
            if ("png".equals(extension)) {
                if (header.length < 24 || !Arrays.equals(Arrays.copyOf(header, 8), PNG_HEADER)) return false;
                if (header[12] != 'I' || header[13] != 'H' || header[14] != 'D' || header[15] != 'R') return false;
                int imageWidth = readIntBigEndian(header, 16);
                int imageHeight = readIntBigEndian(header, 20);
                return imageWidth > 0 && imageWidth <= 4096 && imageHeight > 0 && imageHeight <= 4096;
            }
            if (header.length < 4) return false;
            if ("ogg".equals(extension)) {
                if (header.length < 34 || header[0] != 'O' || header[1] != 'g'
                        || header[2] != 'g' || header[3] != 'S') return false;
                int packetStart = 27 + (header[26] & 0xFF);
                return packetStart + 7 <= header.length
                        && header[packetStart] == 1
                        && header[packetStart + 1] == 'v' && header[packetStart + 2] == 'o'
                        && header[packetStart + 3] == 'r' && header[packetStart + 4] == 'b'
                        && header[packetStart + 5] == 'i' && header[packetStart + 6] == 's';
            }
            if ("otf".equals(extension)) return header[0] == 'O' && header[1] == 'T'
                    && header[2] == 'T' && header[3] == 'O';
            return (header[0] == 0 && header[1] == 1 && header[2] == 0 && header[3] == 0)
                    || (header[0] == 't' && header[1] == 'r' && header[2] == 'u' && header[3] == 'e');
        }
    }

    private static int readIntBigEndian(byte[] bytes, int offset) {
        return (bytes[offset] & 0xFF) << 24 | (bytes[offset + 1] & 0xFF) << 16
                | (bytes[offset + 2] & 0xFF) << 8 | bytes[offset + 3] & 0xFF;
    }

    private static boolean validLiftId(String liftId) {
        return liftId != null && SAFE_LIFT_ID.matcher(liftId).matches()
                && !liftId.equals(".") && !liftId.equals("..");
    }

    private static String soundEventId(String liftId, String direction) {
        return "mtr_jcm_ev:lift_" + liftId + "_" + direction + "_chime";
    }

    private static String nextRevision(Map<String, String> previous) {
        long old = 0;
        try {
            old = Long.parseLong(previous.getOrDefault("media_revision", "0"));
        } catch (NumberFormatException ignored) {
            // A corrupt revision cannot block otherwise valid settings.
        }
        return Long.toString(Math.max(System.currentTimeMillis(), old == Long.MAX_VALUE ? 0 : old + 1));
    }

    private static void restore(String liftId, Map<String, String> before) {
        if (before.isEmpty()) DATA.remove(liftId);
        else DATA.put(liftId, before);
    }

    private static Path gameDir() {
        return Minecraft.getInstance().gameDirectory.toPath();
    }

    private static synchronized void load() {
        if (loaded) return;
        loaded = true;
        Path file = gameDir().resolve("config/mtr_jcm_elevator_media.json");
        if (!Files.exists(file)) return;
        try {
            Map<String, Map<String, String>> saved = GSON.fromJson(Files.readString(file), DATA_TYPE);
            if (saved == null) return;
            Map<Long, LiftMediaRuntime> initialRuntime = new LinkedHashMap<>();
            for (Map.Entry<String, Map<String, String>> entry : saved.entrySet()) {
                String liftId = entry.getKey();
                if (!validLiftId(liftId) || entry.getValue() == null) continue;
                Map<String, String> values = new LinkedHashMap<>(entry.getValue());
                // Corrupt or hand-edited JSON can contain null map entries.
                values.entrySet().removeIf(value -> value.getKey() == null
                        || value.getValue() == null || value.getValue().isBlank());
                migrateLegacyValue(values, "up", "arrow_up");
                migrateLegacyValue(values, "down", "arrow_down");
                migrateLegacyValue(values, "chime", "chime_up");
                migrateLegacyChime(values, liftId, "chime_up", "up");
                migrateLegacyChime(values, liftId, "chime_down", "down");
                DATA.put(liftId, values);
                if (!values.isEmpty() && !(values.size() == 1 && values.containsKey("media_revision"))) {
                    try {
                        initialRuntime.put(Long.parseLong(liftId), new LiftMediaRuntime(liftId, values));
                    } catch (NumberFormatException ignored) {
                        // An old nonnumeric lift ID can remain in the config without loading at runtime.
                    }
                }
            }
            runtimeSnapshot = Map.copyOf(initialRuntime);
        } catch (IOException | RuntimeException exception) {
            DATA.clear();
            runtimeSnapshot = Map.of();
            loadError = "Settings JSON could not be read. Back up and repair config/mtr_jcm_elevator_media.json.";
        }
    }

    private static void migrateLegacyValue(Map<String, String> values, String oldKey, String newKey) {
        if (!values.containsKey(newKey) && values.containsKey(oldKey)) values.put(newKey, values.get(oldKey));
        values.remove(oldKey);
    }

    private static void migrateLegacyChime(Map<String, String> values, String liftId, String key, String direction) {
        if (("mtr_jcm_ev:lift/" + liftId + "/chime_" + direction).equals(values.get(key))) {
            values.put(key, soundEventId(liftId, direction));
        }
    }

    private static void writeConfig(Path gameDir) throws IOException {
        Path config = gameDir.resolve("config/mtr_jcm_elevator_media.json");
        Path pack = gameDir.resolve("resourcepacks").resolve(PACK_NAME);
        atomicWrite(pack.resolve("pack.mcmeta"),
                "{\"pack\":{\"pack_format\":15,\"description\":\"MTR JCM Elevator Custom media\"}}");
        Map<String, Object> sounds = new LinkedHashMap<>();
        Set<String> expectedFonts = new HashSet<>();
        for (Map.Entry<String, Map<String, String>> entry : DATA.entrySet()) {
            String liftId = entry.getKey();
            if (!validLiftId(liftId)) continue;
            writeFontDefinition(pack, liftId, entry.getValue());
            if (entry.getValue().containsKey("floor_font")
                    || entry.getValue().containsKey("font_custom")) {
                expectedFonts.add("lift_" + liftId + ".json");
            }
            addSound(sounds, liftId, entry.getValue(), "chime_up", "up");
            addSound(sounds, liftId, entry.getValue(), "chime_down", "down");
        }
        // Remove only our own generated per-lift font descriptors. Deleted
        // settings otherwise left old font JSONs active forever in the pack.
        Path fontFolder = pack.resolve("assets/mtr_jcm_ev/font");
        if (Files.isDirectory(fontFolder)) {
            try (Stream<Path> files = Files.list(fontFolder)) {
                for (Path path : files.toList()) {
                    String name = path.getFileName().toString();
                    if (Files.isRegularFile(path) && name.matches("lift_[a-zA-Z0-9_-]{1,64}\\.json")
                            && !expectedFonts.contains(name)) {
                        Files.deleteIfExists(path);
                    }
                }
            }
        }
        atomicWrite(pack.resolve("assets/mtr_jcm_ev/sounds.json"), GSON.toJson(sounds));
        // Save the authoritative configuration last so failed pack writes do not commit settings.
        atomicWrite(config, GSON.toJson(DATA));
    }

    private static void writeFontDefinition(Path pack, String liftId, Map<String, String> values)
            throws IOException {
        Path destination = pack.resolve("assets/mtr_jcm_ev/font/lift_" + liftId + ".json");
        if (!values.containsKey("font_custom") && !values.containsKey("floor_font")) {
            Files.deleteIfExists(destination);
            return;
        }
        String file = values.get("font_custom");
        if (file == null || file.isBlank()) {
            file = values.getOrDefault("floor_font", "yte:font/otis-gen3-display.ttf");
        }
        if (file == null || !file.matches("[a-z0-9_.-]+:[a-z0-9_./-]+\\.(ttf|otf)")) return;
        Map<String, Object> selectedProvider = new LinkedHashMap<>();
        selectedProvider.put("type", "ttf");
        selectedProvider.put("file", file);
        selectedProvider.put("shift", List.of(0.0, 0.0));
        selectedProvider.put("size", 11.0);
        selectedProvider.put("oversample", 2.0);
        Map<String, Object> fallback = new LinkedHashMap<>();
        fallback.put("type", "reference");
        fallback.put("id", "minecraft:default");
        Map<String, Object> definition = new LinkedHashMap<>();
        definition.put("providers", List.of(selectedProvider, fallback));
        atomicWrite(destination, GSON.toJson(definition));
    }

    private static void atomicWrite(Path destination, String contents) throws IOException {
        // No disk writes for unchanged resources: avoids unnecessary file-watcher churn.
        if (Files.isRegularFile(destination) && contents.equals(Files.readString(destination, StandardCharsets.UTF_8))) {
            return;
        }
        Files.createDirectories(destination.getParent());
        Path temporary = Files.createTempFile(destination.getParent(), ".mtr_jcm_ev-", ".tmp");
        try {
            Files.writeString(temporary, contents, StandardCharsets.UTF_8);
            moveReplacing(temporary, destination);
        } finally {
            Files.deleteIfExists(temporary);
        }
    }

    /**
     * Oversized arrow textures needlessly consume VRAM for a tiny lift display.
     * Resize only at import time; never decode images on the render thread.
     */
    private static void importArrow(Path source, Path destination) throws IOException {
        BufferedImage input = ImageIO.read(source.toFile());
        if (input == null) throw new IOException("Invalid or unsupported PNG image.");
        int longest = Math.max(input.getWidth(), input.getHeight());
        if (longest <= MAX_STORED_ARROW_PIXELS) {
            atomicCopy(source, destination);
            return;
        }

        double ratio = MAX_STORED_ARROW_PIXELS / (double) longest;
        int targetWidth = Math.max(1, (int) Math.round(input.getWidth() * ratio));
        int targetHeight = Math.max(1, (int) Math.round(input.getHeight() * ratio));
        BufferedImage scaled = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = scaled.createGraphics();
        try {
            graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                    RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
            graphics.drawImage(input, 0, 0, targetWidth, targetHeight, null);
        } finally {
            graphics.dispose();
        }

        Path temporary = Files.createTempFile(destination.getParent(), ".mtr_jcm_ev-arrow-", ".png");
        try {
            if (!ImageIO.write(scaled, "png", temporary.toFile())) {
                throw new IOException("No PNG encoder available.");
            }
            moveReplacing(temporary, destination);
        } finally {
            Files.deleteIfExists(temporary);
        }
    }

    private static void atomicCopy(Path source, Path destination) throws IOException {
        Path temporary = Files.createTempFile(destination.getParent(), ".mtr_jcm_ev-", ".tmp");
        try {
            Files.copy(source, temporary, StandardCopyOption.REPLACE_EXISTING);
            moveReplacing(temporary, destination);
        } finally {
            Files.deleteIfExists(temporary);
        }
    }

    private static void moveReplacing(Path source, Path destination) throws IOException {
        try {
            Files.move(source, destination, StandardCopyOption.REPLACE_EXISTING,
                    StandardCopyOption.ATOMIC_MOVE);
        } catch (AtomicMoveNotSupportedException ignored) {
            Files.move(source, destination, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    /**
     * Preview the exact SoundEvent selected in the UI. This also identifies
     * missing YTE sounds and generated resource packs that are not yet loaded.
     */
    public static synchronized String previewChime(String liftId, boolean up) {
        load();
        String key = up ? "chime_up" : "chime_down";
        String configured = DATA.getOrDefault(liftId, Map.of()).get(key);
        // An unset direction uses JCM's normal note_block.pling (pitch 2),
        // NOT the YTE sample shown in the old UI.
        String id = configured == null ? "minecraft:block.note_block.pling" : configured;
        ResourceLocation soundId = ResourceLocation.tryParse(id);
        if (soundId == null) return "Invalid sound event: " + id;
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.getSoundManager().getSoundEvent(soundId) == null) {
            return "Sound not loaded: " + id + " (check YTE or Apply media pack / reload)";
        }
        minecraft.getSoundManager().play(
                SimpleSoundInstance.forUI(SoundEvent.createVariableRangeEvent(soundId),
                        configured == null ? 2f : 1f));
        return null;
    }

    public static String reloadMediaPack() {
        try {
            Minecraft minecraft = Minecraft.getInstance();
            PackRepository repository = minecraft.getResourcePackRepository();
            repository.reload();
            String packId = repository.getAvailableIds().stream()
                    .filter(id -> id.equalsIgnoreCase("file/" + PACK_NAME))
                    .findFirst().orElse(null);
            if (packId == null) return "Save a media setting first; no generated resource pack was found.";
            List<String> selected = new ArrayList<>(repository.getSelectedIds());
            if (!selected.contains(packId)) {
                selected.add(packId);
                repository.setSelected(selected);
            }
            minecraft.options.updateResourcePacks(repository);
            minecraft.options.save();
            minecraft.reloadResourcePacks();
            synchronized (ElevatorMediaStore.class) {
                for (String id : DATA.keySet()) refreshRuntime(id);
            }
            return null;
        } catch (RuntimeException exception) {
            return "Could not reload media pack: " + exception.getMessage();
        }
    }

    private static void addSound(Map<String, Object> sounds, String liftId,
                                 Map<String, String> values, String key, String direction) {
        String selected = values.get(key);
        if (soundEventId(liftId, direction).equals(selected)) {
            Map<String, Object> sound = new LinkedHashMap<>();
            sound.put("sounds", List.of("mtr_jcm_ev:lift/" + liftId + "/chime_" + direction));
            sounds.put("lift_" + liftId + "_" + direction + "_chime", sound);
        }
    }
}
