package cn.orzmc.template.paper.config;

import cn.orzmc.template.core.config.ConfigStore;
import java.io.IOException;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

/** Paper YAML adapter. Bukkit/Paper types do not cross into Core. */
public final class PaperYamlConfigStore implements ConfigStore {
    private final JavaPlugin plugin;
    private final Path file;

    public PaperYamlConfigStore(JavaPlugin plugin) {
        this.plugin = plugin;
        this.file = plugin.getDataFolder().toPath().resolve("config.yml");
    }

    @Override public boolean exists() { return Files.isRegularFile(file); }

    @Override public Map<String, Object> load() throws IOException {
        if (!exists()) plugin.saveResource("config.yml", false);
        YamlConfiguration yaml = YamlConfiguration.loadConfiguration(file.toFile());
        return copySection(yaml);
    }

    @Override public void backup() throws IOException {
        if (!exists()) return;
        Path backup = file.resolveSibling("config.yml.bak-" + Instant.now().toEpochMilli());
        Files.copy(file, backup, StandardCopyOption.COPY_ATTRIBUTES);
    }

    @Override public void save(Map<String, Object> values) throws IOException {
        Files.createDirectories(file.getParent());
        YamlConfiguration yaml = new YamlConfiguration();
        values.forEach(yaml::set);
        Path temporary = file.resolveSibling("config.yml.tmp");
        yaml.save(temporary.toFile());
        try {
            Files.move(temporary, file, StandardCopyOption.REPLACE_EXISTING,
                    StandardCopyOption.ATOMIC_MOVE);
        } catch (AtomicMoveNotSupportedException ignored) {
            Files.move(temporary, file, StandardCopyOption.REPLACE_EXISTING);
        } finally {
            Files.deleteIfExists(temporary);
        }
    }

    private Map<String, Object> copySection(ConfigurationSection section) {
        Map<String, Object> result = new LinkedHashMap<>();
        for (String key : section.getKeys(false)) {
            Object value = section.get(key);
            if (value instanceof ConfigurationSection child) {
                result.put(key, copySection(child));
            } else if (value instanceof Map<?, ?> map) {
                result.put(key, copyMap(map));
            } else if (value instanceof java.util.List<?> list) {
                result.put(key, ListCopy.copy(list));
            } else {
                result.put(key, value);
            }
        }
        return result;
    }

    private Map<String, Object> copyMap(Map<?, ?> source) {
        Map<String, Object> result = new LinkedHashMap<>();
        source.forEach((key, value) -> {
            if (key instanceof String stringKey) result.put(stringKey, value);
        });
        return result;
    }

    private static final class ListCopy {
        private static java.util.List<Object> copy(java.util.List<?> source) {
            return new java.util.ArrayList<>(source);
        }
    }
}
