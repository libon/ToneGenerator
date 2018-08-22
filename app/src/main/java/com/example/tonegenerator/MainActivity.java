package com.example.tonegenerator;

import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;



import java.lang.reflect.Field;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    private ToneGenerator mToneGenerator;
    private Spinner mStreamsChoices;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mStreamsChoices = findViewById(R.id.streams_choices);
        loadStreams();
        loadTones();
    }

    private void loadStreams() {
        ArrayAdapter<String> streamsAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1);
        Field[] fields = AudioManager.class.getDeclaredFields();
        for (final Field field : fields) {
            if (field.getName().startsWith("STREAM_")) {
                streamsAdapter.add(field.getName());
            }
        }
        mStreamsChoices.setAdapter(streamsAdapter);
    }

    private void loadTones() {
        ViewGroup parent = findViewById(R.id.buttons_container);
        assert parent != null;
        Field[] fields = ToneGenerator.class.getDeclaredFields();
        for (final Field field : fields) {
            if (field.getName().startsWith("TONE_")) {
                Button button = new Button(this);
                button.setText(field.getName());
                button.setOnClickListener(v -> {
                    try {
                        String selectedStream = (String) mStreamsChoices.getSelectedItem();

                        Log.v(TAG, String.format(Locale.US, "Playing tone %s on stream %s", field.getName(), selectedStream));
                        int streamId = AudioManager.class.getDeclaredField(selectedStream).getInt(null);
                        if (mToneGenerator != null) mToneGenerator.release();
                        mToneGenerator = new ToneGenerator(streamId, 100);
                        mToneGenerator.startTone(field.getInt(null));
                    } catch (Exception e) {
                        Log.w(TAG, String.format(Locale.US, "Can't play tone %s: %s", field.getName(), e.getMessage()));
                    }
                });
                parent.addView(button);
            }
        }
    }

    public void onStopClicked(View view) {
        if (mToneGenerator != null) mToneGenerator.stopTone();
    }

    @Override
    protected void onDestroy() {
        if (mToneGenerator != null) mToneGenerator.release();
        super.onDestroy();
    }
}
