package com.example.mediaplayer.Activity.Activity;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
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
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.util.ArrayList;

public class ListRecordings extends AppCompatActivity
{
    private RecyclerView        recyclerViewRecordings;
    private ArrayList<Model>    recordingArraylist;
    private RecordingAdapter    recordingAdapter;
    private TextView            textViewNoRecordings;

    private FirebaseStorage mStorage;
    private StorageReference mStorageRefURL;
    private StorageReference mStorageRef;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_recordings);

        //Function for initializing all the views
        ViewInit();

        //Function for calling all the views
        ViewCalls();

        //Function for fetching the recordings from device
        fetchRecordings();

        //Function for downloading all the recorded files from FirebaseStorage
        downloadFile();
    }

    //View Initialization function
    @SuppressLint("WrongConstant")
    private void ViewInit()
    {
        recordingArraylist  =   new ArrayList<Model>();

        /* enabling back button */
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        /*setting up recyclerView*/
        recyclerViewRecordings  =   findViewById(R.id.recyclerViewRecordings);
        textViewNoRecordings    =   findViewById(R.id.textViewNoRecordings);

        mStorageRefURL     =    FirebaseStorage.getInstance().getReference("https://mediaplayer-d1042.firebaseio.com/");
    }

    @SuppressLint("WrongConstant")
    private void ViewCalls()
    {

        recyclerViewRecordings.setLayoutManager(new LinearLayoutManager(this,LinearLayoutManager.VERTICAL, false));
        recyclerViewRecordings.setHasFixedSize(true);
    }

    //Function for fetching the recordings from device
    private void fetchRecordings()
    {
        //Creating the environment variable for file
        File root = android.os.Environment.getExternalStorageDirectory();

        //Setting the file path of device
        String path = root.getAbsolutePath() + "/VoiceRecorder/Audios";
        Log.d("Files", "Path: " + path);

        //Creating the fileDirectory
        File directory = new File(path);

        //Making array list of all the files.
        File[] files = directory.listFiles();
        Log.d("Files", "Size: "+ files.length);


        if( files!=null )
        {
            //Getting all the files in an array list
            for (int i = 0; i < files.length; i++)
            {
                Log.d("Files", "FileName:" + files[i].getName());
                String fileName = files[i].getName();
                String recordingUri = root.getAbsolutePath() + "/VoiceRecorder/Audios/" + fileName;

                Model recording = new Model(recordingUri,fileName,false);
                recordingArraylist.add(recording);
            }

            textViewNoRecordings.setVisibility(View.GONE);
            recyclerViewRecordings.setVisibility(View.VISIBLE);

            //Calling the Function for Recycler View Adapter
            setAdaptertoRecyclerView();

        }
        else
            {
                textViewNoRecordings.setVisibility(View.VISIBLE);
                recyclerViewRecordings.setVisibility(View.GONE);
            }
    }

    //Function for Recycler View Adapter
    private void setAdaptertoRecyclerView()
    {
        recordingAdapter = new RecordingAdapter(this,recordingArraylist);
        recyclerViewRecordings.setAdapter(recordingAdapter);
    }

    //Function for Downloading file from FirebaseStorage
    private void downloadFile()
    {

        mStorageRef        =    mStorageRefURL.child("Uploads").child("storage").child("emulated").child("0").child("VoiceRecorder").child("Audios");

        //Creating the environment variable for file
        File root = android.os.Environment.getExternalStorageDirectory();

        //Setting the file path of device
        String path = root.getAbsolutePath() + "/VoiceRecorder/Audios";
        Log.d("Files", "Path: " + path);

        //Creating the fileDirectory
        File directory = new File(path);

        //Making array list of all the files.
        File[] files = directory.listFiles();
        Log.d("Files", "Size: "+ files.length);

        if( files!=null )
        {
            //Getting all the files in an array list
            for (int i = 0; i < files.length; i++)
            {
                Log.d("Files", "FileName:" + files[i].getName());
                String fileName = files[i].getName();
                String recordingUri = root.getAbsolutePath() + "/VoiceRecorder/Audios/" + fileName;

                Model recording = new Model(recordingUri,fileName,false);
                recordingArraylist.add(recording);
            }

            textViewNoRecordings.setVisibility(View.GONE);
            recyclerViewRecordings.setVisibility(View.VISIBLE);

            //Calling the Function for Recycler View Adapter
            setAdaptertoRecyclerView();

        }
        else
        {
            textViewNoRecordings.setVisibility(View.VISIBLE);
            recyclerViewRecordings.setVisibility(View.GONE);
        }

    /*FirebaseStorage storage = FirebaseStorage.getInstance();
    StorageReference storageRef = storage.getReferenceFromUrl("<your_bucket>");
    StorageReference  islandRef = storageRef.child("file.txt");

    File rootPath = new File(Environment.getExternalStorageDirectory(), "file_name");
    if(!rootPath.exists()) {
        rootPath.mkdirs();
    }

    final File localFile = new File(rootPath,"imageName.txt");

    islandRef.getFile(localFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
        @Override
        public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
            Log.e("firebase ",";local tem file created  created " +localFile.toString());
            //  updateDb(timestamp,localFile.toString(),position);
        }
    }).addOnFailureListener(new OnFailureListener() {
        @Override
        public void onFailure(@NonNull Exception exception) {
            Log.e("firebase ",";local tem file not created  created " +exception.toString());
        }
    });*/
    }
}
