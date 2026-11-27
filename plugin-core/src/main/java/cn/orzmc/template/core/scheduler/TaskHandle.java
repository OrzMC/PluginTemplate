package cn.orzmc.template.core.scheduler;

import cn.orzmc.template.core.lifecycle.ResourceHandle;

/** Cancellable, observable handle for one scheduled task. */
public interface TaskHandle extends ResourceHandle {
    boolean cancel();
    TaskState state();
}
