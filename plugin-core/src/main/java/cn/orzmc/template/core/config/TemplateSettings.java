package cn.orzmc.template.core.config;

/** Immutable, validated runtime settings. */
public record TemplateSettings(boolean enabled, String message) {
    public static TemplateSettings defaults() {
        return new TemplateSettings(true, "OrzMC PluginTemplate is running.");
    }
}
