package cn.orzmc.template.core.lifecycle;

/** Reports one or more failures while closing lifecycle-owned resources. */
public final class ResourceCleanupException extends RuntimeException {
    public ResourceCleanupException(String message, Throwable cause) {
        super(message, cause);
    }
}
