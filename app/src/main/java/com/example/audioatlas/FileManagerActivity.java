package com.example.audioatlas;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Objects;

public class FileManagerActivity extends AppCompatActivity implements  AudioFilesRecyclerViewAdapter.ItemClickListener {

    private AudioFilesRecyclerViewAdapter adapter;
    private MediaPlayer mediaPlayer = null;

    /** ATTRIBUTES **/
    private ImageButton btnPrevious, btnPlayAndResume,btnNext,btnVolumeDown,btnVolumeMax;
    private SeekBar seekBarAudioLen;
    private TextView textViewStart, textViewEnd;
    private RecyclerView recyclerView;

    /****************/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file_manager);
        // data to populate the RecyclerView with
        ArrayList<File> audioFiles = new ArrayList<>();
        Collections.addAll(audioFiles, Objects.requireNonNull(this.getFilesDir().listFiles()));

        setElementsId();
        // set up the RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new AudioFilesRecyclerViewAdapter(this, audioFiles);
        adapter.setClickListener(this);
        recyclerView.setAdapter(adapter);
    }


    @Override
    public void onItemClick(View view, int position)
    {
        loadDataToMultimedia(position);
    }

    private void loadDataToMultimedia(int position)
    {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
        {
            File selectedFile = adapter.getItem(position);
            String audioLen = String.valueOf(adapter.getAudioLength(selectedFile));
            textViewStart.setText("0:0:0");
            textViewEnd.setText(audioLen);
        }
    }

    private void setElementsId()
    {
        btnPrevious = findViewById(R.id.btn_previous);
        btnPlayAndResume = findViewById(R.id.btn_play_resume);
        btnNext = findViewById(R.id.btn_next);
        btnVolumeDown = findViewById(R.id.btn_volume_down);
        btnVolumeMax = findViewById(R.id.btn_volume_up);
        textViewStart = findViewById(R.id.TextView_audio_start);
        seekBarAudioLen = findViewById(R.id.seekBar_audio_duration);
        textViewEnd = findViewById(R.id.TextView_audio_end);
        recyclerView = findViewById(R.id.recycler_view_audio_files);
    }
}