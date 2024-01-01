package com.example.audioatlas;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Objects;

public class FileManagerActivity extends AppCompatActivity implements  AudioFilesRecyclerViewAdapter.ItemClickListener {

    private AudioFilesRecyclerViewAdapter adapter;

    private boolean isPlaying = false;

    private MediaPlayer mediaPlayer = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file_manager);
        // data to populate the RecyclerView with
        ArrayList<File> audioFiles = new ArrayList<>();

        Collections.addAll(audioFiles, Objects.requireNonNull(this.getFilesDir().listFiles()));

        // set up the RecyclerView
        RecyclerView recyclerView = findViewById(R.id.audioFilesRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new AudioFilesRecyclerViewAdapter(this, audioFiles);
        adapter.setClickListener(this);
        recyclerView.setAdapter(adapter);
    }

    @Override
    public void onItemClick(View view, int position) {
        //Toast.makeText(this, "You clicked " + adapter.getItem(position) + " on row number " + position, Toast.LENGTH_SHORT).show();
        File file = adapter.getItem(position);

        if(!isPlaying)
        {
            mediaPlayer = new MediaPlayer();
            try {
                // below method is used to set the
                // data source which will be our file name
                mediaPlayer.setDataSource(file.getAbsolutePath());

                // below method will prepare our media player
                mediaPlayer.prepare();

                // below method will start our media player.
                mediaPlayer.start();

                isPlaying = true;
            } catch (IOException e) {
                Log.e("TAG", "prepare() failed");
            }
        }
        else
        {
            mediaPlayer.stop();
            mediaPlayer.reset();
            mediaPlayer.release();
            mediaPlayer = null;
            isPlaying = false;

        }


    }
}