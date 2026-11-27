package cn.orzmc.template.core.lifecycle;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class ResourceScopeTest {
    @Test
    void closesInReverseOrderAndIsIdempotent() {
        List<String> calls = new ArrayList<>();
        ResourceScope scope = new ResourceScope();
        scope.register(() -> calls.add("a"));
        scope.register(() -> calls.add("b"));

        scope.close();
        scope.close();

        assertEquals(List.of("b", "a"), calls);
        assertTrue(scope.isClosed());
    }

    @Test
    void continuesClosingAfterAResourceFails() {
        List<String> calls = new ArrayList<>();
        ResourceScope scope = new ResourceScope();
        scope.register(() -> calls.add("a"));
        scope.register(() -> {
            calls.add("b");
            throw new IllegalStateException("broken b");
        });
        scope.register(() -> calls.add("c"));

        ResourceCleanupException failure = assertThrows(ResourceCleanupException.class, scope::close);

        assertEquals(List.of("c", "b", "a"), calls);
        assertEquals("broken b", failure.getCause().getMessage());
    }

    @Test
    void closesAndRejectsAResourceRegisteredAfterClose() {
        List<String> calls = new ArrayList<>();
        ResourceScope scope = new ResourceScope();
        scope.close();

        assertThrows(IllegalStateException.class, () -> scope.register(() -> calls.add("late")));

        assertEquals(List.of("late"), calls);
    }

    @Test
    void suppressesLateCleanupFailureOnRegistrationError() {
        ResourceScope scope = new ResourceScope();
        scope.close();

        IllegalStateException failure = assertThrows(
                IllegalStateException.class,
                () -> scope.register(() -> { throw new IllegalStateException("cleanup"); }));

        assertEquals(1, failure.getSuppressed().length);
        assertEquals("cleanup", failure.getSuppressed()[0].getMessage());
    }
}
