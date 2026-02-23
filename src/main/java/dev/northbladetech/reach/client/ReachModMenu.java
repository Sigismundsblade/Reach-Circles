package dev.northbladetech.reach.client;

import dev.northbladetech.reach.config.ReachConfig;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.text.Text;
import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;

public final class ReachModMenu implements ModMenuApi {
    private enum ColorPreset {
        BRIGHT_RED("Bright Red", "FF0000"),
        RED("Red", "CC0000"),
        ORANGE("Orange", "FF7A00"),
        AMBER("Amber", "FFB000"),
        YELLOW("Yellow", "FFE600"),
        LIME("Lime", "7CFF00"),
        GREEN("Green", "00FF00"),
        EMERALD("Emerald", "00FF7F"),
        TEAL("Teal", "00C8A0"),
        CYAN("Cyan", "00FFFF"),
        AQUA("Aqua", "00B7FF"),
        SKY("Sky", "4AA3FF"),
        BLUE("Blue", "0080FF"),
        NAVY("Navy", "1E3A8A"),
        PURPLE("Purple", "7E3FFF"),
        VIOLET("Violet", "B100FF"),
        MAGENTA("Magenta", "FF00FF"),
        PINK("Pink", "FF5BD6"),
        ROSE("Rose", "FF3366"),
        WHITE("White", "FFFFFF"),
        LIGHT_GRAY("Light Gray", "CFCFCF"),
        GRAY("Gray", "8A8A8A"),
        DARK_GRAY("Dark Gray", "444444"),
        BLACK("Black", "000000");

        private final String label;
        private final String hex;

        ColorPreset(String label, String hex) {
            this.label = label;
            this.hex = hex;
        }

        public String hex() {
            return hex;
        }

        @Override
        public String toString() {
            return label;
        }

        public static ColorPreset fromHex(String value, ColorPreset fallback) {
            if (value == null) {
                return fallback;
            }
            String hex = value.trim();
            if (hex.startsWith("#")) {
                hex = hex.substring(1);
            }
            if (hex.length() == 8) {
                hex = hex.substring(2);
            }
            for (ColorPreset preset : values()) {
                if (preset.hex.equalsIgnoreCase(hex)) {
                    return preset;
                }
            }
            return fallback;
        }
    }

    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return parent -> {
            ReachConfig config = ReachConfig.get();
            ConfigBuilder builder = ConfigBuilder.create()
                    .setParentScreen(parent)
                    .setTitle(Text.literal("Reach Circle"));

            ConfigEntryBuilder entryBuilder = builder.entryBuilder();
            ConfigCategory rendering = builder.getOrCreateCategory(Text.literal("Rendering"));
            rendering.addEntry(entryBuilder.startIntSlider(Text.literal("Smoothness"), config.segments, 16, 512)
                    .setDefaultValue(96)
                    .setSaveConsumer(value -> config.segments = value)
                    .build());
            rendering.addEntry(entryBuilder.startFloatField(Text.literal("Line Thickness"), config.lineWidth)
                    .setDefaultValue(34.5f)
                    .setMin(1.0f)
                    .setMax(111.0f)
                    .setSaveConsumer(value -> config.lineWidth = value)
                    .build());
            rendering.addEntry(entryBuilder.startFloatField(Text.literal("Opacity"), config.alpha)
                    .setDefaultValue(0.9f)
                    .setMin(0.0f)
                    .setMax(1.0f)
                    .setSaveConsumer(value -> config.alpha = value)
                    .build());

            ConfigCategory localColors = builder.getOrCreateCategory(Text.literal("Local Colors"));
            localColors.addEntry(entryBuilder.startEnumSelector(Text.literal("Local Default"), ColorPreset.class,
                            ColorPreset.fromHex(config.colors.localDefault, ColorPreset.BRIGHT_RED))
                    .setDefaultValue(ColorPreset.BRIGHT_RED)
                    .setSaveConsumer(value -> config.colors.localDefault = value.hex())
                    .build());
            localColors.addEntry(entryBuilder.startEnumSelector(Text.literal("Local Any In Local"), ColorPreset.class,
                            ColorPreset.fromHex(config.colors.localAnyInLocal, ColorPreset.GREEN))
                    .setDefaultValue(ColorPreset.GREEN)
                    .setSaveConsumer(value -> config.colors.localAnyInLocal = value.hex())
                    .build());
            localColors.addEntry(entryBuilder.startEnumSelector(Text.literal("Local Any In Other"), ColorPreset.class,
                            ColorPreset.fromHex(config.colors.localAnyInOther, ColorPreset.AQUA))
                    .setDefaultValue(ColorPreset.AQUA)
                    .setSaveConsumer(value -> config.colors.localAnyInOther = value.hex())
                    .build());
            localColors.addEntry(entryBuilder.startEnumSelector(Text.literal("Local Mutual"), ColorPreset.class,
                            ColorPreset.fromHex(config.colors.localMutual, ColorPreset.EMERALD))
                    .setDefaultValue(ColorPreset.EMERALD)
                    .setSaveConsumer(value -> config.colors.localMutual = value.hex())
                    .build());

            ConfigCategory otherColors = builder.getOrCreateCategory(Text.literal("Other Colors"));
            otherColors.addEntry(entryBuilder.startEnumSelector(Text.literal("Other Default"), ColorPreset.class,
                            ColorPreset.fromHex(config.colors.otherDefault, ColorPreset.BRIGHT_RED))
                    .setDefaultValue(ColorPreset.BRIGHT_RED)
                    .setSaveConsumer(value -> config.colors.otherDefault = value.hex())
                    .build());
            otherColors.addEntry(entryBuilder.startEnumSelector(Text.literal("Other Within Local"), ColorPreset.class,
                            ColorPreset.fromHex(config.colors.otherWithinLocal, ColorPreset.GREEN))
                    .setDefaultValue(ColorPreset.GREEN)
                    .setSaveConsumer(value -> config.colors.otherWithinLocal = value.hex())
                    .build());
            otherColors.addEntry(entryBuilder.startEnumSelector(Text.literal("Other Local Within Other"), ColorPreset.class,
                            ColorPreset.fromHex(config.colors.otherLocalWithinOther, ColorPreset.AQUA))
                    .setDefaultValue(ColorPreset.AQUA)
                    .setSaveConsumer(value -> config.colors.otherLocalWithinOther = value.hex())
                    .build());
            otherColors.addEntry(entryBuilder.startEnumSelector(Text.literal("Other Mutual"), ColorPreset.class,
                            ColorPreset.fromHex(config.colors.otherMutual, ColorPreset.EMERALD))
                    .setDefaultValue(ColorPreset.EMERALD)
                    .setSaveConsumer(value -> config.colors.otherMutual = value.hex())
                    .build());

            builder.setSavingRunnable(config::saveConfig);
            return builder.build();
        };
    }
}
