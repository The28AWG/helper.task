package io.github.the28awg.helper.task;

import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

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
