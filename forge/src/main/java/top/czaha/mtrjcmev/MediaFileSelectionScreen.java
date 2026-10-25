package top.czaha.mtrjcmev;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.nio.file.Path;

final class MediaFileSelectionScreen extends Screen {
    private final Screen parent;
    private final String liftId;
    private final String key;
    private EditBox pathField;
    private String errorMessage;

    MediaFileSelectionScreen(Screen parent, String liftId, String key) {
        super(Component.literal("Select elevator " + key + " file"));
        this.parent = parent;
        this.liftId = liftId;
        this.key = key;
    }

    @Override
    protected void init() {
        int centerX = width / 2;
        int fieldWidth = Math.max(1, Math.min(300, width - 24));
        pathField = new EditBox(font, centerX - fieldWidth / 2, height / 2 - 5, fieldWidth, 20,
                Component.literal("File path"));
        pathField.setMaxLength(1024);
        pathField.setResponder(value -> errorMessage = null);
        addRenderableWidget(pathField);
        addRenderableWidget(Button.builder(Component.literal("Load"), button -> loadFile())
                .bounds(centerX - 100, height / 2 + 30, 95, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Cancel"), button -> onClose())
                .bounds(centerX + 5, height / 2 + 30, 95, 20).build());
        setInitialFocus(pathField);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics);
        String prompt = "Paste the full path to a " + extensionDescription() + " file";
        graphics.drawCenteredString(font, Component.literal(font.plainSubstrByWidth(prompt, Math.max(20, width - 16))),
                width / 2, height / 2 - 35, 0xFFFFFF);
        super.render(graphics, mouseX, mouseY, partialTick);
        if (errorMessage != null) {
            graphics.drawCenteredString(font,
                    Component.literal(font.plainSubstrByWidth(errorMessage, Math.max(20, width - 16))),
                    width / 2, height / 2 + 65, 0xFF5555);
        }
    }

    @Override
    public void onClose() {
        minecraft.setScreen(parent);
    }

    private void loadFile() {
        String value = pathField.getValue().trim();
        if (value.length() >= 2 && value.startsWith("\"") && value.endsWith("\"")) {
            value = value.substring(1, value.length() - 1);
        }
        if (value.isBlank()) {
            errorMessage = "Enter a file path first.";
            return;
        }
        try {
            String error = ElevatorMediaStore.save(liftId, key, Path.of(value));
            if (error == null) {
                minecraft.setScreen(parent);
            } else {
                errorMessage = error;
            }
        } catch (RuntimeException exception) {
            errorMessage = "Invalid file path: " + exception.getMessage();
        }
    }

    private String extension() {
        return switch (key) {
            case "arrow_up", "arrow_down" -> "png";
            case "font_custom" -> "ttf";
            default -> "ogg";
        };
    }

    private String extensionDescription() {
        return key.equals("font_custom") ? ".ttf or .otf" : "." + extension();
    }
}
