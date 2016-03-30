package io.github.the28awg.helper.task.app;

import android.os.Bundle;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import io.github.the28awg.helper.task.Task;
import io.github.the28awg.helper.task.TaskContext;
import io.github.the28awg.helper.task.TaskHelper;

public class Launcher extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launcher);
        findViewById(R.id.button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TaskHelper.helper().task(new Task<String, String>() {
                    @Override
                    public String task(TaskContext<String> context) {
                        for (int i = 0; i < 10; i++) {
                            context.update("Task@" + Integer.toHexString(this.hashCode()) + " (" + i + ")");
                            try {
                                Thread.sleep(1000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                        return null;
                    }

                    @Override
                    public void update(String progress) {
                        // ui thread?
                        if (Looper.myLooper() == Looper.getMainLooper()) {
                            // yep!
                        }
                    }

                    @Override
                    public void successful(String result) {
                        // ui thread?
                        if (Looper.myLooper() == Looper.getMainLooper()) {
                            // yep!
                        }
                    }

                    @Override
                    public void failure(Exception e) {
                        // ui thread?
                        if (Looper.myLooper() == Looper.getMainLooper()) {
                            // yep!
                        }
                    }
                });
            }
        });
    }
}
