package cn.orzmc.template.core.scheduler;

import static org.junit.jupiter.api.Assertions.assertEquals;

import cn.orzmc.template.core.lifecycle.ResourceScope;
import org.junit.jupiter.api.Test;

class ManagedTaskScopeTest {
    @Test void moduleScopeCancelsOutstandingTask() {
        ResourceScope scope = new ResourceScope();
        ManagedTaskHandle task = scope.register(new ManagedTaskHandle());
        scope.close();
        assertEquals(TaskState.CANCELLED, task.state());
    }
}
