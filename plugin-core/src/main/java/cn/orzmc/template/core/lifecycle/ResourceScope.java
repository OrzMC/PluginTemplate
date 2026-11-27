package cn.orzmc.template.core.lifecycle;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Owns resources acquired by one module and closes them in reverse registration order.
 *
 * <p>Registration and close are synchronized so a resource racing with shutdown is either
 * included in the close snapshot or immediately closed and rejected.</p>
 */
public final class ResourceScope implements AutoCloseable {
    private final List<ResourceHandle> resources = new ArrayList<>();
    private boolean closed;

    /**
     * Registers a resource with this scope and returns it for convenient inline ownership.
     *
     * <p>If this scope is already closed, the supplied resource is closed immediately and
     * registration fails. This prevents a late acquisition from silently leaking.</p>
     */
    public synchronized <T extends ResourceHandle> T register(T resource) {
        Objects.requireNonNull(resource, "resource");
        if (closed) {
            IllegalStateException failure =
                    new IllegalStateException("Cannot register a resource in a closed scope");
            try {
                resource.close();
            } catch (RuntimeException | Error closeFailure) {
                failure.addSuppressed(closeFailure);
            }
            throw failure;
        }
        resources.add(resource);
        return resource;
    }

    public synchronized boolean isClosed() {
        return closed;
    }

    @Override
    public void close() {
        List<ResourceHandle> toClose;
        synchronized (this) {
            if (closed) return;
            closed = true;
            toClose = new ArrayList<>(resources);
            resources.clear();
        }

        Throwable failure = null;
        for (int i = toClose.size() - 1; i >= 0; i--) {
            try {
                toClose.get(i).close();
            } catch (RuntimeException | Error closeFailure) {
                if (failure == null) {
                    failure = closeFailure;
                } else {
                    failure.addSuppressed(closeFailure);
                }
            }
        }

        if (failure != null) {
            throw new ResourceCleanupException("One or more owned resources failed to close", failure);
        }
    }
}
