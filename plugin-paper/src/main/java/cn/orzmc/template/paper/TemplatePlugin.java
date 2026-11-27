package cn.orzmc.template.paper;

import cn.orzmc.template.core.config.ConfigLoadResult;
import cn.orzmc.template.core.config.ConfigService;
import cn.orzmc.template.core.lifecycle.ModuleManager;
import cn.orzmc.template.paper.command.TemplateCommandRegistrar;
import cn.orzmc.template.paper.config.PaperYamlConfigStore;
import cn.orzmc.template.paper.module.LoggingModule;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import org.bukkit.plugin.java.JavaPlugin;

public final class TemplatePlugin extends JavaPlugin {
    private ModuleManager modules;
    private ConfigService configService;

    @Override public void onEnable() {
        try {
            configService = new ConfigService(new PaperYamlConfigStore(this));
            ConfigLoadResult loaded = configService.load();
            loaded.warnings().forEach(warning -> getLogger().warning(warning));
        } catch (IOException | RuntimeException exception) {
            getLogger().log(Level.SEVERE, "Unable to load plugin configuration; disabling plugin.", exception);
            throw new IllegalStateException("Plugin configuration could not be loaded.", exception);
        }

        modules = new ModuleManager(List.of(new LoggingModule(getLogger())));
        modules.start();
        new TemplateCommandRegistrar(this, configService).register();
        getLogger().info("Plugin enabled.");
    }

    @Override public void onDisable() {
        if (modules == null) return;
        try { modules.stop(); }
        catch (RuntimeException exception) {
            getLogger().log(Level.SEVERE, "Plugin shutdown completed with module errors.", exception);
        }
    }
}
