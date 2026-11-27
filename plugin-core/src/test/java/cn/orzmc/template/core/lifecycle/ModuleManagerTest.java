package cn.orzmc.template.core.lifecycle;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class ModuleManagerTest {
    @Test
    void startsInOrderAndStopsInReverseOrder() {
        List<String> calls = new ArrayList<>();
        ModuleManager manager = new ModuleManager(List.of(module("a", calls), module("b", calls)));

        manager.start();
        manager.stop();
        manager.stop();

        assertEquals(List.of("start:a", "start:b", "stop:b", "stop:a"), calls);
    }

    @Test
    void rollsBackIncludingPartiallyStartedModuleAndItsResources() {
        List<String> calls = new ArrayList<>();
        PluginModule broken = new PluginModule() {
            @Override
            public void start(ModuleContext context) {
                calls.add("start:x");
                context.resources().register(() -> calls.add("resource:x"));
                throw new IllegalStateException("boom");
            }

            @Override
            public void stop() {
                calls.add("stop:x");
            }
        };
        ModuleManager manager = new ModuleManager(List.of(module("a", calls), broken));

        assertThrows(ModuleLifecycleException.class, manager::start);

        assertEquals(
                List.of("start:a", "start:x", "stop:x", "resource:x", "stop:a"),
                calls);
    }

    @Test
    void stopFailureDoesNotSkipEarlierModulesOrResourceCleanup() {
        List<String> calls = new ArrayList<>();
        PluginModule broken = new PluginModule() {
            @Override
            public void start(ModuleContext context) {
                calls.add("start:x");
                context.resources().register(() -> calls.add("resource:x"));
            }

            @Override
            public void stop() {
                calls.add("stop:x");
                throw new IllegalStateException("boom");
            }
        };
        ModuleManager manager = new ModuleManager(List.of(module("a", calls), broken));

        manager.start();
        assertThrows(ModuleLifecycleException.class, manager::stop);

        assertEquals(
                List.of("start:a", "start:x", "stop:x", "resource:x", "stop:a"),
                calls);
    }

    @Test
    void resourceCleanupFailureDoesNotSkipEarlierModules() {
        List<String> calls = new ArrayList<>();
        PluginModule broken = new PluginModule() {
            @Override
            public void start(ModuleContext context) {
                calls.add("start:x");
                context.resources().register(() -> {
                    calls.add("resource:x");
                    throw new IllegalStateException("resource boom");
                });
            }

            @Override
            public void stop() {
                calls.add("stop:x");
            }
        };
        ModuleManager manager = new ModuleManager(List.of(module("a", calls), broken));

        manager.start();
        assertThrows(ModuleLifecycleException.class, manager::stop);

        assertEquals(
                List.of("start:a", "start:x", "stop:x", "resource:x", "stop:a"),
                calls);
    }

    private static PluginModule module(String name, List<String> calls) {
        return new PluginModule() {
            @Override
            public void start(ModuleContext context) {
                calls.add("start:" + name);
            }

            @Override
            public void stop() {
                calls.add("stop:" + name);
            }
        };
    }
}
