package cn.orzmc.template.core.lifecycle;

import java.util.Objects;

/** Runtime context supplied to a module when it starts. */
public final class ModuleContext {
    private final ResourceScope resources;

    ModuleContext(ResourceScope resources) {
        this.resources = Objects.requireNonNull(resources, "resources");
    }

    /** Returns the resource scope owned by this module. */
    public ResourceScope resources() {
        return resources;
    }
}
