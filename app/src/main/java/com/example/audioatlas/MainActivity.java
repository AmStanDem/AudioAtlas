package com.example.audioatlas;
import static android.Manifest.permission.MODIFY_AUDIO_SETTINGS;
import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.RECORD_AUDIO;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static android.Manifest.permission.WRITE_SETTINGS;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.widget.Chronometer;
import android.widget.Toast;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
public class MainActivity extends AppCompatActivity
{
    private static final int REQUEST_AUDIO_PERMISSION_CODE = 1;
    /** ATTRIBUTES */
    private FloatingActionButton btnRecord, btnStop, btnPause;
    private Chronometer chronometerRecordTimer;
    private MaterialToolbar topBar;
    private long timeWhenStopped = 0;
    private MediaRecorder mediaRecorder;
    private boolean isRecordingPaused = false;
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
        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());
        timeWhenStopped = chronometerRecordTimer.getBase() - SystemClock.elapsedRealtime();
        chronometerRecordTimer.stop();
        if (CheckPermissions())
            RequestPermissions();

        topBar.setOnMenuItemClickListener(menuItem ->
        {
            if (menuItem.getItemId() == R.id.records)
            {
                changeActivityToRecordsActivity();
            }
            return false;
        });
        btnRecord.setOnClickListener(v -> {
            //Log.println(Log.DEBUG, "C", "Ciao");
            if(CheckPermissions())
            {
                btnRecord.setVisibility(View.INVISIBLE);
                btnPause.setVisibility(View.VISIBLE);
                btnStop.setVisibility(View.VISIBLE);
                SimpleDateFormat timeStampFormat = new SimpleDateFormat
                        (
                                "dd-MM-yyyy-HH.mm.ss", Locale.ITALY
                        );

                File Directory = MainActivity.this.getCacheDir();
                File file = new File(Directory, "AudioAtlas_" + timeStampFormat.format(new Date()) + ".mp3");
                //Log.println(Log.DEBUG, "d", file.getAbsolutePath());
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
                chronometerRecordTimer.setBase(SystemClock.elapsedRealtime() + timeWhenStopped);
                chronometerRecordTimer.start();
            }
            else
            {
                RequestPermissions();
            }
        });
        btnStop.setOnClickListener(v -> {
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
                    chronometerRecordTimer.setBase(SystemClock.elapsedRealtime());
                    chronometerRecordTimer.stop();
                    timeWhenStopped = 0;
                }
                changeActivityToRecordsActivity();
            }
        });
        btnPause.setOnClickListener(v -> {
            if (mediaRecorder != null)
            {
                if (!isRecordingPaused)
                {
                    mediaRecorder.pause();
                    timeWhenStopped = chronometerRecordTimer.getBase() - SystemClock.elapsedRealtime();
                    chronometerRecordTimer.stop();
                    isRecordingPaused = true;
                    btnPause.setImageResource(R.drawable.play_arrow);
                }
                else
                {
                    mediaRecorder.resume();
                    chronometerRecordTimer.setBase(SystemClock.elapsedRealtime() + timeWhenStopped);
                    chronometerRecordTimer.start();
                    isRecordingPaused = false;
                    btnPause.setImageResource(R.drawable.pause);
                }
            }
        });
    }
    private void setElementsIds()
    {
        topBar = findViewById(R.id.topAppBar);
        btnRecord = findViewById(R.id.btn_record);
        btnPause = findViewById(R.id.btn_pause);
        btnStop = findViewById(R.id.btn_stop);
        chronometerRecordTimer = findViewById(R.id.Chronometer_record_timer);
    }
    public boolean CheckPermissions() {
        // this method is used to check permission
        int result = ActivityCompat.checkSelfPermission(getApplicationContext(), WRITE_EXTERNAL_STORAGE);
        int result1 = ActivityCompat.checkSelfPermission(getApplicationContext(), RECORD_AUDIO);
        int result2 = ActivityCompat.checkSelfPermission(getApplicationContext(), READ_EXTERNAL_STORAGE);
        int result3 = ActivityCompat.checkSelfPermission(getApplicationContext(), WRITE_SETTINGS);
        int result4 = ActivityCompat.checkSelfPermission(getApplicationContext(), MODIFY_AUDIO_SETTINGS);
        return
                        result != PackageManager.PERMISSION_GRANTED ||
                        result1 != PackageManager.PERMISSION_GRANTED ||
                        result2 != PackageManager.PERMISSION_GRANTED ||
                        result3 != PackageManager.PERMISSION_GRANTED ||
                        result4 != PackageManager.PERMISSION_GRANTED;
    }
    private void RequestPermissions() {
        // this method is used to request the
        // permission for audio recording and storage.
        ActivityCompat.requestPermissions(MainActivity.this, new String[]{RECORD_AUDIO, WRITE_EXTERNAL_STORAGE, READ_EXTERNAL_STORAGE, WRITE_SETTINGS, MODIFY_AUDIO_SETTINGS}, REQUEST_AUDIO_PERMISSION_CODE);
    }
    private void changeActivityToRecordsActivity()
    {
        Intent intent = new Intent(MainActivity.this, FileManagerActivity.class);
        startActivity(intent);
    }
}