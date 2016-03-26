package io.github.the28awg.helper.task;

/**
 * Created by the28awg on 26.03.16.
 */
public interface Task<R, U> {
    // Background thread
    R task(TaskContext<U> context);
    // UI thread
    void update(U progress);
    // UI thread
    void successful(R result);
    // UI thread
    void failure(Exception e);
}
