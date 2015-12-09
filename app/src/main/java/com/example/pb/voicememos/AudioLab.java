package com.example.pb.voicememos;

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

}
