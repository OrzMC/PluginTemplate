package cn.orzmc.template.core.config;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

/** Coordinates migration, validation, persistence and atomic publication. */
public final class ConfigService {
    private final ConfigStore store;
    private final TemplateConfigMigrator migrator;
    private final TemplateSettingsCodec codec;
    private final AtomicReference<TemplateSettings> active = new AtomicReference<>();

    public ConfigService(ConfigStore store) {
        this(store, new TemplateConfigMigrator(), new TemplateSettingsCodec());
    }

    ConfigService(ConfigStore store, TemplateConfigMigrator migrator, TemplateSettingsCodec codec) {
        this.store = Objects.requireNonNull(store);
        this.migrator = Objects.requireNonNull(migrator);
        this.codec = Objects.requireNonNull(codec);
    }

    public synchronized ConfigLoadResult load() throws IOException {
        if (active.get() != null) return reload();
        return readAndPublish();
    }

    public synchronized ConfigLoadResult reload() throws IOException {
        if (active.get() == null) return load();
        return readAndPublish();
    }

    public TemplateSettings current() {
        TemplateSettings settings = active.get();
        if (settings == null) throw new IllegalStateException("配置尚未加载");
        return settings;
    }

    private ConfigLoadResult readAndPublish() throws IOException {
        Map<String, Object> original;
        boolean existed = store.exists();
        if (existed) {
            original = new LinkedHashMap<>(store.load());
        } else {
            TemplateSettings defaults = TemplateSettings.defaults();
            original = Map.of(
                    "schema-version", TemplateConfigMigrator.CURRENT_SCHEMA,
                    "enabled", defaults.enabled(),
                    "message", defaults.message());
        }

        Map<String, Object> migrated = migrator.migrate(original);
        ConfigLoadResult candidate = codec.decode(migrated);

        if (!migrated.equals(original)) {
            if (existed) store.backup();
            store.save(migrated);
        } else if (!existed) {
            store.save(migrated);
        }

        // Publish only after every required persistence operation succeeds.
        active.set(candidate.settings());
        return candidate;
    }
}
