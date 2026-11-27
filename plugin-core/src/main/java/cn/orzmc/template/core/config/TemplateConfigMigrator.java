package cn.orzmc.template.core.config;

import java.util.LinkedHashMap;
import java.util.Map;

/** Pure schema migrations; no filesystem or platform dependencies. */
public final class TemplateConfigMigrator {
    public static final int CURRENT_SCHEMA = 1;

    public Map<String, Object> migrate(Map<String, Object> source) {
        Map<String, Object> values = new LinkedHashMap<>(source);
        int version = schemaVersion(values);
        if (version > CURRENT_SCHEMA) {
            throw new ConfigValidationException("schema-version",
                    "配置版本 " + version + " 高于当前支持版本 " + CURRENT_SCHEMA);
        }
        if (version == 0) {
            Object oldMessage = values.remove("status-message");
            if (oldMessage != null && !values.containsKey("message")) {
                values.put("message", oldMessage);
            }
            values.put("schema-version", CURRENT_SCHEMA);
        }
        return Map.copyOf(values);
    }

    public int schemaVersion(Map<String, Object> values) {
        Object value = values.get("schema-version");
        if (value == null) return 0;
        if (!(value instanceof Number number)) {
            throw new ConfigValidationException("schema-version", "必须是整数");
        }
        int version = number.intValue();
        if (number.doubleValue() != version || version < 0) {
            throw new ConfigValidationException("schema-version", "必须是非负整数");
        }
        return version;
    }
}
