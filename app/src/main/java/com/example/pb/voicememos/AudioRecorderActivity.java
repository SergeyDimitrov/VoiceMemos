package com.example.pb.voicememos;

import android.app.Activity;
import android.app.FragmentManager;
import android.os.Bundle;

public class AudioRecorderActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_audio_recorder);

        FragmentManager fm = getFragmentManager();
        AudioRecorderFragment fragment = (AudioRecorderFragment)fm.findFragmentById(R.id.recorder_fragment_container);

        if (fragment == null) {
            fragment = new AudioRecorderFragment();
            fm.beginTransaction().add(R.id.recorder_fragment_container, fragment).commit();
        }

    }

}
