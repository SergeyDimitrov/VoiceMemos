package com.example.pb.voicememos;

import android.app.Activity;
import android.app.FragmentManager;
import android.os.Bundle;

public class AudioListActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_audios);

        FragmentManager fm = getFragmentManager();
        AudioListFragment fragment = (AudioListFragment)fm.findFragmentById(R.id.list_fragment_container);

        if (fragment == null) {
            fragment = new AudioListFragment();
            fm.beginTransaction().add(R.id.list_fragment_container, fragment).commit();
        }
    }
}
