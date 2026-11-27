package cn.orzmc.template.paper.command;

import cn.orzmc.template.core.config.ConfigLoadResult;
import cn.orzmc.template.core.config.ConfigService;
import io.papermc.paper.command.brigadier.Commands;
import java.io.IOException;
import java.util.logging.Level;
import net.kyori.adventure.text.Component;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import org.bukkit.command.CommandSender;
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
                            CommandSender sender = context.getSource().getSender();
                            if (!config.current().enabled()) {
                                sender.sendMessage(Component.text("Plugin is disabled by configuration."));
                                return 0;
                            }
                            sender.sendMessage(Component.text(config.current().message()));
                            return 1;
                        })
                        .then(Commands.literal("reload")
                                .requires(source -> source.getSender().hasPermission(PermissionNodes.CONFIG_RELOAD))
                                .executes(context -> reload(context.getSource().getSender())))
                        .build()));
    }

    private int reload(CommandSender sender) {
        try {
            ConfigLoadResult result = config.reload();
            result.warnings().forEach(plugin.getLogger()::warning);
            sender.sendMessage(Component.text("Configuration reloaded."));
            return 1;
        } catch (IOException | RuntimeException exception) {
            plugin.getLogger().log(Level.WARNING, "Configuration reload rejected; active settings unchanged.", exception);
            sender.sendMessage(Component.text("Configuration reload failed; previous settings remain active."));
            return 0;
        }
    }
}
