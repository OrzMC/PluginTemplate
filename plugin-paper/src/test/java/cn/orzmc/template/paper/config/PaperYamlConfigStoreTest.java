package cn.orzmc.template.paper.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import org.bukkit.plugin.java.JavaPlugin;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;

class PaperYamlConfigStoreTest {
    private JavaPlugin plugin;
    private Path dataFolder;
    private PaperYamlConfigStore store;

    @BeforeEach
    void setUp() throws Exception {
        MockBukkit.mock();
        plugin = MockBukkit.createMockPlugin("TemplateYamlConfigTest");
        dataFolder = plugin.getDataFolder().toPath();
        Files.createDirectories(dataFolder);
        store = new PaperYamlConfigStore(plugin);
    }

    @AfterEach
    void tearDown() {
        MockBukkit.unmock();
    }

    @Test
    void savesAndLoadsYamlValuesIncludingNestedMapsAndLists() throws Exception {
        Map<String, Object> values = new LinkedHashMap<>();
        values.put("schema-version", 1);
        values.put("enabled", true);
        values.put("message", "hello from yaml");
        values.put("nested", Map.of("mode", "safe", "count", 3));
        values.put("labels", List.of("one", "two"));

        store.save(values);

        Map<String, Object> loaded = store.load();
        assertEquals(1, loaded.get("schema-version"));
        assertEquals(true, loaded.get("enabled"));
        assertEquals("hello from yaml", loaded.get("message"));
        assertEquals(Map.of("mode", "safe", "count", 3), loaded.get("nested"));
        assertEquals(List.of("one", "two"), loaded.get("labels"));
        assertFalse(Files.exists(dataFolder.resolve("config.yml.tmp")),
                "temporary file must be removed after atomic replacement");
    }

    @Test
    void createsTimestampedBackupOfExistingConfiguration() throws Exception {
        store.save(Map.of("schema-version", 1, "enabled", true, "message", "before backup"));

        store.backup();

        try (Stream<Path> files = Files.list(dataFolder)) {
            Path backup = files
                    .filter(path -> path.getFileName().toString().startsWith("config.yml.bak-"))
                    .findFirst()
                    .orElse(null);
            assertNotNull(backup, "backup file should be created");
            String content = Files.readString(backup);
            assertTrue(content.contains("before backup"));
        }
    }

    @Test
    void replacesExistingConfigurationWithoutLeavingTemporaryFile() throws Exception {
        store.save(Map.of("schema-version", 1, "enabled", true, "message", "old"));
        store.save(Map.of("schema-version", 1, "enabled", false, "message", "new"));

        Map<String, Object> loaded = store.load();
        assertEquals(false, loaded.get("enabled"));
        assertEquals("new", loaded.get("message"));
        assertFalse(Files.exists(dataFolder.resolve("config.yml.tmp")));
    }
}
