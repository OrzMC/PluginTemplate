package cn.orzmc.template.core.config;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class TemplateSettingsCodec {
    private static final Set<String> KNOWN_KEYS =
            Set.of("schema-version", "enabled", "message");

    public ConfigLoadResult decode(Map<String, Object> values) {
        List<String> warnings = new ArrayList<>();
        for (String key : values.keySet()) {
            if (!KNOWN_KEYS.contains(key)) warnings.add("未知配置项: " + key);
        }

        Object enabledValue = values.getOrDefault("enabled", true);
        if (!(enabledValue instanceof Boolean enabled)) {
            throw new ConfigValidationException("enabled", "必须是 true 或 false");
        }

        Object messageValue = values.getOrDefault(
                "message", TemplateSettings.defaults().message());
        if (!(messageValue instanceof String message)) {
            throw new ConfigValidationException("message", "必须是字符串");
        }
        if (message.isBlank()) {
            throw new ConfigValidationException("message", "不能为空或仅包含空白字符");
        }
        if (message.length() > 256) {
            throw new ConfigValidationException("message", "长度不能超过 256 个字符");
        }
        return new ConfigLoadResult(new TemplateSettings(enabled, message), warnings);
    }
}
