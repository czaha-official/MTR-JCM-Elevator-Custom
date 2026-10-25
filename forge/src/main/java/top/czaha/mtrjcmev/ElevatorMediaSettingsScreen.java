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

    @Override
    protected void init() {
        int centerX = width / 2;
        addRenderableWidget(tabButton(centerX - 154, "Arrows", 0));
        addRenderableWidget(tabButton(centerX - 52, "Numbers", 1));
        addRenderableWidget(tabButton(centerX + 50, "Chimes", 2));

        if (tab == 0) addArrowTab(centerX);
        else if (tab == 1) addNumberTab(centerX);
        else addChimeTab(centerX);

        addRenderableWidget(Button.builder(Component.literal("Done"), button -> minecraft.setScreen(parent))
                .bounds(centerX - 50, height - 35, 100, 20).build());
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics);
        graphics.drawCenteredString(font, title, width / 2, 18, 0xFFFFFF);
        graphics.drawCenteredString(font, Component.literal("Lift ID: " + liftId), width / 2, 64, 0xAAAAAA);
        super.render(graphics, mouseX, mouseY, partialTick);
    }

    @Override
    public void onClose() {
        minecraft.setScreen(parent);
    }

    private Button tabButton(int x, String label, int targetTab) {
        return Button.builder(Component.literal(label), button ->
                        minecraft.setScreen(new ElevatorMediaSettingsScreen(parent, liftId, targetTab)))
                .bounds(x, 38, 96, 20).build();
    }

    private void addArrowTab(int centerX) {
        String up = valueOrDefault("yte_up", DEFAULT_UP_ARROW);
        String down = valueOrDefault("yte_down", DEFAULT_DOWN_ARROW);
        String customUp = ElevatorMediaStore.get(liftId, "arrow_up");
        String customDown = ElevatorMediaStore.get(liftId, "arrow_down");
        addRenderableWidget(Button.builder(Component.literal("YTE up: " + shortName(up)), button -> cycleArrow("yte_up", up, -1))
                .bounds(centerX - 155, 100, 150, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Next up: " + shortName(up)), button -> cycleArrow("yte_up", up, 1))
                .bounds(centerX + 5, 100, 150, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Use custom UP PNG" + customStatus(customUp)), button -> ElevatorMediaStore.choose(liftId, "arrow_up"))
                .bounds(centerX - 155, 128, 150, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Use YTE UP arrow"), button -> clear("arrow_up"))
                .bounds(centerX + 5, 128, 150, 20).build());

        addRenderableWidget(Button.builder(Component.literal("YTE down: " + shortName(down)), button -> cycleArrow("yte_down", down, -1))
                .bounds(centerX - 155, 174, 150, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Next down: " + shortName(down)), button -> cycleArrow("yte_down", down, 1))
                .bounds(centerX + 5, 174, 150, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Use custom DOWN PNG" + customStatus(customDown)), button -> ElevatorMediaStore.choose(liftId, "arrow_down"))
                .bounds(centerX - 155, 202, 150, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Use YTE DOWN arrow"), button -> clear("arrow_down"))
                .bounds(centerX + 5, 202, 150, 20).build());
    }

    private void addNumberTab(int centerX) {
        String fontId = valueOrDefault("floor_font", DEFAULT_FONT);
        String customFont = ElevatorMediaStore.get(liftId, "font_custom");
        boolean enabled = !"false".equals(ElevatorMediaStore.get(liftId, "floor_enabled"));
        addRenderableWidget(Button.builder(Component.literal("YTE font: " + shortName(fontId)), button -> cycleFont(fontId))
                .bounds(centerX - 155, 102, 310, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Use custom TTF/OTF" + customStatus(customFont)), button -> ElevatorMediaStore.choose(liftId, "font_custom"))
                .bounds(centerX - 155, 132, 150, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Use selected YTE font"), button -> clear("font_custom"))
                .bounds(centerX + 5, 132, 150, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Floor number: " + (enabled ? "ON" : "OFF")), button -> {
                    ElevatorMediaStore.set(liftId, "floor_enabled", Boolean.toString(!enabled));
                    reopen(1);
                })
                .bounds(centerX - 100, 170, 200, 20).build());
    }

    private void addChimeTab(int centerX) {
        String up = valueOrDefault("chime_up", DEFAULT_UP_CHIME);
        String down = valueOrDefault("chime_down", DEFAULT_DOWN_CHIME);
        addRenderableWidget(Button.builder(Component.literal("UP: " + shortName(up)), button -> cycleChime("chime_up", up, YTE_UP_CHIMES, -1))
                .bounds(centerX - 155, 100, 150, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Next UP sound"), button -> cycleChime("chime_up", up, YTE_UP_CHIMES, 1))
                .bounds(centerX + 5, 100, 150, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Use custom UP OGG" + customStatus(up)), button -> ElevatorMediaStore.choose(liftId, "chime_up"))
                .bounds(centerX - 155, 128, 150, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Reset UP to YTE"), button -> setDefaultChime("chime_up", DEFAULT_UP_CHIME))
                .bounds(centerX + 5, 128, 150, 20).build());
        addRenderableWidget(Button.builder(Component.literal("DOWN: " + shortName(down)), button -> cycleChime("chime_down", down, YTE_DOWN_CHIMES, -1))
                .bounds(centerX - 155, 166, 150, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Next DOWN sound"), button -> cycleChime("chime_down", down, YTE_DOWN_CHIMES, 1))
                .bounds(centerX + 5, 166, 150, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Use custom DOWN OGG" + customStatus(down)), button -> ElevatorMediaStore.choose(liftId, "chime_down"))
                .bounds(centerX - 155, 194, 150, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Reset DOWN to YTE"), button -> setDefaultChime("chime_down", DEFAULT_DOWN_CHIME))
                .bounds(centerX + 5, 194, 150, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Apply custom OGG files (resource reload)"), button -> ElevatorMediaStore.reloadMediaPack())
                .bounds(centerX - 155, 225, 310, 20).build());
    }

    private void cycleArrow(String key, String current, int step) {
        int index = ARROW_TEXTURES.indexOf(current);
        if (index < 0) index = 0;
        index = Math.floorMod(index + step, ARROW_TEXTURES.size());
        ElevatorMediaStore.set(liftId, key, ARROW_TEXTURES.get(index));
        reopen(0);
    }

    private void cycleFont(String current) {
        int index = YTE_FONTS.indexOf(current);
        if (index < 0) index = 0;
        ElevatorMediaStore.set(liftId, "floor_font", YTE_FONTS.get((index + 1) % YTE_FONTS.size()));
        reopen(1);
    }

    private void cycleChime(String key, String current, List<String> sounds, int step) {
        int index = sounds.indexOf(current);
        if (index < 0) index = -1;
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
