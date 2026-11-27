package cn.orzmc.template.core.lifecycle;
/** A lifecycle-owned unit of plugin functionality. */
public interface PluginModule {
    /** Acquire resources. If this throws, stop() is still called during rollback. */
    void start();
    /** Release resources. Must tolerate partially completed startup. */
    void stop();
}
