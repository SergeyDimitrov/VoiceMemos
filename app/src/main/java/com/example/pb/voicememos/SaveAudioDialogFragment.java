package com.example.pb.voicememos;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class SaveAudioDialogFragment extends DialogFragment {

    private EditText audioName;
    private static final int BUFFER_SIZE = 1024;
    public static final String EXTRA_FILENAME = "extra_filename";

    public static final String SOURCE_FILENAME_KEY = "source_filename";
    public static final String IS_RENAMING_KEY = "is_renaming";

    public static SaveAudioDialogFragment getInstance(boolean isRenaming, String oldName) {
        SaveAudioDialogFragment fragment = new SaveAudioDialogFragment();
        Bundle args = new Bundle();
        args.putBoolean(IS_RENAMING_KEY, isRenaming);
        args.putString(SOURCE_FILENAME_KEY, oldName);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View v = getActivity().getLayoutInflater().inflate(R.layout.dialog_save, null);

        final boolean isRenaming = getArguments().getBoolean(IS_RENAMING_KEY);
        final String sourceFilename = getArguments().getString(SOURCE_FILENAME_KEY);

        audioName = (EditText)v.findViewById(R.id.audio_name);
        if (sourceFilename == null) audioName.setText(getNextName());
        else audioName.setText(sourceFilename);

        return new AlertDialog.Builder(getActivity()).
                setView(v).
                setTitle(R.string.save_dialog_title).
                setPositiveButton(R.string.save, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        boolean success = true;
                        String filename = null;
                        if (audioName.getText().toString().equals("")) {
                            Toast.makeText(getActivity(), R.string.empty_audioname_error, Toast.LENGTH_SHORT).show();
                            success = false;
                        } else {
                            filename = getUniqueFilename(audioName.getText().toString());
                            if (isRenaming) {
                                Log.d("myTAG", "Here");
                                if (!filename.equals(audioName.getText().toString())) {
                                    sendResult(Activity.RESULT_OK, null);
                                    return;
                                }
                            }
                            FileInputStream in = null;
                            FileOutputStream out = null;
                            try {
                                if (!isRenaming) {
                                    in = new FileInputStream(getActivity().getFilesDir() + "/"
                                            + getResources().getString(R.string.temp_audio_filename));
                                } else {
                                    in = new FileInputStream(Environment.getExternalStorageDirectory().getPath()
                                            + "/Music/" + sourceFilename + ".3gp");
                                }

                                out = new FileOutputStream(Environment.getExternalStorageDirectory().getPath()
                                        + "/Music/" + filename + ".3gp");

                                byte[] buffer = new byte[BUFFER_SIZE];
                                int bytesRead;

                                while ((bytesRead = in.read(buffer)) > 0) {
                                    out.write(buffer, 0, bytesRead);
                                }

                            } catch (IOException e) {
                                Log.d("myTAG", "MSG", e);
                                Toast.makeText(getActivity(), R.string.writing_file_error, Toast.LENGTH_SHORT).show();
                                success = false;
                            } finally {
                                try {
                                    if (in != null) {
                                        in.close();
                                    }
                                    if (out != null) {
                                        out.flush();
                                        out.close();
                                    }
                                } catch (IOException e) {
                                    Toast.makeText(getActivity(), R.string.closing_streams_error, Toast.LENGTH_SHORT).show();
                                    success = false;
                                }
                            }
                        }
                        if (success) sendResult(Activity.RESULT_OK, filename);
                        else sendResult(Activity.RESULT_CANCELED, null);
                    }
                }).
                setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        sendResult(Activity.RESULT_CANCELED, null);
                    }
                }).create();
    }

    private String getNextName() {
        AudioLab al = AudioLab.getInstance();
        int currNum = al.getAudios().size() + 1;
        String currFilename = null;
        boolean found = false;
        while (!found) {
            currFilename = getResources().getString(R.string.default_audio_name) + " " + getNumber(currNum);
            if (!AudioLab.getInstance().doesNameExist(currFilename)) {
                found = true;
            }
            currNum++;
        }
        return currFilename;
    }

    private String getNumber(int num) {
        if (num < 10) return "00" + num;
        else if (num < 100) return "0" + num;
        else return "" + num;
    }

    private String getUniqueFilename(String sourceFilename) {
        if (AudioLab.getInstance().doesNameExist(sourceFilename)) {
            int currNum = 1;
            boolean isNameFound = false;
            String currFilename = null;
            while(!isNameFound) {
                currFilename = sourceFilename + " " + getNumber(currNum);

                if (!AudioLab.getInstance().doesNameExist(currFilename))
                    isNameFound = true;

                currNum++;
            }
            return currFilename;
        } else {
            return sourceFilename;
        }
    }

    private void sendResult(int resultCode, String filename) {
        if (getTargetFragment() == null)
            return;

        Intent i = new Intent();
        i.putExtra(EXTRA_FILENAME, filename);
        getTargetFragment().onActivityResult(getTargetRequestCode(), resultCode, i);
    }
}
