package cn.orzmc.template.core.scheduler;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/** Platform-neutral task state machine used by scheduler adapters. */
public final class ManagedTaskHandle implements TaskHandle {
    private final AtomicReference<TaskState> state = new AtomicReference<>(TaskState.SCHEDULED);
    private final AtomicReference<Runnable> cancellation = new AtomicReference<>();
    private final AtomicBoolean cancellationInvoked = new AtomicBoolean();

    public void attachCancellation(Runnable action) {
        cancellation.set(Objects.requireNonNull(action, "action"));
        if (state.get() == TaskState.CANCELLED) invokeCancellation();
    }

    public void run(Runnable action) {
        Objects.requireNonNull(action, "action");
        if (!state.compareAndSet(TaskState.SCHEDULED, TaskState.RUNNING)) return;
        try {
            action.run();
            state.compareAndSet(TaskState.RUNNING, TaskState.COMPLETED);
        } catch (RuntimeException | Error failure) {
            state.compareAndSet(TaskState.RUNNING, TaskState.FAILED);
            throw failure;
        }
    }

    @Override public boolean cancel() {
        if (!state.compareAndSet(TaskState.SCHEDULED, TaskState.CANCELLED)) return false;
        invokeCancellation();
        return true;
    }

    private void invokeCancellation() {
        Runnable action = cancellation.get();
        if (action != null && cancellationInvoked.compareAndSet(false, true)) action.run();
    }

    @Override public TaskState state() { return state.get(); }

    @Override public void close() { cancel(); }
}
