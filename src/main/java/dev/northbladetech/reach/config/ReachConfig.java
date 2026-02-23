package dev.northbladetech.reach.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;

public final class ReachConfig {
    private static final Logger LOGGER = LoggerFactory.getLogger("Reach");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path PATH = FabricLoader.getInstance().getConfigDir().resolve("reach.json");
    private static ReachConfig instance;

    public int segments = 96;
    public float lineWidth = 34.5f;
    public float alpha = 0.9f;
    public Colors colors = new Colors();

    public static ReachConfig get() {
        if (instance == null) {
            instance = load();
        }
        return instance;
    }

    public static int parseColor(String hex, int fallback) {
        if (hex == null) {
            return fallback;
        }
        String value = hex.trim();
        if (value.startsWith("#")) {
            value = value.substring(1);
        }
        if (value.length() == 6) {
            value = "FF" + value;
        }
        if (value.length() != 8) {
            return fallback;
        }
        try {
            return (int) Long.parseLong(value, 16);
        } catch (NumberFormatException e) {
            return fallback;
        }
    }

    public void saveConfig() {
        validate();
        save();
    }

    private static ReachConfig load() {
        ReachConfig config = new ReachConfig();
        if (Files.exists(PATH)) {
            try (Reader reader = Files.newBufferedReader(PATH)) {
                ReachConfig loaded = GSON.fromJson(reader, ReachConfig.class);
                if (loaded != null) {
                    config = loaded;
                }
            } catch (Exception e) {
                LOGGER.error("Reach config load error", e);
            }
        }
        config.validate();
        config.save();
        return config;
    }

    private void save() {
        try {
            Files.createDirectories(PATH.getParent());
            try (Writer writer = Files.newBufferedWriter(PATH)) {
                GSON.toJson(this, writer);
            }
        } catch (Exception e) {
            LOGGER.error("Reach config save error", e);
        }
    }

    private void validate() {
        if (segments < 16) {
            segments = 16;
        }
        if (segments > 512) {
            segments = 512;
        }
        if (lineWidth < 1.0f) {
            lineWidth = 1.0f;
        }
        if (lineWidth > 111.0f) {
            lineWidth = 111.0f;
        }
        if (alpha < 0.0f) {
            alpha = 0.0f;
        }
        if (alpha > 1.0f) {
            alpha = 1.0f;
        }
        if (colors == null) {
            colors = new Colors();
        }
        colors.normalize();
    }

    public static final class Colors {
        public String localDefault = "FF0000";
        public String localAnyInLocal = "00FF00";
        public String localAnyInOther = "00B7FF";
        public String localMutual = "00FF7F";
        public String otherDefault = "FF0000";
        public String otherWithinLocal = "00FF00";
        public String otherLocalWithinOther = "00B7FF";
        public String otherMutual = "00FF7F";

        private void normalize() {
            if (localDefault == null || localDefault.isBlank()) {
                localDefault = "FF0000";
            }
            if (localAnyInLocal == null || localAnyInLocal.isBlank()) {
                localAnyInLocal = "00FF00";
            }
            if (localAnyInOther == null || localAnyInOther.isBlank()) {
                localAnyInOther = "00B7FF";
            }
            if (localMutual == null || localMutual.isBlank()) {
                localMutual = "00FF7F";
            }
            if (otherDefault == null || otherDefault.isBlank()) {
                otherDefault = "FF0000";
            }
            if (otherWithinLocal == null || otherWithinLocal.isBlank()) {
                otherWithinLocal = "00FF00";
            }
            if (otherLocalWithinOther == null || otherLocalWithinOther.isBlank()) {
                otherLocalWithinOther = "00B7FF";
            }
            if (otherMutual == null || otherMutual.isBlank()) {
                otherMutual = "00FF7F";
            }
        }
    }
}
