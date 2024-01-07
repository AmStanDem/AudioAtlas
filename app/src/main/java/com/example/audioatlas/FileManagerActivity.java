package com.example.audioatlas;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Objects;

public class FileManagerActivity extends AppCompatActivity implements  AudioFilesRecyclerViewAdapter.ItemClickListener {

    private AudioFilesRecyclerViewAdapter adapter;

    private MediaPlayer mediaPlayer = new MediaPlayer();

    /** ATTRIBUTES **/
    private ImageButton btnPrevious, btnPlayAndResume,btnNext,btnVolumeDown,btnVolumeMax;
    private SeekBar seekBarAudioLen;
    private TextView textViewStart, textViewEnd;
    private RecyclerView recyclerView;
    private File selectedFile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file_manager);
        // data to populate the RecyclerView with
        ArrayList<File> audioFiles = new ArrayList<>();
        Collections.addAll(audioFiles, Objects.requireNonNull(this.getCacheDir().listFiles()));
        setElementsId();
        // set up the RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new AudioFilesRecyclerViewAdapter(this, audioFiles);
        adapter.setClickListener(this);
        recyclerView.setAdapter(adapter);

        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp)
            {
                btnPlayAndResume.setImageResource(R.drawable.play_arrow);
                seekBarAudioLen.setProgress(0, false);
                mp = null;
            }
        });

        Handler mHandler = new Handler();
        FileManagerActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(mediaPlayer != null){
                    int mCurrentPosition = mediaPlayer.getCurrentPosition() / 1000;
                    seekBarAudioLen.setProgress(mCurrentPosition);
                }
                mHandler.post(this);
            }
        });
        seekBarAudioLen.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if(mediaPlayer != null && fromUser){
                    mediaPlayer.seekTo(progress * 1000);
                }
            }
        });

        btnPrevious.setOnClickListener(l->
        {
            if (mediaPlayer != null)
            {
                mediaPlayer.release();
                mediaPlayer = null;
                seekBarAudioLen.setProgress(0);
                btnPlayAndResume.setImageResource(R.drawable.play_arrow);
            }
        });
        btnPlayAndResume.setOnClickListener(l->
        {
            if (selectedFile != null)
            {
                setAudio(selectedFile);
                if (!mediaPlayer.isPlaying())
                {
                    playAudio();
                    btnPlayAndResume.setImageResource(R.drawable.pause);
                }
                else
                {
                    mediaPlayer.pause();
                    btnPlayAndResume.setImageResource(R.drawable.play_arrow);
                }
            }
        });
        btnNext.setOnClickListener(l->
        {
            if (mediaPlayer != null)
            {
                mediaPlayer.release();
                mediaPlayer = null;
                seekBarAudioLen.setProgress(seekBarAudioLen.getMax());
                btnPlayAndResume.setImageResource(R.drawable.play_arrow);

            }

        });
        btnVolumeDown.setOnClickListener(l ->
        {
            // Get the AudioManager service
            AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
            audioManager.adjustVolume(AudioManager.ADJUST_LOWER, AudioManager.FLAG_PLAY_SOUND);
        });
        btnVolumeMax.setOnClickListener(l->
        {
            // Get the AudioManager service
            AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
            audioManager.adjustVolume(AudioManager.ADJUST_RAISE, AudioManager.FLAG_PLAY_SOUND);
        });
    }

    @Override
    public void onPrepared(MediaPlayer mp) {

    }

    @Override
    public void onItemClick(View view, int position)
    {
        selectedFile = null;
        loadDataToMultimedia(position);
    }
    private void loadDataToMultimedia(int position)
    {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
        {
            selectedFile = adapter.getItem(position);
            mediaPlayer = null;
            String audioLen = String.valueOf(adapter.getAudioLength(selectedFile));
            textViewStart.setText("0:0:0");
            textViewEnd.setText(audioLen);
            seekBarAudioLen.setProgress(0, false);
            setAudio(selectedFile);
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
    private void playAudio()
    {
            try
            {
                mediaPlayer.start();
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    seekBarAudioLen.setMax((int) (mediaPlayer.getDuration() / 1000));
                }
                mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mp)
                    {
                        mp = null;
                        btnPlayAndResume.setImageResource(R.drawable.play_arrow);
                        seekBarAudioLen.setProgress(0, true);
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
    }

    private void setAudio(File selectedFile)
    {
        if (selectedFile == null)
            return;
        if (mediaPlayer == null)
            mediaPlayer = new MediaPlayer();
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                mediaPlayer.setDataSource(FileManagerActivity.this, Objects.requireNonNull(FileProvider.getUriForFile(FileManagerActivity.this, FileManagerActivity.this.getApplicationContext().getPackageName() + ".provider", selectedFile)));
            }
            mediaPlayer.prepare();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }


}