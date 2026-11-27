package cn.orzmc.template.paper.command;

import cn.orzmc.template.core.config.ConfigService;
import io.papermc.paper.command.brigadier.Commands;
import net.kyori.adventure.text.Component;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import org.bukkit.plugin.java.JavaPlugin;

/** Registers commands through the reload-aware Paper Lifecycle API. */
public final class TemplateCommandRegistrar {
    private final JavaPlugin plugin;
    private final ConfigService config;

    public TemplateCommandRegistrar(JavaPlugin plugin, ConfigService config) {
        this.plugin = plugin;
        this.config = config;
    }

    public void register() {
        plugin.getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS,
                event -> event.registrar().register(Commands.literal("template")
                        .executes(context -> {
                            if (!config.current().enabled()) {
                                context.getSource().getSender().sendMessage(
                                        Component.text("Plugin is disabled by configuration."));
                                return 0;
                            }
                            context.getSource().getSender().sendMessage(
                                    Component.text(config.current().message()));
                            return 1;
                        }).build()));
    }
}
