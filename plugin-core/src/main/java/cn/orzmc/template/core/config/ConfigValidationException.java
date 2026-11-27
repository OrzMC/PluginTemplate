package cn.orzmc.template.core.config;

/** A configuration error with a stable key path suitable for operator diagnostics. */
public final class ConfigValidationException extends RuntimeException {
    private final String path;

    public ConfigValidationException(String path, String message) {
        super(path + ": " + message);
        this.path = path;
    }

    public String path() { return path; }
}
