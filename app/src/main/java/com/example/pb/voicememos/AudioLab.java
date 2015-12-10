package com.example.pb.voicememos;

import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;

public class AudioLab {
    private static AudioLab audioLab;
    private ArrayList<AudioRecord> audioRecords;
    private HashSet<String> filenames;

    private AudioLab() {
        audioRecords = new ArrayList<>();
        filenames = new HashSet<>();
    }

    public static AudioLab getInstance() {
        if (audioLab == null) {
            return audioLab = new AudioLab();
        } else {
            return audioLab;
        }
    }

    public ArrayList<AudioRecord> getAudios() {
        return audioRecords;
    }

    public boolean doesNameExist(String filename) {
        return filenames.contains(filename);
    }

    public void add(AudioRecord newAudio) {
        audioRecords.add(newAudio);
        filenames.add(newAudio.getName());
    }

    public AudioRecord getPositionRecord(int position) {
        return audioRecords.get(position);
    }

    public void deleteFile(AudioRecord record) {
        File audioFile = new File(Environment.getExternalStorageDirectory().getPath()
                + "/Music/" + record.getName() + ".3gp");
        if (audioFile.delete()) {
            Log.d("myTAG", "OK. File deleted");
        } else {
            Log.d("myTAG", "NOT OK. File is not deleted");
        }
        audioRecords.remove(record);
        filenames.remove(record.getName());
    }
}
