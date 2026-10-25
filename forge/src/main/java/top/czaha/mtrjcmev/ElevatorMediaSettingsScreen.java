package top.czaha.mtrjcmev;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
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
        int panel = panelWidth();
        int tabWidth = (panel - 8) / 3;
        int left = (width - panel) / 2;
        addRenderableWidget(tabButton(left, tabWidth, "Arrows", 0));
        addRenderableWidget(tabButton(left + tabWidth + 4, tabWidth, "Numbers", 1));
        addRenderableWidget(tabButton(left + (tabWidth + 4) * 2, panel - (tabWidth + 4) * 2,
                "Chimes", 2));

        if (tab == 0) addArrowTab();
        else if (tab == 1) addNumberTab();
        else addChimeTab();

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
        Button button = Button.builder(Component.literal(label), pressed ->
                        minecraft.setScreen(new ElevatorMediaSettingsScreen(parent, liftId, targetTab)))
                .bounds(x, 32, buttonWidth, 20).build();
        button.active = targetTab != tab;
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
        return Button.builder(Component.literal(label), onPress)
                .bounds(x, contentTop() + controlY, buttonWidth, controlHeight).build();
    }

    private void addArrowTab() {
        String up = valueOrDefault("yte_up", DEFAULT_UP_ARROW);
        String down = valueOrDefault("yte_down", DEFAULT_DOWN_ARROW);
        String customUp = ElevatorMediaStore.get(liftId, "arrow_up");
        String customDown = ElevatorMediaStore.get(liftId, "arrow_down");

        addRenderableWidget(control("Previous UP", 0, 0, false,
                button -> cycleArrow("yte_up", up, -1)));
        addRenderableWidget(control("UP: " + shortName(up), 1, 0, false,
                button -> cycleArrow("yte_up", up, 1)));
        addRenderableWidget(control("Custom UP PNG" + customStatus(customUp), 0, 24, false,
                button -> ElevatorMediaStore.choose(liftId, "arrow_up")));
        addRenderableWidget(control("Use YTE UP arrow", 1, 24, false,
                button -> clear("arrow_up")));

        addRenderableWidget(control("Previous DOWN", 0, 52, false,
                button -> cycleArrow("yte_down", down, -1)));
        addRenderableWidget(control("DOWN: " + shortName(down), 1, 52, false,
                button -> cycleArrow("yte_down", down, 1)));
        addRenderableWidget(control("Custom DOWN PNG" + customStatus(customDown), 0, 76, false,
                button -> ElevatorMediaStore.choose(liftId, "arrow_down")));
        addRenderableWidget(control("Use YTE DOWN arrow", 1, 76, false,
                button -> clear("arrow_down")));
        addRenderableWidget(control("Apply media pack / reload", 0, 101, true,
                button -> applyPack()));
    }

    private void addNumberTab() {
        String fontId = valueOrDefault("floor_font", DEFAULT_FONT);
        String customFont = ElevatorMediaStore.get(liftId, "font_custom");
        boolean enabled = !"false".equals(ElevatorMediaStore.get(liftId, "floor_enabled"));
        addRenderableWidget(control("YTE font: " + shortName(fontId), 0, 0, true,
                button -> cycleFont(fontId)));
        addRenderableWidget(control("Custom TTF/OTF" + customStatus(customFont), 0, 26, false,
                button -> ElevatorMediaStore.choose(liftId, "font_custom")));
        addRenderableWidget(control("Use YTE font", 1, 26, false,
                button -> clear("font_custom")));
        addRenderableWidget(control("Floor number: " + (enabled ? "ON" : "OFF"), 0, 54, true,
                button -> {
                    ElevatorMediaStore.set(liftId, "floor_enabled", Boolean.toString(!enabled));
                    reopen(1);
                }));
        addRenderableWidget(control("Apply media pack / reload", 0, 82, true,
                button -> applyPack()));
    }

    private void addChimeTab() {
        String up = valueOrDefault("chime_up", DEFAULT_UP_CHIME);
        String down = valueOrDefault("chime_down", DEFAULT_DOWN_CHIME);
        addRenderableWidget(control("Previous UP", 0, 0, false,
                button -> cycleChime("chime_up", up, YTE_UP_CHIMES, -1)));
        addRenderableWidget(control("UP: " + shortName(up), 1, 0, false,
                button -> cycleChime("chime_up", up, YTE_UP_CHIMES, 1)));
        addRenderableWidget(control("Custom UP OGG" + customStatus(up), 0, 23, false,
                button -> ElevatorMediaStore.choose(liftId, "chime_up")));
        addRenderableWidget(control("Reset UP to YTE", 1, 23, false,
                button -> setDefaultChime("chime_up", DEFAULT_UP_CHIME)));
        addRenderableWidget(control("Previous DOWN", 0, 49, false,
                button -> cycleChime("chime_down", down, YTE_DOWN_CHIMES, -1)));
        addRenderableWidget(control("DOWN: " + shortName(down), 1, 49, false,
                button -> cycleChime("chime_down", down, YTE_DOWN_CHIMES, 1)));
        addRenderableWidget(control("Custom DOWN OGG" + customStatus(down), 0, 72, false,
                button -> ElevatorMediaStore.choose(liftId, "chime_down")));
        addRenderableWidget(control("Reset DOWN to YTE", 1, 72, false,
                button -> setDefaultChime("chime_down", DEFAULT_DOWN_CHIME)));
        if (height >= 255) {
            addRenderableWidget(control("Test selected UP", 0, 97, false,
                    button -> previewChime(true)));
            addRenderableWidget(control("Test selected DOWN", 1, 97, false,
                    button -> previewChime(false)));
            addRenderableWidget(control("Apply media pack / reload", 0, 122, true,
                    button -> applyPack()));
        } else {
            addRenderableWidget(control("Apply media pack / reload", 0, 97, true,
                    button -> applyPack()));
        }
    }

    private void previewChime(boolean up) {
        String error = ElevatorMediaStore.previewChime(liftId, up);
        applyFailed = error != null;
        statusMessage = error == null
                ? (up ? "Playing selected UP chime." : "Playing selected DOWN chime.")
                : error;
    }

    private void applyPack() {
        String error = ElevatorMediaStore.reloadMediaPack();
        applyFailed = error != null;
        statusMessage = error != null ? error : "Resource pack reload started.";
    }

    private void cycleArrow(String key, String current, int step) {
        int index = ARROW_TEXTURES.indexOf(current);
        if (index < 0) index = step < 0 ? 0 : -1;
        index = Math.floorMod(index + step, ARROW_TEXTURES.size());
        ElevatorMediaStore.set(liftId, key, ARROW_TEXTURES.get(index));
        reopen(0);
    }

    private void cycleFont(String current) {
        int index = YTE_FONTS.indexOf(current);
        if (index < 0) index = -1;
        ElevatorMediaStore.set(liftId, "floor_font", YTE_FONTS.get((index + 1) % YTE_FONTS.size()));
        reopen(1);
    }

    private void cycleChime(String key, String current, List<String> sounds, int step) {
        int index = sounds.indexOf(current);
        if (index < 0) index = step < 0 ? 0 : -1;
        ElevatorMediaStore.set(liftId, key, sounds.get(Math.floorMod(index + step, sounds.size())));
        reopen(2);
    }

    private void setDefaultChime(String key, String sound) {
        ElevatorMediaStore.set(liftId, key, sound);
        reopen(2);
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
