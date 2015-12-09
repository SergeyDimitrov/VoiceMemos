package com.example.pb.voicememos;

import android.app.ListFragment;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
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

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        setRetainInstance(true);
        setListAdapter(new AudioAdapter(AudioLab.getInstance().getAudios()));
    }

    @Override
    public void onStart() {
        super.onStart();
        setEmptyText(getResources().getString(R.string.empty_view_text));
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        releasePlayer();
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        AudioRecord audioRecord = (AudioRecord) getListAdapter().getItem(position);
        String filePath = Environment.getExternalStorageDirectory().getPath()
                + "/Music/" + audioRecord.getName() + ".3gp";
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
}
