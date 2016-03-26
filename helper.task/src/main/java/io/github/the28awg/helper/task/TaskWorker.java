package io.github.the28awg.helper.task;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

/**
 * Created by the28awg on 26.03.16.
 */
public class TaskWorker {
    public static boolean DEBUG = false;
    private static final String TAG = "TaskWorker";
    private static final String BACKGROUND_THREAD_NAME = "BackgroundTaskHandler";
    private static final int TASK_FINISH = 0;
    private static final int TASK_EXECUTE = 1;
    private static final int TASK_SUCCESSFUL = 2;
    private static final int TASK_FAILURE = 3;
    private static final int TASK_UPDATE = 4;

    private Handler mResultHandler;
    private final Object lock = new Object();

    private TaskWorker() {
        mResultHandler = new ResultsHandler();
    }

    public <T, U> void task(Task<T, U> task) {
        synchronized (lock) {
            final Wrapper<T, U> wrapper = new Wrapper<>();
            HandlerThread thread = new HandlerThread(BACKGROUND_THREAD_NAME + "[task=" + task + "]", android.os.Process.THREAD_PRIORITY_BACKGROUND);
            thread.start();
            wrapper.handler = new BackgroundHandler(thread.getLooper());
            wrapper.task = task;
            wrapper.context = new TaskContext<U>() {
                @Override
                public void update(U update) {
                    wrapper.update = update;
                    mResultHandler.obtainMessage(TASK_UPDATE, wrapper).sendToTarget();
                }
            };
            //wrapper.handler.removeMessages(TASK_FINISH);
            wrapper.handler.obtainMessage(TASK_EXECUTE, wrapper).sendToTarget();
        }
    }

    public static TaskWorker worker() {
        return Helper.WORKER;
    }

    private static void debugWrapper(int task_stage, Wrapper wrapper) {
        if (DEBUG) {
            String name = Thread.currentThread().getName();
            String stage = "";
            switch (task_stage) {
                case TASK_EXECUTE: stage = "TASK_EXECUTE"; break;
                case TASK_FAILURE: stage = "TASK_FAILURE"; break;
                case TASK_SUCCESSFUL: stage = "TASK_SUCCESSFUL"; break;
                case TASK_UPDATE: stage = "TASK_UPDATE"; break;
                case TASK_FINISH: stage = "TASK_FINISH"; break;
            }
            Log.d(TAG, String.format("%s (%s): wrapper.task: %s", stage, name, wrapper.task));
            Log.d(TAG, String.format("%s (%s): wrapper.result: %s", stage, name, wrapper.result));
            Log.d(TAG, String.format("%s (%s): wrapper.update: %s", stage, name, wrapper.update));
            Log.d(TAG, String.format("%s (%s): wrapper.exception: %s", stage, name, wrapper.exception));
            Log.d(TAG, String.format("%s (%s): wrapper.context: %s", stage, name, wrapper.context));
            Log.d(TAG, String.format("%s (%s): wrapper.handler: %s", stage, name, wrapper.handler));
        }
    }

    private static class Helper {
        static final TaskWorker WORKER = new TaskWorker();
    }


    private class BackgroundHandler extends Handler {

        public BackgroundHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case TASK_FINISH:
                    synchronized (lock) {
                        Wrapper wrapper = (Wrapper) msg.obj;
                        debugWrapper(TASK_FINISH, wrapper);
                        if (wrapper.handler != null) {
                            wrapper.handler.getLooper().quit();
                            wrapper.handler = null;
                        }
                        if (DEBUG) {
                            Log.d(TAG, String.format("TASK_FINISH (%s): erase wrapper: handler", Thread.currentThread().getName()));
                        }
                    }
                    break;
                case TASK_EXECUTE:
                    Wrapper wrapper = (Wrapper) msg.obj;
                    try {
                        debugWrapper(TASK_EXECUTE, wrapper);
                        wrapper.result = wrapper.task.task(wrapper.context);
                        mResultHandler.obtainMessage(TASK_SUCCESSFUL, wrapper).sendToTarget();
                    } catch (Exception e) {
                        e.printStackTrace();
                        wrapper.exception = e;
                        mResultHandler.obtainMessage(TASK_FAILURE, wrapper).sendToTarget();
                    }
                    synchronized (lock) {
                        if (wrapper.handler != null) {
                            Message finishMessage = wrapper.handler.obtainMessage(TASK_FINISH, wrapper);
                            wrapper.handler.sendMessageDelayed(finishMessage, 3000);
                        }
                    }
                    break;
            }
        }
    }

    private class ResultsHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            Wrapper wrapper = (Wrapper) msg.obj;
            switch (msg.what) {
                case TASK_UPDATE:
                    debugWrapper(TASK_UPDATE, wrapper);
                    wrapper.task.update(wrapper.update);
                    wrapper.update = null;
                    if (DEBUG) {
                        Log.d(TAG, String.format("TASK_UPDATE (%s): erase wrapper: update", Thread.currentThread().getName()));
                    }
                    break;
                case TASK_FAILURE:
                    debugWrapper(TASK_FAILURE, wrapper);
                    wrapper.task.failure(wrapper.exception);
                    wrapper.exception = null;
                    wrapper.task = null;
                    if (DEBUG) {
                        Log.d(TAG, String.format("TASK_FAILURE (%s): erase wrapper: exception, task", Thread.currentThread().getName()));
                    }
                    break;
                case TASK_SUCCESSFUL:
                    debugWrapper(TASK_SUCCESSFUL, wrapper);
                    wrapper.task.successful(wrapper.result);
                    wrapper.result = null;
                    wrapper.task = null;
                    if (DEBUG) {
                        Log.d(TAG, String.format("TASK_SUCCESSFUL (%s): erase wrapper: result, task", Thread.currentThread().getName()));
                    }
                    break;
            }
        }
    }

    private static class Wrapper<R, U> {
        Task<R, U> task;
        R result;
        U update;
        Exception exception;
        TaskContext<U> context;
        BackgroundHandler handler;
    }
}
