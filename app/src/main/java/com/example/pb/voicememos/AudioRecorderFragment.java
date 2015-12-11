package com.example.pb.voicememos;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Intent;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import java.io.IOException;
import java.util.Date;


public class AudioRecorderFragment extends Fragment {
    private Button recorderButton;
    private int currState = STATE_IDLE;
    private MediaRecorder recorder;
    private Handler handler;
    private Date lastRecordDate;
    private Runnable stopRecordingAction = new Runnable() {
        @Override
        public void run() {
            stopRecording(false);
        }
    };

    private static final String SAVE_DIALOG_TAG = "save_dialog_tag";

    private static final int STATE_IDLE = 1;
    private static final int STATE_RECORDING = 2;

    private static final int REQUEST_SAVE = 0;

    private static final int MAX_RECORDING_TIME_MILLIS = 10000;

    public AudioRecorderFragment() {
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        prepareRecorder();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_audio_recorder, parent, false);
        recorderButton = (Button)v.findViewById(R.id.recorder_button);

        updateUI(currState);
        return v;
    }

    private void prepareRecorder() {
        recorder = new MediaRecorder();
        recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_WB);
        recorder.setOutputFile(getActivity().getFilesDir() + "/" + getResources().getString(R.string.temp_audio_filename));
    }

    private void updateUI(int state) {
        currState = state;
        switch (state) {
            case STATE_IDLE:
                recorderButton.setText(R.string.start_recording);
                recorderButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        startRecording();
                    }
                });
                break;
            case STATE_RECORDING:
                recorderButton.setText(R.string.stop_recording);
                recorderButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        stopRecording(false);
                    }
                });
                break;
            default:
                Toast.makeText(getActivity(), R.string.unknown_state_error, Toast.LENGTH_SHORT).show();
                break;
        }
    }

    private void startRecording() {
        try {
            recorder.prepare();
            recorder.start();
            handler = new Handler();
            handler.postDelayed(stopRecordingAction, MAX_RECORDING_TIME_MILLIS);
            updateUI(STATE_RECORDING);
        } catch (IOException e) {
            Toast.makeText(getActivity(), R.string.recorder_prepare_error, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.d("myTAG", "onStop");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d("myTAG", "onDestroy");
        if (currState == STATE_RECORDING) {
            stopRecording(true);
        }
    }

    private void stopRecording(boolean isOperationCancelled) {
        updateUI(STATE_IDLE);
        handler.removeCallbacks(stopRecordingAction);
        try {
            recorder.stop();
        } catch (RuntimeException e) {
            isOperationCancelled = true;
            getActivity().finish();
        }
        recorder.release();
        if (!isOperationCancelled) {
            lastRecordDate = new Date();
            showSaveDialog();
        }
    }

    private void showSaveDialog() {
        FragmentManager fm = getActivity().getFragmentManager();
        SaveAudioDialogFragment fragment = SaveAudioDialogFragment.getInstance(false, null);
        fragment.setTargetFragment(this, REQUEST_SAVE);
        fragment.show(fm, SAVE_DIALOG_TAG);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_SAVE) {
            if (resultCode == Activity.RESULT_OK) {
                String audioName = data.getStringExtra(SaveAudioDialogFragment.EXTRA_FILENAME);
                AudioLab.getInstance().add(new AudioRecord(lastRecordDate, audioName));
            }
            getActivity().finish();
        }
    }
}
