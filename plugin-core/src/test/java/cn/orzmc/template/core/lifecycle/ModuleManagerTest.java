package cn.orzmc.template.core.lifecycle;
import static org.junit.jupiter.api.Assertions.*;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
class ModuleManagerTest {
    @Test void startsInOrderAndStopsInReverseOrder() {
        List<String> calls = new ArrayList<>();
        ModuleManager manager = new ModuleManager(List.of(module("a", calls), module("b", calls)));
        manager.start(); manager.stop(); manager.stop();
        assertEquals(List.of("start:a", "start:b", "stop:b", "stop:a"), calls);
    }
    @Test void rollsBackIncludingPartiallyStartedModule() {
        List<String> calls = new ArrayList<>();
        PluginModule broken = new PluginModule() {
            public void start() { calls.add("start:x"); throw new IllegalStateException("boom"); }
            public void stop() { calls.add("stop:x"); }
        };
        ModuleManager manager = new ModuleManager(List.of(module("a", calls), broken));
        assertThrows(ModuleLifecycleException.class, manager::start);
        assertEquals(List.of("start:a", "start:x", "stop:x", "stop:a"), calls);
    }
    @Test void stopFailureDoesNotSkipEarlierModules() {
        List<String> calls = new ArrayList<>();
        PluginModule broken = new PluginModule() {
            public void start() { calls.add("start:x"); }
            public void stop() { calls.add("stop:x"); throw new IllegalStateException("boom"); }
        };
        ModuleManager manager = new ModuleManager(List.of(module("a", calls), broken));
        manager.start();
        assertThrows(ModuleLifecycleException.class, manager::stop);
        assertEquals(List.of("start:a", "start:x", "stop:x", "stop:a"), calls);
    }
    private static PluginModule module(String name, List<String> calls) {
        return new PluginModule() {
            public void start() { calls.add("start:" + name); }
            public void stop() { calls.add("stop:" + name); }
        };
    }
}
