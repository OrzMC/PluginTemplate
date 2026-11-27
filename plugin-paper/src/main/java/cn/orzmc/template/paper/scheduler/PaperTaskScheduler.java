package cn.orzmc.template.paper.scheduler;

import cn.orzmc.template.core.lifecycle.ModuleContext;
import cn.orzmc.template.core.scheduler.ManagedTaskHandle;
import cn.orzmc.template.core.scheduler.TaskHandle;
import java.util.Objects;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

/**
 * Paper's non-regionized scheduler adapter.
 *
 * <p>Main-thread tasks may access normal Paper server state. Async tasks are for blocking I/O
 * and pure computation only; they must not access Bukkit world, entity, inventory or other
 * server-owned state. These methods do not imply Folia compatibility.</p>
 */
public final class PaperTaskScheduler {
    private final JavaPlugin plugin;

    public PaperTaskScheduler(JavaPlugin plugin) {
        this.plugin = Objects.requireNonNull(plugin, "plugin");
    }

    /** Schedules one task on Paper's main server thread. */
    public TaskHandle executeMain(ModuleContext owner, Runnable action) {
        return schedule(owner, action, false);
    }

    /** Schedules one task asynchronously; the callback must not touch Bukkit/Paper state. */
    public TaskHandle executeAsync(ModuleContext owner, Runnable action) {
        return schedule(owner, action, true);
    }

    private TaskHandle schedule(ModuleContext owner, Runnable action, boolean async) {
        Objects.requireNonNull(owner, "owner");
        Objects.requireNonNull(action, "action");
        ManagedTaskHandle handle = new ManagedTaskHandle();
        Runnable guarded = () -> handle.run(action);
        BukkitTask task = async
                ? plugin.getServer().getScheduler().runTaskAsynchronously(plugin, guarded)
                : plugin.getServer().getScheduler().runTask(plugin, guarded);
        handle.attachCancellation(task::cancel);
        return owner.resources().register(handle);
    }
}
