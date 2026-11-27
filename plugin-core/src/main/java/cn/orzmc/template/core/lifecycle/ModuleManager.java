package cn.orzmc.template.core.lifecycle;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/** Starts modules in declaration order and stops them in reverse order. */
public final class ModuleManager {
    private enum State { NEW, STARTED, STOPPED }

    private record StartedModule(PluginModule module, ModuleContext context) {}

    private final List<PluginModule> modules;
    private final List<StartedModule> started = new ArrayList<>();
    private State state = State.NEW;

    public ModuleManager(List<? extends PluginModule> modules) {
        Objects.requireNonNull(modules, "modules");
        this.modules = List.copyOf(modules);
        this.modules.forEach(module -> Objects.requireNonNull(module, "module"));
    }

    public synchronized void start() {
        if (state != State.NEW) {
            throw new IllegalStateException("Cannot start manager in state " + state);
        }
        try {
            for (PluginModule module : modules) {
                ModuleContext context = new ModuleContext(new ResourceScope());
                // Include the module before start so partial initialization can be rolled back.
                started.add(new StartedModule(module, context));
                module.start(context);
            }
            state = State.STARTED;
        } catch (RuntimeException | Error cause) {
            Throwable failure = stopStarted(cause);
            state = State.STOPPED;
            throw new ModuleLifecycleException("Module startup failed; rollback was attempted", failure);
        }
    }

    public synchronized void stop() {
        if (state == State.STOPPED) return;
        Throwable failure = stopStarted(null);
        state = State.STOPPED;
        if (failure != null) {
            throw new ModuleLifecycleException("One or more modules failed to stop", failure);
        }
    }

    private Throwable stopStarted(Throwable primary) {
        Throwable failure = primary;
        for (int i = started.size() - 1; i >= 0; i--) {
            StartedModule item = started.get(i);
            try {
                item.module().stop();
            } catch (RuntimeException | Error stopFailure) {
                failure = append(failure, stopFailure);
            }
            try {
                item.context().resources().close();
            } catch (RuntimeException | Error resourceFailure) {
                failure = append(failure, resourceFailure);
            }
        }
        started.clear();
        return failure;
    }

    private static Throwable append(Throwable primary, Throwable next) {
        if (primary == null) return next;
        primary.addSuppressed(next);
        return primary;
    }
}
