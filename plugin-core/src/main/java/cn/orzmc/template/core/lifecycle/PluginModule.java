package cn.orzmc.template.core.lifecycle;

/** A lifecycle-owned unit of plugin functionality. */
public interface PluginModule {
    /**
     * Acquire resources.
     *
     * <p>Register every acquired resource with {@code context.resources()}. If this method
     * throws, {@link #stop()} and resource-scope cleanup are still attempted during rollback.</p>
     */
    void start(ModuleContext context);

    /**
     * Release module-specific state. Must tolerate partially completed startup.
     *
     * <p>The manager closes the module's resource scope after this method returns or throws.</p>
     */
    void stop();
}
