package cn.orzmc.template.paper.module;
import cn.orzmc.template.core.lifecycle.PluginModule;
import java.util.logging.Logger;
public final class LoggingModule implements PluginModule {
    private final Logger logger;
    public LoggingModule(Logger logger) { this.logger = logger; }
    @Override public void start() { logger.info("Template module started."); }
    @Override public void stop() { logger.info("Template module stopped."); }
}
