package com.example.mediaplayer.Activity.Activity;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.example.mediaplayer.Activity.Adapter.RecordingAdapter;
import com.example.mediaplayer.Activity.Model.Model;
import com.example.mediaplayer.R;

import java.io.File;
import java.util.ArrayList;

public class ListRecordings extends AppCompatActivity
{
    private Toolbar toolbar;
    private RecyclerView recyclerViewRecordings;
    private ArrayList<Model> recordingArraylist;
    private RecordingAdapter recordingAdapter;
    private TextView textViewNoRecordings;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_recordings);

        initViews();
        fetchRecordings();
    }

    private void fetchRecordings()
    {
        File root = android.os.Environment.getExternalStorageDirectory();
        String path = root.getAbsolutePath() + "/VoiceRecorder/Audios";
        Log.d("Files", "Path: " + path);
        File directory = new File(path);
        File[] files = directory.listFiles();
        Log.d("Files", "Size: "+ files.length);
        if( files!=null ){

            for (int i = 0; i < files.length; i++) {

                Log.d("Files", "FileName:" + files[i].getName());
                String fileName = files[i].getName();
                String recordingUri = root.getAbsolutePath() + "/VoiceRecorder/Audios/" + fileName;

                Model recording = new Model(recordingUri,fileName,false);
                recordingArraylist.add(recording);
            }

            textViewNoRecordings.setVisibility(View.GONE);
            recyclerViewRecordings.setVisibility(View.VISIBLE);
            setAdaptertoRecyclerView();

        }else{
            textViewNoRecordings.setVisibility(View.VISIBLE);
            recyclerViewRecordings.setVisibility(View.GONE);
        }
    }

    private void setAdaptertoRecyclerView()
    {

        recordingAdapter = new RecordingAdapter(this,recordingArraylist);
        recyclerViewRecordings.setAdapter(recordingAdapter);
    }

    private void initViews()
    {
        recordingArraylist  =   new ArrayList<Model>();
        /* setting up the toolbar  */

       /* toolbar             =   findViewById(R.id.toolbar);
        toolbar.setTitle("Recording List");
        toolbar.setTitleTextColor(getResources().getColor(android.R.color.black));
        setSupportActionBar(toolbar);*/

        /* enabling back button */
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        /*setting up recyclerView*/
        recyclerViewRecordings  =   findViewById(R.id.recyclerViewRecordings);
        recyclerViewRecordings.setLayoutManager(new LinearLayoutManager(this,LinearLayoutManager.VERTICAL, false));
        recyclerViewRecordings.setHasFixedSize(true);

        textViewNoRecordings    =   findViewById(R.id.textViewNoRecordings);

    }
}


/*import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;

public class RecordingListActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private RecyclerView recyclerViewRecordings;
    private ArrayList<Recording> recordingArraylist;
    private RecordingAdapter recordingAdapter;
    private TextView textViewNoRecordings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recording_list);


        initViews();

        fetchRecordings();
    }

    private void initViews() {
        recordingArraylist = new ArrayList<Recording>();
        /** setting up the toolbar  **/
/*
        toolbar = (Toolbar) findViewById(R.id.toolbar);
                toolbar.setTitle("Recording List");
                toolbar.setTitleTextColor(getResources().getColor(android.R.color.black));
                setSupportActionBar(toolbar);

                /** enabling back button ***/
/*
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);

                /** setting up recyclerView **//*
                recyclerViewRecordings = (RecyclerView) findViewById(R.id.recyclerViewRecordings);
                recyclerViewRecordings.setLayoutManager(new LinearLayoutManager(this,LinearLayoutManager.VERTICAL, false));
                recyclerViewRecordings.setHasFixedSize(true);

                textViewNoRecordings = (TextView) findViewById(R.id.textViewNoRecordings);

                }

private void fetchRecordings() {

        File root = android.os.Environment.getExternalStorageDirectory();
        String path = root.getAbsolutePath() + "/VoiceRecorderSimplifiedCoding/Audios";
        Log.d("Files", "Path: " + path);
        File directory = new File(path);
        File[] files = directory.listFiles();
        Log.d("Files", "Size: "+ files.length);
        if( files!=null ){

        for (int i = 0; i < files.length; i++) {

        Log.d("Files", "FileName:" + files[i].getName());
        String fileName = files[i].getName();
        String recordingUri = root.getAbsolutePath() + "/VoiceRecorderSimplifiedCoding/Audios/" + fileName;

        Recording recording = new Recording(recordingUri,fileName,false);
        recordingArraylist.add(recording);
        }

        textViewNoRecordings.setVisibility(View.GONE);
        recyclerViewRecordings.setVisibility(View.VISIBLE);
        setAdaptertoRecyclerView();

        }else{
        textViewNoRecordings.setVisibility(View.VISIBLE);
        recyclerViewRecordings.setVisibility(View.GONE);
        }

        }

private void setAdaptertoRecyclerView() {
        recordingAdapter = new RecordingAdapter(this,recordingArraylist);
        recyclerViewRecordings.setAdapter(recordingAdapter);
        }

@Override
public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()){

        case android.R.id.home:
        this.finish();
        return true;
default:
        return super.onOptionsItemSelected(item);

        }

        }

        }
        */