package cn.orzmc.template.paper.event;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import cn.orzmc.template.core.lifecycle.ModuleContext;
import cn.orzmc.template.core.lifecycle.ModuleManager;
import cn.orzmc.template.core.lifecycle.PluginModule;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;
import org.mockbukkit.mockbukkit.plugin.PluginMock;

class PaperListenerRegistrarTest {
    private ServerMock server;
    private PluginMock plugin;

    @BeforeEach
    void setUp() {
        server = MockBukkit.mock();
        plugin = MockBukkit.createMockPlugin("TemplateListenerTest");
    }

    @AfterEach
    void tearDown() {
        MockBukkit.unmock();
    }

    @Test
    void listenerIsUnregisteredWhenOwningModuleStops() {
        AtomicInteger joinEvents = new AtomicInteger();
        AtomicReference<ModuleContext> moduleContext = new AtomicReference<>();
        Listener listener = new Listener() {
            @EventHandler
            public void onJoin(PlayerJoinEvent event) {
                joinEvents.incrementAndGet();
            }
        };

        PluginModule module = new PluginModule() {
            @Override
            public void start(ModuleContext context) {
                moduleContext.set(context);
            }

            @Override
            public void stop() {
                // Listener ownership is released by the module ResourceScope.
            }
        };
        ModuleManager modules = new ModuleManager(List.of(module));
        modules.start();

        assertNotNull(moduleContext.get());
        new PaperListenerRegistrar(plugin).register(listener, moduleContext.get());

        server.addPlayer("before-stop");
        assertEquals(1, joinEvents.get(), "listener should receive events while its module is active");

        modules.stop();

        server.addPlayer("after-stop");
        assertEquals(1, joinEvents.get(), "listener must not receive events after module shutdown");
    }
}
