package cn.orzmc.template.paper.event;

import cn.orzmc.template.core.lifecycle.ModuleContext;
import cn.orzmc.template.core.lifecycle.ResourceHandle;
import java.util.Objects;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

/** Registers Paper listeners and binds their exact unregistration to the owning module. */
public final class PaperListenerRegistrar {
    private final JavaPlugin plugin;

    public PaperListenerRegistrar(JavaPlugin plugin) {
        this.plugin = Objects.requireNonNull(plugin, "plugin");
    }

    public ResourceHandle register(Listener listener, ModuleContext context) {
        Objects.requireNonNull(listener, "listener");
        Objects.requireNonNull(context, "context");
        plugin.getServer().getPluginManager().registerEvents(listener, plugin);
        return context.resources().register(() -> HandlerList.unregisterAll(listener));
    }
}
