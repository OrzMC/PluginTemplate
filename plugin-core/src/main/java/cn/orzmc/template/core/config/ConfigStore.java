package cn.orzmc.template.core.config;

import java.io.IOException;
import java.util.Map;

/** Platform-neutral persistence boundary. Values are YAML-compatible primitives, lists and maps. */
public interface ConfigStore {
    boolean exists() throws IOException;
    Map<String, Object> load() throws IOException;
    void backup() throws IOException;
    void save(Map<String, Object> values) throws IOException;
}
