package top.czaha.mtrjcmev;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.List;

final class ElevatorMediaSettingsScreen extends Screen {
    private static final List<String> ARROW_TEXTURES = List.of(
            "yte:block/otis_gen3_arrow_up", "yte:block/otis_gen3_arrow_down", "yte:block/otis_gen3_arrow",
            "yte:block/otis_s3_lantern_1_arrow", "yte:block/otis_s3_button_1_arrow", "yte:block/otis_s3_eld_arrow_1",
            "yte:block/otis_s1_arrow", "yte:block/otis_spec_60_arrow_1", "yte:block/otis_spec_90_arrow_1",
            "yte:block/fujitec_mic400_button_1_old_arrow_up", "yte:block/fujitec_mic400_button_1_old_arrow_down",
            "yte:block/fujitec_mic400_round_arrow/fujitec_mic400_round_arrow_0",
            "yte:block/fujitec_mic400_round_arrow/fujitec_mic400_round_arrow_1",
            "yte:block/fujitec_mic400_round_arrow/fujitec_mic400_round_arrow_2",
            "yte:block/fujitec_mic400_round_arrow/fujitec_mic400_round_arrow_3",
            "yte:block/fujitec_mic400_round_arrow/fujitec_mic400_round_arrow_4",
            "yte:block/fujitec_mic400_round_arrow/fujitec_mic400_round_arrow_5",
            "yte:block/fujitec_mic400_round_arrow/fujitec_mic400_round_arrow_6",
            "yte:block/hitachi_b85_arrow_1", "yte:block/hitachi_b85_arrow_2", "yte:block/hitachi_bxsclc5_arrow",
            "yte:block/hitachi_bxsclc5_arrow_white", "yte:block/hitachi_cip71_arrow", "yte:block/hitachi_cip71_arrow_white",
            "yte:block/hitachi_dot_arrow_1", "yte:block/hitachi_ghl820_arrow", "yte:block/hitachi_hip31_arrow",
            "yte:block/hitachi_hip32_arrow", "yte:block/hitachi_hip43_arrow", "yte:block/hitachi_sclva043_arrow",
            "yte:block/kone_arrow_back", "yte:block/kone_arrow_light", "yte:block/kone_kds330_arrow",
            "yte:block/kone_m_arrow_1", "yte:block/kone_m_lantern_arrow_1", "yte:block/mitsubishi_gps3_button_2_arrow",
            "yte:block/mitsubishi_lantern_arrow", "yte:block/mitsubishi_lantern_arrow_lit",
            "yte:block/mitsubishi_lantern_light_1_arrow", "yte:block/mitsubishi_maxiez_1_lcd_arrow_1",
            "yte:block/mitsubishi_maxiez_2_lcd_arrow_1", "yte:block/mitsubishi_nexway_1_arrow",
            "yte:block/mitsubishi_nexway_1_arrow_segmented", "yte:block/mitsubishi_nexway_screen_3_arrow",
            "yte:block/monarch_generic_arrow_1", "yte:block/schindler_d_series_screen_1_arrow",
            "yte:block/schindler_d_series_screen_2_arrow_blue", "yte:block/schindler_d_series_screen_2_arrow_green",
            "yte:block/schindler_d_series_screen_2_arrow_red", "yte:block/schindler_fi_gs_arrow_2",
            "yte:block/schindler_fi_gs_arrow_3", "yte:block/schindler_fi_gs_arrow_4",
            "yte:block/schindler_linea_arrow_1", "yte:block/schindler_linea_arrow_2", "yte:block/schindler_linea_arrow_3",
            "yte:block/schindler_m_series_lantern_2_arrow", "yte:block/schindler_m_series_panel_arrow_1",
            "yte:block/schindler_m_series_panel_arrow_2", "yte:block/schindler_m_series_screen_4_arrow",
            "yte:block/schindler_r_series_screen_1_arrow", "yte:block/shanghai_mitsubishi_lehy_3_arrow_1",
            "yte:block/shanghai_mitsubishi_lehy_3_arrow_2", "yte:block/test_lift_lanterns_arrow",
            "yte:block/thyssenkrupp_arrow_1", "yte:block/thyssenkrupp_arrow_1_square",
            "yte:block/tonic_dm_screen_1_arrow_green", "yte:block/tonic_dm_screen_1_arrow_red",
            "yte:block/tonic_dm_screen_1_arrow_yellow", "yte:block/toshiba_lantern_1_arrow",
            "yte:block/ces_arrow_1", "yte:block/dewhurst_ul200_arrow_1", "yte:block/dewhurst_us81_button_1_arrow",
            "yte:block/dover_impulse_lantern_1_arrow", "yte:block/arrow_down_left"
    );
    private static final List<String> YTE_FONTS = List.of(
            "yte:font/otis-gen3-display.ttf", "yte:font/otis_series_3.ttf", "yte:font/otis-segmented.ttf",
            "yte:font/otis-segmented-type-2.ttf", "yte:font/series1.otf", "yte:font/series1_x11.otf",
            "yte:font/hitachi_modern.ttf", "yte:font/hitachi_b85.ttf", "yte:font/hitachi_b85_2.ttf",
            "yte:font/hitachi-cip71-led.ttf", "yte:font/hitachi-hip43-lcd.ttf", "yte:font/hitachi-hip31-lcd.ttf",
            "yte:font/hitachi-dot-matrix-regular.ttf", "yte:font/hitachi-dot-matrix-small-generic.ttf",
            "yte:font/kone-m-series.ttf", "yte:font/kone-lcd-segment.ttf", "yte:font/kone_kds220_segment.ttf",
            "yte:font/mitsubishi_modern_v3.ttf", "yte:font/mitsubishi_seg_universal.ttf",
            "yte:font/mitsubishi-dot-matrix-small-generic.ttf", "yte:font/thyssenkrupp-round-dot-matrix.ttf",
            "yte:font/thyssenkrupp-square-dot-matrix.ttf", "yte:font/schindler-led.ttf",
            "yte:font/schindler_lcd.ttf", "yte:font/tonic-led.ttf", "yte:font/toshiba-segmented.ttf",
            "yte:font/ryoden-led.ttf", "yte:font/ces-14x7.ttf", "yte:font/acme-led.ttf"
    );
    private static final List<String> YTE_UP_CHIMES = List.of(
            "yte:otis_series_1_lantern_1_up", "yte:otis_series_1_lantern_2_up",
            "yte:otis_series_3_lantern_1_up", "yte:mitsubishi_nexway_lantern_1_up",
            "yte:toshiba_lantern_1_up", "yte:kone_m_lantern_1_up"
    );
    private static final List<String> YTE_DOWN_CHIMES = List.of(
            "yte:otis_series_1_lantern_1_down", "yte:otis_series_1_lantern_2_down",
            "yte:otis_series_3_lantern_1_down", "yte:mitsubishi_nexway_lantern_1_down",
            "yte:toshiba_lantern_1_down", "yte:kone_m_lantern_1_down"
    );
    private static final String DEFAULT_UP_CHIME = "yte:otis_series_3_lantern_1_up";
    private static final String DEFAULT_DOWN_CHIME = "yte:otis_series_3_lantern_1_down";
    private static final String DEFAULT_UP_ARROW = "yte:block/otis_gen3_arrow_up";
    private static final String DEFAULT_DOWN_ARROW = "yte:block/otis_gen3_arrow_down";
    private static final String DEFAULT_FONT = "yte:font/otis-gen3-display.ttf";

    private final Screen parent;
    private final String liftId;
    private final int tab;

    ElevatorMediaSettingsScreen(Screen parent, String liftId, int tab) {
        super(Component.literal("Elevator media settings"));
        this.parent = parent;
        this.liftId = liftId;
        this.tab = tab;
    }

    private String statusMessage;
    private boolean applyFailed;

    @Override
    protected void init() {
        // Minecraft GUI scales can produce a screen too small for even three
        // safe touch targets. Display a readable explanation rather than
        // overlapping actionable buttons.
        if (width < 160 || height < 140) {
            statusMessage = "Screen too small - reduce Minecraft GUI scale.";
            addRenderableWidget(Button.builder(Component.literal("Back"), button -> onClose())
                    .bounds(Math.max(0, width / 2 - 40), Math.max(0, height - 25),
                            Math.min(80, Math.max(1, width)), 20).build());
            return;
        }
        int panel = panelWidth();
        int tabWidth = (panel - 12) / 4;
        int left = (width - panel) / 2;
        addRenderableWidget(tabButton(left, tabWidth, "Arrows", 0));
        addRenderableWidget(tabButton(left + tabWidth + 4, tabWidth, "Numbers", 1));
        addRenderableWidget(tabButton(left + (tabWidth + 4) * 2, tabWidth, "Chimes", 2));
        addRenderableWidget(tabButton(left + (tabWidth + 4) * 3,
                panel - (tabWidth + 4) * 3, "Script", 4));

        if (tab == 0) addArrowTab();
        else if (tab == 1) addNumberTab();
        else if (tab == 2) addChimeTab();
        else if (tab == 3) addChimePreviewTab();
        else if (tab == 4) addScriptTab();
        else if (tab == 5) addArrowDownCompact();
        else if (tab == 6) addChimeDownCompact();
        else addChimePreviewTab();

        addRenderableWidget(Button.builder(Component.literal("Done"), button -> onClose())
                .bounds(Math.max(0, width / 2 - 50), Math.max(0, height - 24),
                        Math.min(100, width), 20).build());
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics);
        String error = ElevatorMediaStore.getLastError();
        String heading = error != null ? error : statusMessage != null ? statusMessage : title.getString();
        graphics.drawCenteredString(font, Component.literal(font.plainSubstrByWidth(heading, Math.max(20, width - 16))),
                width / 2, 12, error != null || applyFailed ? 0xFF5555 : 0xFFFFFF);
        if (height >= 255) {
            String id = "Lift ID: " + liftId;
            graphics.drawCenteredString(font, Component.literal(font.plainSubstrByWidth(id, Math.max(20, width - 16))),
                    width / 2, 57, 0xAAAAAA);
        }
        super.render(graphics, mouseX, mouseY, partialTick);
    }

    @Override
    public void onClose() {
        minecraft.setScreen(parent);
    }

    private int panelWidth() {
        return Math.max(1, Math.min(310, width - 12));
    }

    private int contentTop() {
        return Math.max(54, Math.min(78, height - 147));
    }

    private Button tabButton(int x, int buttonWidth, String label, int targetTab) {
        String shortened = font.plainSubstrByWidth(label, Math.max(8, buttonWidth - 8));
        Button button = Button.builder(Component.literal(shortened), pressed ->
                minecraft.setScreen(new ElevatorMediaSettingsScreen(parent, liftId, targetTab)))
                .bounds(x, 32, buttonWidth, 20).build();
        button.active = targetTab != tab;
        if (!shortened.equals(label)) button.setTooltip(Tooltip.create(Component.literal(label)));
        return button;
    }

    private Button control(String label, int column, int yOffset, boolean fullWidth, Button.OnPress onPress) {
        int panel = panelWidth();
        int left = (width - panel) / 2;
        int half = (panel - 6) / 2;
        int x = fullWidth ? left : left + column * (half + 6);
        int buttonWidth = fullWidth ? panel : column == 0 ? half : panel - half - 6;
        int controlY = height < 200 ? yOffset * 4 / 5 : yOffset;
        int controlHeight = height < 200 ? 16 : 20;
        String rendered = font.plainSubstrByWidth(label, Math.max(8, buttonWidth - 12));
        Button button = Button.builder(Component.literal(rendered), onPress)
                .bounds(x, contentTop() + controlY, buttonWidth, controlHeight).build();
        if (!rendered.equals(label)) button.setTooltip(Tooltip.create(Component.literal(label)));
        return button;
    }

    private void addArrowTab() {
        if (height < 205) {
            addArrowUpCompact();
            return;
        }
        String up = valueOrDefault("yte_up", DEFAULT_UP_ARROW);
        String down = valueOrDefault("yte_down", DEFAULT_DOWN_ARROW);
        String customUp = ElevatorMediaStore.get(liftId, "arrow_up");
        String customDown = ElevatorMediaStore.get(liftId, "arrow_down");
        String upLabel = customUp != null ? "Custom PNG" :
                ElevatorMediaStore.get(liftId, "yte_up") == null ? "MTR default" : shortName(up);
        String downLabel = customDown != null ? "Custom PNG" :
                ElevatorMediaStore.get(liftId, "yte_down") == null ? "MTR default" : shortName(down);

        addRenderableWidget(control("Previous UP", 0, 0, false,
                button -> cycleArrow("yte_up", up, -1)));
        addRenderableWidget(control("UP: " + upLabel, 1, 0, false,
                button -> cycleArrow("yte_up", up, 1)));
        addRenderableWidget(control("Custom UP PNG" + customStatus(customUp), 0, 24, false,
                button -> ElevatorMediaStore.choose(liftId, "arrow_up")));
        addRenderableWidget(control("Use YTE UP arrow", 1, 24, false,
                button -> useYteArrow(true)));

        addRenderableWidget(control("Previous DOWN", 0, 52, false,
                button -> cycleArrow("yte_down", down, -1)));
        addRenderableWidget(control("DOWN: " + downLabel, 1, 52, false,
                button -> cycleArrow("yte_down", down, 1)));
        addRenderableWidget(control("Custom DOWN PNG" + customStatus(customDown), 0, 76, false,
                button -> ElevatorMediaStore.choose(liftId, "arrow_down")));
        addRenderableWidget(control("Use YTE DOWN arrow", 1, 76, false,
                button -> useYteArrow(false)));
        addRenderableWidget(control("Apply media pack / reload", 0, 101, true,
                button -> applyPack()));
    }

    private void addArrowUpCompact() {
        String up = valueOrDefault("yte_up", DEFAULT_UP_ARROW);
        String upLabel = ElevatorMediaStore.get(liftId, "arrow_up") != null ? "Custom PNG"
                : ElevatorMediaStore.get(liftId, "yte_up") == null ? "MTR default" : shortName(up);
        addRenderableWidget(control("Previous UP", 0, 0, false,
                button -> cycleArrow("yte_up", up, -1)));
        addRenderableWidget(control("UP: " + upLabel, 1, 0, false,
                button -> cycleArrow("yte_up", up, 1)));
        addRenderableWidget(control("Custom UP PNG", 0, 24, false,
                button -> ElevatorMediaStore.choose(liftId, "arrow_up")));
        addRenderableWidget(control("Use YTE UP", 1, 24, false,
                button -> useYteArrow(true)));
        addRenderableWidget(control("DOWN settings", 0, 52, false, button -> reopen(5)));
        addRenderableWidget(control("Apply pack", 1, 52, false, button -> applyPack()));
    }

    private void addArrowDownCompact() {
        String down = valueOrDefault("yte_down", DEFAULT_DOWN_ARROW);
        String downLabel = ElevatorMediaStore.get(liftId, "arrow_down") != null ? "Custom PNG"
                : ElevatorMediaStore.get(liftId, "yte_down") == null ? "MTR default" : shortName(down);
        addRenderableWidget(control("Previous DOWN", 0, 0, false,
                button -> cycleArrow("yte_down", down, -1)));
        addRenderableWidget(control("DOWN: " + downLabel, 1, 0, false,
                button -> cycleArrow("yte_down", down, 1)));
        addRenderableWidget(control("Custom DOWN PNG", 0, 24, false,
                button -> ElevatorMediaStore.choose(liftId, "arrow_down")));
        addRenderableWidget(control("Use YTE DOWN", 1, 24, false,
                button -> useYteArrow(false)));
        addRenderableWidget(control("UP settings", 0, 52, false, button -> reopen(0)));
        addRenderableWidget(control("Apply pack", 1, 52, false, button -> applyPack()));
    }

    private void addNumberTab() {
        if (height < 165) {
            addNumberCompact();
            return;
        }
        String fontId = valueOrDefault("floor_font", DEFAULT_FONT);
        String customFont = ElevatorMediaStore.get(liftId, "font_custom");
        String fontLabel = customFont != null ? "Custom font"
                : ElevatorMediaStore.get(liftId, "floor_font") == null
                ? "MTR default" : shortName(fontId);
        boolean enabled = !"false".equals(ElevatorMediaStore.get(liftId, "floor_enabled"));
        addRenderableWidget(control("Font: " + fontLabel, 0, 0, true,
                button -> cycleFont(fontId)));
        addRenderableWidget(control("Custom TTF/OTF" + customStatus(customFont), 0, 26, false,
                button -> ElevatorMediaStore.choose(liftId, "font_custom")));
        addRenderableWidget(control("Use YTE font", 1, 26, false,
                button -> useYteFont()));
        addRenderableWidget(control("Floor number: " + (enabled ? "ON" : "OFF"), 0, 54, true,
                button -> {
                    // ON is the vanilla default. Clearing OFF instead of
                    // storing an explicit true avoids needless MTR display overrides.
                    ElevatorMediaStore.set(liftId, "floor_enabled", enabled ? "false" : null);
                    reopen(1);
                }));
        addRenderableWidget(control("MTR default font", 0, 82, false,
                button -> resetFont()));
        addRenderableWidget(control("Apply media pack", 1, 82, false,
                button -> applyPack()));
    }

    private void addNumberCompact() {
        String fontId = valueOrDefault("floor_font", DEFAULT_FONT);
        String fontLabel = ElevatorMediaStore.get(liftId, "font_custom") != null ? "Custom font"
                : ElevatorMediaStore.get(liftId, "floor_font") == null
                ? "MTR default" : shortName(fontId);
        boolean enabled = !"false".equals(ElevatorMediaStore.get(liftId, "floor_enabled"));
        addRenderableWidget(control("Font: " + fontLabel, 0, 0, true,
                button -> cycleFont(fontId)));
        addRenderableWidget(control("Custom TTF/OTF", 0, 24, false,
                button -> ElevatorMediaStore.choose(liftId, "font_custom")));
        addRenderableWidget(control("MTR default font", 1, 24, false,
                button -> resetFont()));
        addRenderableWidget(control("Floor: " + (enabled ? "ON" : "OFF"), 0, 52, false,
                button -> {
                    ElevatorMediaStore.set(liftId, "floor_enabled", enabled ? "false" : null);
                    reopen(1);
                }));
        addRenderableWidget(control("Apply pack", 1, 52, false,
                button -> applyPack()));
    }

    private void addChimeTab() {
        if (height < 205) {
            addChimeUpCompact();
            return;
        }
        String up = valueOrDefault("chime_up", DEFAULT_UP_CHIME);
        String down = valueOrDefault("chime_down", DEFAULT_DOWN_CHIME);
        String upLabel = ElevatorMediaStore.get(liftId, "chime_up") == null ? "JCM default" : shortName(up);
        String downLabel = ElevatorMediaStore.get(liftId, "chime_down") == null ? "JCM default" : shortName(down);
        addRenderableWidget(control("Previous UP", 0, 0, false,
                button -> cycleChime("chime_up", up, YTE_UP_CHIMES, -1)));
        addRenderableWidget(control("UP: " + upLabel, 1, 0, false,
                button -> cycleChime("chime_up", up, YTE_UP_CHIMES, 1)));
        addRenderableWidget(control("Custom UP OGG" + customStatus(up), 0, 23, false,
                button -> ElevatorMediaStore.choose(liftId, "chime_up")));
        addRenderableWidget(control("Reset UP to YTE", 1, 23, false,
                button -> setDefaultChime("chime_up", DEFAULT_UP_CHIME)));
        addRenderableWidget(control("Previous DOWN", 0, 49, false,
                button -> cycleChime("chime_down", down, YTE_DOWN_CHIMES, -1)));
        addRenderableWidget(control("DOWN: " + downLabel, 1, 49, false,
                button -> cycleChime("chime_down", down, YTE_DOWN_CHIMES, 1)));
        addRenderableWidget(control("Custom DOWN OGG" + customStatus(down), 0, 72, false,
                button -> ElevatorMediaStore.choose(liftId, "chime_down")));
        addRenderableWidget(control("Reset DOWN to YTE", 1, 72, false,
                button -> setDefaultChime("chime_down", DEFAULT_DOWN_CHIME)));
        addRenderableWidget(control("Apply media pack", 0, 97, false,
                button -> applyPack()));
        addRenderableWidget(control("Test selected sounds", 1, 97, false,
                button -> reopen(3)));
    }

    private void addChimeUpCompact() {
        String up = valueOrDefault("chime_up", DEFAULT_UP_CHIME);
        String upLabel = ElevatorMediaStore.get(liftId, "chime_up") == null ? "JCM default" : shortName(up);
        addRenderableWidget(control("Previous UP", 0, 0, false,
                button -> cycleChime("chime_up", up, YTE_UP_CHIMES, -1)));
        addRenderableWidget(control("UP: " + upLabel, 1, 0, false,
                button -> cycleChime("chime_up", up, YTE_UP_CHIMES, 1)));
        addRenderableWidget(control("Custom UP OGG", 0, 24, false,
                button -> ElevatorMediaStore.choose(liftId, "chime_up")));
        addRenderableWidget(control("Use JCM UP", 1, 24, false,
                button -> clear("chime_up")));
        addRenderableWidget(control("DOWN settings", 0, 52, false, button -> reopen(6)));
        addRenderableWidget(control("Test / timing", 1, 52, false, button -> reopen(3)));
    }

    private void addChimeDownCompact() {
        String down = valueOrDefault("chime_down", DEFAULT_DOWN_CHIME);
        String downLabel = ElevatorMediaStore.get(liftId, "chime_down") == null ? "JCM default" : shortName(down);
        addRenderableWidget(control("Previous DOWN", 0, 0, false,
                button -> cycleChime("chime_down", down, YTE_DOWN_CHIMES, -1)));
        addRenderableWidget(control("DOWN: " + downLabel, 1, 0, false,
                button -> cycleChime("chime_down", down, YTE_DOWN_CHIMES, 1)));
        addRenderableWidget(control("Custom DOWN OGG", 0, 24, false,
                button -> ElevatorMediaStore.choose(liftId, "chime_down")));
        addRenderableWidget(control("Use JCM DOWN", 1, 24, false,
                button -> clear("chime_down")));
        addRenderableWidget(control("UP settings", 0, 52, false, button -> reopen(2)));
        addRenderableWidget(control("Test / timing", 1, 52, false, button -> reopen(3)));
    }

    private void addScriptTab() {
        boolean enabled = "true".equals(ElevatorMediaStore.get(liftId, "scripted_overlay"));
        addRenderableWidget(control("Scripted arrow overlay: " + (enabled ? "ON" : "OFF"), 0, 0, true,
                button -> {
                    ElevatorMediaStore.set(liftId, "scripted_overlay", enabled ? null : "true");
                    reopen(4);
                }));
        addRenderableWidget(control("Safe mode (recommended)", 0, 28, true,
                button -> {
                    ElevatorMediaStore.set(liftId, "scripted_overlay", null);
                    reopen(4);
                }));
        addRenderableWidget(control("Apply media pack / reload", 0, 52, true,
                button -> applyPack()));
        if (height >= 165) {
            addRenderableWidget(control("Restore both MTR arrows", 0, 84, true,
                    button -> restoreVanillaArrows()));
        }
    }

    private void restoreVanillaArrows() {
        ElevatorMediaStore.set(liftId, "arrow_up", null);
        ElevatorMediaStore.set(liftId, "arrow_down", null);
        ElevatorMediaStore.set(liftId, "yte_up", null);
        ElevatorMediaStore.set(liftId, "yte_down", null);
        reopen(4);
    }

    /** A compact preview page is available at every GUI scale. */
    private void addChimePreviewTab() {
        addRenderableWidget(control("Test selected UP", 0, 0, true,
                button -> previewChime(true)));
        addRenderableWidget(control("Test selected DOWN", 0, 25, true,
                button -> previewChime(false)));
        if (height >= 160) {
            addRenderableWidget(control("Apply media pack / reload", 0, 52, true,
                    button -> applyPack()));
        }
        String timing = valueOrDefault("chime_trigger", "arrival");
        String label = switch (timing) {
            case "deceleration" -> "Slowdown";
            case "door_open" -> "Door opens";
            default -> "Arrival (JCM)";
        };
        if (height >= 160) addRenderableWidget(control("Chime timing: " + label, 0, 78, true,
                button -> {
                    String next = switch (timing) {
                        case "deceleration" -> "door_open";
                        case "door_open" -> null;
                        default -> "deceleration";
                    };
                    ElevatorMediaStore.set(liftId, "chime_trigger", next);
                    reopen(3);
                }));
        if (height < 160) {
            addRenderableWidget(control("Timing: " + label, 0, 52, true, button -> {
                String next = switch (timing) {
                    case "deceleration" -> "door_open";
                    case "door_open" -> null;
                    default -> "deceleration";
                };
                ElevatorMediaStore.set(liftId, "chime_trigger", next);
                reopen(3);
            }));
        }
        if (height >= 230) {
            addRenderableWidget(control("Use JCM UP", 0, 104, false,
                    button -> clearChime("chime_up")));
            addRenderableWidget(control("Use JCM DOWN", 1, 104, false,
                    button -> clearChime("chime_down")));
        }
    }

    private void clearChime(String key) {
        ElevatorMediaStore.set(liftId, key, null);
        reopen(3);
    }

    private void previewChime(boolean up) {
        String error = ElevatorMediaStore.previewChime(liftId, up);
        applyFailed = error != null;
        statusMessage = error == null
                ? (ElevatorMediaStore.get(liftId, up ? "chime_up" : "chime_down") == null
                        ? "Playing the original JCM chime."
                        : (up ? "Playing selected UP chime." : "Playing selected DOWN chime."))
                : error;
    }

    private void applyPack() {
        String error = ElevatorMediaStore.reloadMediaPack();
        applyFailed = error != null;
        statusMessage = error != null ? error : "Resource pack reload started.";
    }

    private void cycleArrow(String key, String current, int step) {
        int index = ElevatorMediaStore.get(liftId, key) == null
                ? -1 : ARROW_TEXTURES.indexOf(current);
        if (index < 0) index = step < 0 ? 0 : -1;
        index = Math.floorMod(index + step, ARROW_TEXTURES.size());
        // A selected PNG previously took priority over a new YTE selection.
        ElevatorMediaStore.set(liftId, "yte_up".equals(key) ? "arrow_up" : "arrow_down", null);
        ElevatorMediaStore.set(liftId, key, ARROW_TEXTURES.get(index));
        reopen(height < 205 && "yte_down".equals(key) ? 5 : 0);
    }

    private void cycleFont(String current) {
        int index = ElevatorMediaStore.get(liftId, "floor_font") == null
                ? -1 : YTE_FONTS.indexOf(current);
        if (index < 0) index = -1;
        ElevatorMediaStore.set(liftId, "font_custom", null);
        ElevatorMediaStore.set(liftId, "floor_font", YTE_FONTS.get((index + 1) % YTE_FONTS.size()));
        reopen(1);
    }

    private void cycleChime(String key, String current, List<String> sounds, int step) {
        // An unset sound belongs to JCM, not the first YTE entry.
        int index = ElevatorMediaStore.get(liftId, key) == null ? -1 : sounds.indexOf(current);
        if (index < 0) index = step < 0 ? 0 : -1;
        ElevatorMediaStore.set(liftId, key, sounds.get(Math.floorMod(index + step, sounds.size())));
        reopen(height < 205 && "chime_down".equals(key) ? 6 : 2);
    }

    /** Selecting YTE must actually write a YTE resource ID, not fall back
     * to vanilla MTR if there was never an explicit YTE selection. */
    private void useYteArrow(boolean up) {
        String key = up ? "yte_up" : "yte_down";
        String oldCustom = up ? "arrow_up" : "arrow_down";
        String defaultId = up ? DEFAULT_UP_ARROW : DEFAULT_DOWN_ARROW;
        ElevatorMediaStore.set(liftId, oldCustom, null);
        if (ElevatorMediaStore.get(liftId, key) == null) {
            ElevatorMediaStore.set(liftId, key, defaultId);
        }
        reopen(height < 205 && !up ? 5 : 0);
    }

    private void useYteFont() {
        ElevatorMediaStore.set(liftId, "font_custom", null);
        if (ElevatorMediaStore.get(liftId, "floor_font") == null) {
            ElevatorMediaStore.set(liftId, "floor_font", DEFAULT_FONT);
        }
        reopen(1);
    }

    private void resetFont() {
        ElevatorMediaStore.set(liftId, "font_custom", null);
        ElevatorMediaStore.set(liftId, "floor_font", null);
        reopen(1);
    }

    private void setDefaultChime(String key, String sound) {
        ElevatorMediaStore.set(liftId, key, sound);
        reopen(height < 205 && "chime_down".equals(key) ? 6 : 2);
    }

    private void clear(String key) {
        ElevatorMediaStore.set(liftId, key, null);
        reopen(tab);
    }

    private void reopen(int targetTab) {
        minecraft.setScreen(new ElevatorMediaSettingsScreen(parent, liftId, targetTab));
    }

    private String valueOrDefault(String key, String defaultValue) {
        String value = ElevatorMediaStore.get(liftId, key);
        return value == null ? defaultValue : value;
    }

    private static String shortName(String value) {
        int separator = value.lastIndexOf('/');
        return separator >= 0 ? value.substring(separator + 1) : value;
    }

    private static String customStatus(String value) {
        return value != null && value.startsWith("mtr_jcm_ev:") ? " (active)" : "";
    }
}
