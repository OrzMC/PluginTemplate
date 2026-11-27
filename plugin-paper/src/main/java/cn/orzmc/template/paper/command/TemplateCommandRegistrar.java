package cn.orzmc.template.paper.command;
import io.papermc.paper.command.brigadier.Commands;
import net.kyori.adventure.text.Component;
import org.bukkit.event.lifecycle.LifecycleEvents;
import org.bukkit.plugin.java.JavaPlugin;
/** Registers commands through the reload-aware Paper Lifecycle API. */
public final class TemplateCommandRegistrar {
    private final JavaPlugin plugin;
    public TemplateCommandRegistrar(JavaPlugin plugin) { this.plugin = plugin; }
    public void register() {
        plugin.getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS,
                event -> event.registrar().register(Commands.literal("template")
                        .executes(context -> {
                            context.getSource().getSender().sendMessage(
                                    Component.text("OrzMC PluginTemplate is running."));
                            return 1;
                        }).build()));
    }
}
