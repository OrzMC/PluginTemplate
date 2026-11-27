package cn.orzmc.template.core.config;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;

class ConfigServiceTest {
    @Test void initializesDefaultsAndPersistsSchema() throws Exception {
        MemoryStore store = new MemoryStore();
        ConfigService service = new ConfigService(store);
        ConfigLoadResult result = service.load();
        assertEquals(TemplateSettings.defaults(), result.settings());
        assertEquals(1, store.values.get("schema-version"));
        assertEquals(result.settings(), service.current());
    }

    @Test void migratesLegacyMessageAndCreatesBackup() throws Exception {
        MemoryStore store = new MemoryStore();
        store.values = new LinkedHashMap<>(Map.of("status-message", "legacy", "enabled", true));
        ConfigService service = new ConfigService(store);
        assertEquals("legacy", service.load().settings().message());
        assertTrue(store.backedUp);
        assertEquals(1, store.values.get("schema-version"));
        assertFalse(store.values.containsKey("status-message"));
    }

    @Test void rejectsFutureSchema() {
        MemoryStore store = new MemoryStore();
        store.values = Map.of("schema-version", 99, "enabled", true, "message", "x");
        assertThrows(ConfigValidationException.class, () -> new ConfigService(store).load());
    }

    @Test void invalidReloadKeepsPreviouslyPublishedSettings() throws Exception {
        MemoryStore store = new MemoryStore();
        ConfigService service = new ConfigService(store);
        TemplateSettings original = service.load().settings();
        store.values = Map.of("schema-version", 1, "enabled", "yes", "message", "bad");
        assertThrows(ConfigValidationException.class, service::reload);
        assertEquals(original, service.current());
    }

    @Test void failedPersistenceDoesNotPublishCandidate() throws Exception {
        MemoryStore store = new MemoryStore();
        ConfigService service = new ConfigService(store);
        TemplateSettings original = service.load().settings();
        store.values = Map.of("status-message", "migrated");
        store.failSave = true;
        assertThrows(IOException.class, service::reload);
        assertEquals(original, service.current());
    }

    @Test void warnsOnUnknownKeysAndRejectsInvalidMessage() throws Exception {
        MemoryStore store = new MemoryStore();
        store.values = Map.of("schema-version", 1, "enabled", true,
                "message", "hello", "future-option", 7);
        assertTrue(new ConfigService(store).load().warnings().getFirst().contains("future-option"));
        store.values = Map.of("schema-version", 1, "enabled", true, "message", " ");
        assertThrows(ConfigValidationException.class, () -> new ConfigService(store).load());
    }

    private static final class MemoryStore implements ConfigStore {
        Map<String, Object> values = Map.of();
        boolean backedUp;
        boolean failSave;
        @Override public boolean exists() { return !values.isEmpty(); }
        @Override public Map<String, Object> load() { return new LinkedHashMap<>(values); }
        @Override public void backup() { backedUp = true; }
        @Override public void save(Map<String, Object> values) throws IOException {
            if (failSave) throw new IOException("simulated write failure");
            this.values = new LinkedHashMap<>(values);
        }
    }
}
