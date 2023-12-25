package com.example.audioatlas;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.os.SystemClock;
import android.view.Menu;
import android.view.MenuInflater;
import android.widget.Chronometer;
import android.widget.Toolbar;

import com.google.android.material.bottomappbar.BottomAppBar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.shape.CornerFamily;
import com.google.android.material.shape.MaterialShapeDrawable;

public class MainActivity extends AppCompatActivity
{

    /** ATTRIBUTES */
    private FloatingActionButton btnRecord;

    private Chronometer chronometerRecordTimer;

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
        chronometerRecordTimer.setBase(SystemClock.elapsedRealtime());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.hamburger_menu_features, menu);
        // return true so that the menu pop up is opened
        return true;
    }

    private void setElementsIds()
    {
        btnRecord = (FloatingActionButton) findViewById(R.id.btn_record);
        chronometerRecordTimer = (Chronometer) findViewById(R.id.Chronometer_record_timer);
    }
}