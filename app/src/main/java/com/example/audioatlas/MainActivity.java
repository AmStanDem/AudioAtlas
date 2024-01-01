package com.example.audioatlas;

import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.RECORD_AUDIO;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

import static androidx.core.content.PackageManagerCompat.LOG_TAG;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.SystemClock;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.Chronometer;
import android.widget.Toast;
import android.widget.Toolbar;

import com.google.android.material.bottomappbar.BottomAppBar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.shape.CornerFamily;
import com.google.android.material.shape.MaterialShapeDrawable;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

public class MainActivity extends AppCompatActivity
{

    private static final int REQUEST_AUDIO_PERMISSION_CODE = 1;
    /** ATTRIBUTES */
    private FloatingActionButton btnRecord, btnStop, btnPause;
    private Chronometer chronometerRecordTimer;

    private MediaRecorder mediaRecorder;



    /**************************************/

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setElementsIds();
        chronometerRecordTimer.stop();
        chronometerRecordTimer.setBase(SystemClock.elapsedRealtime());
        if (!CheckPermissions())
            RequestPermissions();

        btnRecord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.println(Log.DEBUG, "C", "Ciao");
                if(!CheckPermissions())
                {

                    btnRecord.setVisibility(View.INVISIBLE);
                    btnPause.setVisibility(View.VISIBLE);
                    btnStop.setVisibility(View.VISIBLE);

                    SimpleDateFormat timeStampFormat = new SimpleDateFormat
                            (
                                    "dd-MM-yyyy-HH.mm.ss", Locale.ITALY
                            );

                    File Directory = MainActivity.this.getFilesDir();

                    File file = new File(Directory, "AudioAtlas_" + timeStampFormat.format(new Date()) + ".mp4");

                    Log.println(Log.DEBUG, "d", file.getAbsolutePath());

                    mediaRecorder = new MediaRecorder();

                    mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
                    mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        mediaRecorder.setOutputFile(file);
                    }
                    mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

                    try {
                        mediaRecorder.prepare();
                    } catch (IOException e) {
                        Log.println(Log.DEBUG, "", "prepare() failed");
                    }

                    mediaRecorder.start();
                    chronometerRecordTimer.start();

                }
                else
                {
                    RequestPermissions();
                }
            }
        });
        btnStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mediaRecorder != null) {
                    try
                    {
                        mediaRecorder.stop();
                        mediaRecorder.reset();
                        mediaRecorder.release();
                        mediaRecorder = null;
                    }
                    catch (NullPointerException e)
                    {
                        Toast.makeText(getApplicationContext(),
                                "Recording not started", Toast.LENGTH_SHORT).show();
                        e.printStackTrace();
                    }
                    finally
                    {
                        btnStop.setVisibility(View.INVISIBLE);
                        btnPause.setVisibility(View.INVISIBLE);
                        btnRecord.setVisibility(View.VISIBLE);
                        chronometerRecordTimer.stop();
                        chronometerRecordTimer.setBase(SystemClock.elapsedRealtime());
                    }
                    Intent intent = new Intent(MainActivity.this, FileManagerActivity.class);
                    startActivity(intent);
                }
            }
        });
    }





    private void setElementsIds()
    {
        btnRecord = (FloatingActionButton) findViewById(R.id.btn_record);
        btnPause = (FloatingActionButton) findViewById(R.id.btn_pause);
        btnStop = (FloatingActionButton) findViewById(R.id.btn_stop);
        chronometerRecordTimer = (Chronometer) findViewById(R.id.Chronometer_record_timer);
    }

    public boolean CheckPermissions() {
        // this method is used to check permission
        int result = ActivityCompat.checkSelfPermission(getApplicationContext(), WRITE_EXTERNAL_STORAGE);
        int result1 = ActivityCompat.checkSelfPermission(getApplicationContext(), RECORD_AUDIO);
        int result2 = ActivityCompat.checkSelfPermission(getApplicationContext(), READ_EXTERNAL_STORAGE);
        return result == PackageManager.PERMISSION_GRANTED && result1 == PackageManager.PERMISSION_GRANTED && result2 == PackageManager.PERMISSION_GRANTED;
    }

    private void RequestPermissions() {
        // this method is used to request the
        // permission for audio recording and storage.
        ActivityCompat.requestPermissions(MainActivity.this, new String[]{RECORD_AUDIO, WRITE_EXTERNAL_STORAGE}, REQUEST_AUDIO_PERMISSION_CODE);
    }


}