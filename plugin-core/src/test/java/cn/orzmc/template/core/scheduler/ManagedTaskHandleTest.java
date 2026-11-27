package cn.orzmc.template.core.scheduler;

import static org.junit.jupiter.api.Assertions.*;

import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;

class ManagedTaskHandleTest {
    @Test void transitionsThroughRunToCompleted() {
        ManagedTaskHandle handle = new ManagedTaskHandle();
        AtomicInteger calls = new AtomicInteger();
        handle.run(calls::incrementAndGet);
        assertEquals(1, calls.get());
        assertEquals(TaskState.COMPLETED, handle.state());
        assertFalse(handle.cancel());
    }

    @Test void cancellationIsIdempotentAndInvokesAdapterOnce() {
        ManagedTaskHandle handle = new ManagedTaskHandle();
        AtomicInteger cancellations = new AtomicInteger();
        handle.attachCancellation(cancellations::incrementAndGet);
        assertTrue(handle.cancel());
        assertFalse(handle.cancel());
        assertEquals(1, cancellations.get());
        assertEquals(TaskState.CANCELLED, handle.state());
    }

    @Test void cancellationBeforeAdapterHandleAttachmentIsAppliedLater() {
        ManagedTaskHandle handle = new ManagedTaskHandle();
        AtomicInteger cancellations = new AtomicInteger();
        assertTrue(handle.cancel());
        handle.attachCancellation(cancellations::incrementAndGet);
        assertEquals(1, cancellations.get());
    }

    @Test void failedActionTransitionsToFailedAndPropagates() {
        ManagedTaskHandle handle = new ManagedTaskHandle();
        IllegalStateException failure = new IllegalStateException("boom");
        assertSame(failure, assertThrows(IllegalStateException.class, () -> handle.run(() -> {
            throw failure;
        })));
        assertEquals(TaskState.FAILED, handle.state());
    }

    @Test void closeCancelsPendingTaskButDoesNotInterruptRunningOrCompletedTask() {
        ManagedTaskHandle pending = new ManagedTaskHandle();
        pending.close();
        assertEquals(TaskState.CANCELLED, pending.state());

        ManagedTaskHandle completed = new ManagedTaskHandle();
        completed.run(() -> {});
        completed.close();
        assertEquals(TaskState.COMPLETED, completed.state());
    }
}
