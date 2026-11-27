package cn.orzmc.template.core.config;

import java.util.List;

public record ConfigLoadResult(TemplateSettings settings, List<String> warnings) {
    public ConfigLoadResult {
        warnings = List.copyOf(warnings);
    }
}
