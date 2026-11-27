package cn.orzmc.template.paper;
import cn.orzmc.template.core.lifecycle.ModuleManager;
import cn.orzmc.template.paper.command.TemplateCommandRegistrar;
import cn.orzmc.template.paper.module.LoggingModule;
import java.util.List;
import java.util.logging.Level;
import org.bukkit.plugin.java.JavaPlugin;
public final class TemplatePlugin extends JavaPlugin {
    private ModuleManager modules;
    @Override public void onEnable() {
        modules = new ModuleManager(List.of(new LoggingModule(getLogger())));
        modules.start();
        new TemplateCommandRegistrar(this).register();
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
