package io.github.the28awg.helper.task.app;

import android.database.Observable;
import android.os.Handler;
import android.os.Looper;

import java.util.ArrayList;

import io.github.the28awg.helper.task.Task;
import io.github.the28awg.helper.task.TaskContext;
import io.github.the28awg.helper.task.TaskWorker;

/**
 * Created by the28awg on 26.03.16.
 */
public class TaskHelper {

    private final TaskWorkerObservable taskWorkerObservable = new TaskWorkerObservable();
    private ArrayList<Task> tasks = new ArrayList<>();
    private final Object lock = new Object();
    private TaskHelper() {
        taskWorkerObservable.registerObserver(new TaskWorkerListener() {
            @Override
            public void execute(Task task) {
                synchronized (lock) {
                    tasks.add(task);
                }
            }

            @Override
            public void update(Task task, Object progress) {

            }

            @Override
            public void successful(Task task, Object result) {
                synchronized (lock) {
                    tasks.remove(task);
                }
            }

            @Override
            public void failure(Task task, Exception e) {
                synchronized (lock) {
                    tasks.remove(task);
                }
            }
        });
    }

    public <Result, Update> void task(final Task<Result, Update> task) {
        TaskWorker.worker().task(new TaskWrapper<>(task));
    }

    public TaskWorkerObservable observable() {
        return taskWorkerObservable;
    }

    public ArrayList<Task> tasks() {
        synchronized (lock) {
            return tasks;
        }
    }

    public static TaskHelper helper() {
        return Helper.TASK_HELPER;
    }
    private static class Helper {
        static final TaskHelper TASK_HELPER = new TaskHelper();
    }

    private static class TaskWrapper<R, U> implements Task<R, U> {
        private Task<R, U> task;
        private TaskWrapper(Task<R, U> task) {
            this.task = task;
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    TaskHelper.helper().observable().execute(TaskWrapper.this.task);
                }
            });
        }
        @Override
        public R task(TaskContext<U> context) {
            return task.task(context);
        }

        @Override
        public void update(U progress) {
            task.update(progress);
            TaskHelper.helper().observable().update(this.task, progress);
        }

        @Override
        public void successful(R result) {
            task.successful(result);
            TaskHelper.helper().observable().successful(this.task, result);
        }

        @Override
        public void failure(Exception e) {
            task.failure(e);
            TaskHelper.helper().observable().failure(this.task, e);
        }
    }

    public interface TaskWorkerListener {
        void execute(Task task);
        void update(Task task, Object progress);
        void successful(Task task, Object result);
        void failure(Task task, Exception e);
    }

    public static class TaskWorkerObservable extends Observable<TaskWorkerListener> {

        private TaskWorkerObservable() {

        }
        private void update(Task task, Object progress) {
            synchronized(mObservers) {
//                for (int i = mObservers.size() - 1; i >= 0; i--) {
//                    mObservers.get(i).update(task, progress);
//                }
                for (int i = 0; i < mObservers.size(); i++) {
                    mObservers.get(i).update(task, progress);
                }
            }
        }

        private void successful(Task task, Object result) {
            synchronized(mObservers) {
//                for (int i = mObservers.size() - 1; i >= 0; i--) {
//                    mObservers.get(i).successful(task, result);
//                }
                for (int i = 0; i < mObservers.size(); i++) {
                    mObservers.get(i).successful(task, result);
                }
            }
        }

        private void failure(Task task, Exception e) {
            synchronized(mObservers) {
//                for (int i = mObservers.size() - 1; i >= 0; i--) {
//                    mObservers.get(i).failure(task, e);
//                }
                for (int i = 0; i < mObservers.size(); i++) {
                    mObservers.get(i).failure(task, e);
                }
            }
        }

        private void execute(Task task) {
            synchronized(mObservers) {
//                for (int i = mObservers.size() - 1; i >= 0; i--) {
//                    mObservers.get(i).execute(task);
//                }
                for (int i = 0; i < mObservers.size(); i++) {
                    mObservers.get(i).execute(task);
                }
            }
        }

    }
}
