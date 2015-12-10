package com.example.pb.voicememos;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

public class ListItemDialogFragment extends DialogFragment {

    private static final String SET_STOP_KEY = "set_stop_key";

    public static final String EXTRA_COMMAND = "extra_command";
    public static final int START_KEY = 0;
    public static final int STOP_KEY = 1;
    public static final int RENAME_KEY = 2;
    public static final int DELETE_KEY = 3;

    public static ListItemDialogFragment getInstance(boolean setPlay) {
        ListItemDialogFragment curr = new ListItemDialogFragment();
        Bundle args = new Bundle();
        args.putBoolean(SET_STOP_KEY, setPlay);
        curr.setArguments(args);
        return curr;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        String[] items = new String[] {"Start", "Rename", "Delete", "Cancel"};

        final boolean isStop;

        if (getArguments().getBoolean(SET_STOP_KEY)) {
            items[0] = "Stop";
            isStop = true;
        } else {
            isStop = false;
        }

        return new AlertDialog.Builder(getActivity()).setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case 0:
                        sendResult(Activity.RESULT_OK, isStop ? STOP_KEY : START_KEY);
                        break;
                    case 1:
                        sendResult(Activity.RESULT_OK, RENAME_KEY);
                        break;
                    case 2:
                        sendResult(Activity.RESULT_OK, DELETE_KEY);
                        break;
                    case 3:
                        sendResult(Activity.RESULT_CANCELED, -1);
                        break;
                    default:
                        Log.d("myTAG", "Wrong case");
                        Toast.makeText(getActivity(), R.string.wrong_case, Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        }).create();
    }

    private void sendResult(int resultCode, int commandCode) {
        if (getTargetFragment() == null)
            return;

        Intent i = new Intent();
        i.putExtra(EXTRA_COMMAND, commandCode);
        getTargetFragment().onActivityResult(getTargetRequestCode(), resultCode, i);
    }

}
