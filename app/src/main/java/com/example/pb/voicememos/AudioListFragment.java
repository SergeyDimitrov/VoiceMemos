package com.example.pb.voicememos;

import android.app.Activity;
import android.app.FragmentManager;
import android.app.ListFragment;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class AudioListFragment extends ListFragment {

    private MediaPlayer player;
    private AudioRecord lastPlaying;
    private AudioRecord longClickedItem;

    private static final int REQUEST_ITEM_MENU = 1;
    private static final int REQUEST_RENAME = 2;
    private static final String ITEM_DIALOG_TAG = "item_dialog";
    private static final String RENAME_DIALOG_TAG = "rename_dialog";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        setRetainInstance(true);
        setListAdapter(new AudioAdapter(AudioLab.getInstance().getAudios()));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_audio_list, parent, false);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        releasePlayer();
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        AudioRecord audioRecord = (AudioRecord) getListAdapter().getItem(position);
        playAudio(audioRecord);
    }

    private void playAudio(AudioRecord record) {
        lastPlaying = record;
        String filePath = Environment.getExternalStorageDirectory().getPath()
                + "/Music/" + record.getName() + ".3gp";
        if (player != null) {
            releasePlayer();
        }
        player = new MediaPlayer();
        player.setAudioStreamType(AudioManager.STREAM_MUSIC);
        try {
            player.setDataSource(getActivity(), Uri.fromFile(new File(filePath)));
        } catch (IOException e) {
            Toast.makeText(getActivity(), R.string.no_file_error, Toast.LENGTH_SHORT).show();
            Log.d("myTAG", "Bad file");
        }
        player.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                player.start();
                Log.d("myTAG", "Prepared");
            }
        });
        player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                releasePlayer();
            }
        });
        player.prepareAsync();
    }

    @Override
    public void onStart() {
        super.onStart();
        getListView().setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                longClickedItem = AudioLab.getInstance().getPositionRecord(position);
                showItemDialog(AudioLab.getInstance().getPositionRecord(position));
                return true;
            }
        });
    }

    private void showItemDialog(AudioRecord record) {
        FragmentManager fm = getActivity().getFragmentManager();
        boolean isPlaying = isPlaying(record);
        ListItemDialogFragment fragment = ListItemDialogFragment.getInstance(isPlaying);
        fragment.setTargetFragment(this, REQUEST_ITEM_MENU);
        fragment.show(fm, ITEM_DIALOG_TAG);
    }

    private boolean isPlaying(AudioRecord record) {
        return player != null && record.equals(lastPlaying);
    }

    private void releasePlayer() {
        if (player != null) {
            Log.d("myTAG", "Releasing player");
            player.reset();
            player.release();
            player = null;
        }
    }

    private class AudioAdapter extends ArrayAdapter<AudioRecord> {
        public AudioAdapter(ArrayList<AudioRecord> audioRecords) {
            super(getActivity(), 0, audioRecords);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = getActivity().getLayoutInflater().inflate(R.layout.audio_list_item, parent, false);
            }
            AudioRecord audioRecord = getItem(position);
            TextView name = (TextView)convertView.findViewById(R.id.name_view);
            TextView date = (TextView)convertView.findViewById(R.id.date_view);
            name.setText(audioRecord.getName());
            date.setText(getFormattedDate(audioRecord.getDate()));
            return convertView;
        }

        private String getFormattedDate(Date date) {
            SimpleDateFormat parser = new SimpleDateFormat("MM/dd/yyyy'\n'hh:mm:ss");
            return parser.format(date);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        ((AudioAdapter)getListAdapter()).notifyDataSetChanged();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.audio_list_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.add_audio:
                releasePlayer();
                Intent i = new Intent(getActivity(), AudioRecorderActivity.class);
                startActivity(i);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_ITEM_MENU:
                if (resultCode == Activity.RESULT_OK) {
                    int command = data.getExtras().getInt(ListItemDialogFragment.EXTRA_COMMAND);
                    switch (command) {
                        case ListItemDialogFragment.START_KEY:
                            playAudio(longClickedItem);
                            break;
                        case ListItemDialogFragment.STOP_KEY:
                            releasePlayer();
                            break;
                        case ListItemDialogFragment.DELETE_KEY:
                            if (isPlaying(longClickedItem)) releasePlayer();
                            AudioLab.getInstance().deleteFile(longClickedItem);
                            ((AudioAdapter) getListAdapter()).notifyDataSetChanged();
                            break;
                        case ListItemDialogFragment.RENAME_KEY:
                            if (isPlaying(longClickedItem)) releasePlayer();
                            FragmentManager fm = getActivity().getFragmentManager();
                            SaveAudioDialogFragment fragment = SaveAudioDialogFragment.getInstance(true, longClickedItem.getName());
                            fragment.setTargetFragment(this, REQUEST_RENAME);
                            fragment.show(fm, RENAME_DIALOG_TAG);
                    }
                }
                break;
            case REQUEST_RENAME:
                if (resultCode == Activity.RESULT_OK) {
                    String newFilename = data.getExtras().getString(SaveAudioDialogFragment.EXTRA_FILENAME);
                    if (newFilename == null) {
                        Toast.makeText(getActivity(), R.string.filename_exists, Toast.LENGTH_SHORT).show();
                    } else {
                        AudioRecord renamedRecord = new AudioRecord(longClickedItem.getDate(), newFilename);
                        AudioLab.getInstance().deleteFile(longClickedItem);
                        AudioLab.getInstance().add(renamedRecord);
                        ((AudioAdapter) getListAdapter()).notifyDataSetChanged();
                    }
                }
                break;
            default:
                Toast.makeText(getActivity(), R.string.wrong_case, Toast.LENGTH_SHORT).show();
                break;
        }
    }
}
