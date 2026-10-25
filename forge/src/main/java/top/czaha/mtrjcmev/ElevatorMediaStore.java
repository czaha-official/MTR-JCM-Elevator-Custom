package top.czaha.mtrjcmev;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.server.packs.repository.PackRepository;

import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.LinkedHashMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public final class ElevatorMediaStore {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Type DATA_TYPE = new TypeToken<Map<String, Map<String, String>>>() { }.getType();
    private static final Map<String, Map<String, String>> DATA = new LinkedHashMap<>();
    private static boolean loaded;

    private ElevatorMediaStore() { }

    public static void openSettings(String liftId) {
        Minecraft minecraft = Minecraft.getInstance();
        minecraft.setScreen(new ElevatorMediaSettingsScreen(minecraft.screen, liftId, 0));
    }

    public static void choose(String liftId, String key) {
        Minecraft minecraft = Minecraft.getInstance();
        minecraft.setScreen(new MediaFileSelectionScreen(minecraft.screen, liftId, key));
    }

    public static synchronized String get(String liftId, String key) {
        load();
        Map<String, String> values = DATA.get(liftId);
        return values == null ? null : values.get(key);
    }

    public static synchronized void set(String liftId, String key, String value) {
        load();
        Map<String, String> values = DATA.computeIfAbsent(liftId, ignored -> new LinkedHashMap<>());
        if (value == null || value.isBlank()) {
            values.remove(key);
            if (values.isEmpty()) DATA.remove(liftId);
        } else {
            values.put(key, value);
        }
        DATA.computeIfAbsent(liftId, ignored -> new LinkedHashMap<>())
                .put("media_revision", Long.toString(System.currentTimeMillis()));
        try {
            writeConfig(Minecraft.getInstance().gameDirectory.toPath());
        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }

    static String save(String liftId, String key, Path selected) {
        try {
            String extension = extensionFor(key, selected);
            if (!Files.isRegularFile(selected)) return "File not found: " + selected;
            if (!selected.getFileName().toString().toLowerCase(Locale.ROOT).endsWith("." + extension)) {
                return "Choose a ." + extension + " file.";
            }

            load();
            Path gameDir = Minecraft.getInstance().gameDirectory.toPath();
            Path assetRoot = gameDir.resolve("resourcepacks/MTR_JCM_Elevator_Custom/assets/mtr_jcm_ev");
            Path output;
            String resourceId;
            switch (key) {
                case "arrow_up" -> {
                    output = gameDir.resolve("resourcepacks/MTR_JCM_Elevator_Custom/assets/mtr_jcm_ev/textures/lift").resolve(liftId).resolve("up.png");
                    resourceId = "mtr_jcm_ev:lift/" + liftId + "/up";
                }
                case "arrow_down" -> {
                    output = gameDir.resolve("resourcepacks/MTR_JCM_Elevator_Custom/assets/mtr_jcm_ev/textures/lift").resolve(liftId).resolve("down.png");
                    resourceId = "mtr_jcm_ev:lift/" + liftId + "/down";
                }
                case "font_custom" -> {
                    output = assetRoot.resolve("font").resolve(liftId).resolve("digits." + extension);
                    resourceId = "mtr_jcm_ev:font/" + liftId + "/digits." + extension;
                }
                case "chime_up" -> {
                    output = gameDir.resolve("resourcepacks/MTR_JCM_Elevator_Custom/assets/mtr_jcm_ev/sounds/lift").resolve(liftId).resolve("chime_up.ogg");
                    resourceId = "mtr_jcm_ev:lift/" + liftId + "/chime_up";
                }
                case "chime_down" -> {
                    output = gameDir.resolve("resourcepacks/MTR_JCM_Elevator_Custom/assets/mtr_jcm_ev/sounds/lift").resolve(liftId).resolve("chime_down.ogg");
                    resourceId = "mtr_jcm_ev:lift/" + liftId + "/chime_down";
                }
                default -> { return "Unknown media type."; }
            }

            Files.createDirectories(output.getParent());
            Files.copy(selected, output, StandardCopyOption.REPLACE_EXISTING);
            Map<String, String> values = DATA.computeIfAbsent(liftId, ignored -> new LinkedHashMap<>());
            values.put(key, resourceId);
            values.put("media_revision", Long.toString(System.currentTimeMillis()));
            writeConfig(gameDir);
            return null;
        } catch (IOException exception) {
            exception.printStackTrace();
            return "Could not save file: " + exception.getMessage();
        } catch (RuntimeException exception) {
            exception.printStackTrace();
            return "Invalid file path: " + exception.getMessage();
        }
    }

    private static String extensionFor(String key, Path selected) {
        return switch (key) {
            case "arrow_up", "arrow_down" -> "png";
            case "chime_up", "chime_down" -> "ogg";
            case "font_custom" -> {
                String name = selected.getFileName().toString().toLowerCase(Locale.ROOT);
                if (name.endsWith(".otf")) yield "otf";
                if (name.endsWith(".ttf")) yield "ttf";
                yield "ttf";
            }
            default -> throw new IllegalArgumentException("Unknown media type: " + key);
        };
    }

    private static synchronized void load() {
        if (loaded) return;
        loaded = true;
        try {
            Path file = Minecraft.getInstance().gameDirectory.toPath().resolve("config/mtr_jcm_elevator_media.json");
            if (Files.exists(file)) {
                Map<String, Map<String, String>> saved = GSON.fromJson(Files.readString(file), DATA_TYPE);
                if (saved != null) {
                    for (Map.Entry<String, Map<String, String>> entry : saved.entrySet()) {
                        Map<String, String> values = new LinkedHashMap<>(entry.getValue());
                        migrateLegacyValue(values, "up", "arrow_up");
                        migrateLegacyValue(values, "down", "arrow_down");
                        migrateLegacyValue(values, "chime", "chime_up");
                        DATA.put(entry.getKey(), values);
                    }
                }
            }
        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }

    private static void migrateLegacyValue(Map<String, String> values, String oldKey, String newKey) {
        if (!values.containsKey(newKey) && values.containsKey(oldKey)) values.put(newKey, values.get(oldKey));
        values.remove(oldKey);
    }

    private static void writeConfig(Path gameDir) throws IOException {
        Path config = gameDir.resolve("config/mtr_jcm_elevator_media.json");
        Files.createDirectories(config.getParent());
        Files.writeString(config, GSON.toJson(DATA));

        Path pack = gameDir.resolve("resourcepacks/MTR_JCM_Elevator_Custom");
        Files.createDirectories(pack);
        Files.writeString(pack.resolve("pack.mcmeta"), "{\"pack\":{\"pack_format\":15,\"description\":\"MTR JCM Elevator Custom media\"}}");
        Path script = pack.resolve("assets/mtr/js/mtr_jcm_ev/media_config.js");
        Files.createDirectories(script.getParent());
        Files.writeString(script, "var MEDIA_CONFIG = " + GSON.toJson(DATA) + ";\n");

        Map<String, Object> sounds = new LinkedHashMap<>();
        for (Map.Entry<String, Map<String, String>> entry : DATA.entrySet()) {
            addSound(sounds, entry.getKey(), "chime_up", "up");
            addSound(sounds, entry.getKey(), "chime_down", "down");
        }
        Path soundsFile = pack.resolve("assets/mtr_jcm_ev/sounds.json");
        Files.createDirectories(soundsFile.getParent());
        Files.writeString(soundsFile, GSON.toJson(sounds));
    }

    public static void reloadMediaPack() {
        Minecraft minecraft = Minecraft.getInstance();
        PackRepository repository = minecraft.getResourcePackRepository();
        repository.reload();
        String packId = repository.getAvailableIds().stream()
                .filter(id -> id.equalsIgnoreCase("file/MTR_JCM_Elevator_Custom"))
                .findFirst().orElse(null);
        if (packId == null) return;
        List<String> selected = new ArrayList<>(repository.getSelectedIds());
        if (selected.stream().noneMatch(id -> id.equalsIgnoreCase(packId))) {
            selected.add(packId);
            repository.setSelected(selected);
            minecraft.options.save();
        }
        minecraft.reloadResourcePacks();
    }

    private static void addSound(Map<String, Object> sounds, String liftId, String key, String direction) {
        Map<String, String> values = DATA.get(liftId);
        String selected = values == null ? null : values.get(key);
        if (selected != null && selected.startsWith("mtr_jcm_ev:lift/")) {
            Map<String, Object> sound = new LinkedHashMap<>();
            sound.put("sounds", java.util.List.of("mtr_jcm_ev:lift/" + liftId + "/chime_" + direction));
            sounds.put("lift_" + liftId + "_" + direction + "_chime", sound);
        }
    }
}
