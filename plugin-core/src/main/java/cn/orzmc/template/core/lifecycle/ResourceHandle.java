package cn.orzmc.template.core.lifecycle;

/** A resource that can be released without checked exceptions. */
@FunctionalInterface
public interface ResourceHandle extends AutoCloseable {
    @Override
    void close();
}
