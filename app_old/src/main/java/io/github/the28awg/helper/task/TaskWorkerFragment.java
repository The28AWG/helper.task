package io.github.the28awg.helper.task;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.HashMap;

import io.github.the28awg.helper.task.Task;

/**
 * Created by the28awg on 26.03.16.
 */

public class TaskWorkerFragment extends Fragment {

    private ListView listView;
    private TaskWorkerListenerImpl listener;
    public TaskWorkerFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_task_worker, container, false);
        listView = (ListView) view.findViewById(R.id.listView);
        LinearLayout empty_task = (LinearLayout) view.findViewById(R.id.empty_task);
        listView.setEmptyView(empty_task);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        TaskAdapter adapter = new TaskAdapter(this.getContext());
        listView.setAdapter(adapter);
        listener = new TaskWorkerListenerImpl(adapter);
        TaskHelper.helper().observable().registerObserver(listener);
    }

    @Override
    public void onPause() {
        super.onPause();
        TaskHelper.helper().observable().unregisterObserver(listener);
        listView.setAdapter(null);
    }

    public static class TaskWorkerListenerImpl implements TaskHelper.TaskWorkerListener {

        private TaskAdapter adapter;
        private TaskWorkerListenerImpl(TaskAdapter adapter) {
            this.adapter = adapter;
        }
        @Override
        public void execute(Task task) {
            adapter.notifyDataSetChanged();
        }

        @Override
        public void update(Task task, Object progress) {
            if (progress instanceof String) {
                adapter.update(task, progress);
            }
        }

        @Override
        public void successful(Task task, Object result) {
            adapter.successful(task);
            adapter.notifyDataSetChanged();
        }

        @Override
        public void failure(Task task, Exception e) {
            adapter.failure(task);
            adapter.notifyDataSetChanged();
        }
    }

    private static class TaskAdapter extends BaseAdapter {

        private final Object lock = new Object();
        private Context mContext;
        private HashMap<Task, Object> cache = new HashMap<>();

        public TaskAdapter(Context context) {
            mContext = context;
        }

        static class ViewHolder {
            TextView task_title;
            ProgressBar task_progress;
        }

        @Override
        public Task getItem(int i) {
            return TaskHelper.helper().tasks().get(i);
        }

        public void update(Task task, Object progress) {
            synchronized (lock) {
                cache.put(task, progress);
                notifyDataSetInvalidated();
            }
        }

        public void successful(Task task) {
            synchronized (lock) {
                if (cache.containsKey(task)) {
                    cache.remove(task);
                }
            }
        }

        public void failure(Task task) {
            synchronized (lock) {
                cache.remove(task);
            }
        }

        @Override
        public View getView(int position, View convertView, ViewGroup viewGroup) {
            ViewHolder viewHolder;

            if (convertView == null){
                LayoutInflater inflater = (LayoutInflater) mContext
                        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(R.layout.item_task_progress, viewGroup, false);
                viewHolder = new ViewHolder();
                viewHolder.task_title = (TextView) convertView.findViewById(R.id.task_title);
                viewHolder.task_progress = (ProgressBar) convertView.findViewById(R.id.task_progress);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }

            Task task = getItem(position);
            Object update = cache.get(task);
            viewHolder.task_title.setText((update != null && update instanceof String) ? String.format("%s", update) : String.format("Task@%s", Integer.toHexString(task.hashCode())));

            return convertView;
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public int getCount() {
            return TaskHelper.helper().tasks().size();
        }
    }
}