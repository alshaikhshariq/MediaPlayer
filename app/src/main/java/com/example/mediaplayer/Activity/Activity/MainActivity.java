package com.example.mediaplayer.Activity.Activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.SystemClock;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.appcompat.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.mediaplayer.R;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.IOException;
import java.util.Objects;


public class MainActivity extends AppCompatActivity implements View.OnClickListener
{
    private Button          viewRecordings;
    private MediaRecorder   mRecorder;
    private MediaPlayer     mPlayer;

    private Chronometer     chronometer;
    private ImageView       imageViewRecord,
                            imageViewPlay,
                            imageViewStop;

    private boolean         isPlaying   = false;
    private String          fileName    = null;
    private String          rawFile;

    private DatabaseReference   mDatabase;
    private StorageReference    mStorageRef;
    private StorageReference    filepath;
    private FirebaseAuth        mAuth;


    private File file;
    private String userID;

    private int RECORD_AUDIO_REQUEST_CODE =123;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        viewInit();
        viewCalls();

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            getPermissionToRecordAudio();
        }


    }

    private void viewInit()
    {
        viewRecordings  =   findViewById(R.id.view_recordings_);

        chronometer     =   findViewById(R.id.chronotimer_);
        chronometer.setBase(SystemClock.elapsedRealtime());

        imageViewRecord     =   findViewById(R.id.record_icon_);
        imageViewStop       =   findViewById(R.id.stop_icon_);
        //imageViewPlay     =   findViewById(R.id.imageViewPlay);

        imageViewRecord.setOnClickListener(this);
        imageViewStop.setOnClickListener(this);
        //imageViewPlay.setOnClickListener(this);

        mAuth           =   FirebaseAuth.getInstance();
        mDatabase       =   FirebaseDatabase.getInstance().getReference();
        mStorageRef     =   FirebaseStorage.getInstance().getReference();

    }

    private void viewCalls()
    {
        viewRecordings.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Intent intent = new Intent(MainActivity.this, ListRecordings.class);
                startActivity(intent);
            }
        });



    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public void getPermissionToRecordAudio()
    {
        // 1) Use the support library version ContextCompat.checkSelfPermission(...) to avoid
        // checking the build version since Context.checkSelfPermission(...) is only available
        // in Marshmallow
        // 2) Always check for permission (even if permission has already been granted)
        // since the user can revoke permissions at any time through Settings
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED )
        {
            // The permission is NOT already granted.
            // Check if the user has been asked about this permission already and denied
            // it. If so, we want to give more explanation about why the permission is needed.
            // Fire off an async request to actually get the permission
            // This will show the standard permission request dialog UI
            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.RECORD_AUDIO, Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    RECORD_AUDIO_REQUEST_CODE);
        }
    }

    // Callback with the request from calling requestPermissions(...)
    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        // Make sure it's our original READ_CONTACTS request
        if (requestCode == RECORD_AUDIO_REQUEST_CODE) {
            if (grantResults.length     == 3 &&
                    grantResults[0]     == PackageManager.PERMISSION_GRANTED
                    && grantResults[1]  == PackageManager.PERMISSION_GRANTED
                    && grantResults[2]  == PackageManager.PERMISSION_GRANTED){

                //Toast.makeText(this, "Record Audio permission granted", Toast.LENGTH_SHORT).show();

            } else {
                Toast.makeText(this, "You must give permissions to use this app. App is exiting.", Toast.LENGTH_SHORT).show();
                finishAffinity();
            }
        }

    }


    @Override
    public void onClick(View v)
    {
        if( v == imageViewRecord )
        {
            prepareforRecording();
            startRecording();
        }
        else if( v == imageViewStop )
        {
            prepareforStop();
            stopRecording();
        }
        else if( v == imageViewPlay )
        {
            if( !isPlaying && fileName != null )
            {
                isPlaying = true;
                startPlaying();
            }
            else
                {
                    isPlaying = false;
                    stopPlaying();
                }
        }
    }

    private void stopPlaying()
    {
        try{
            mPlayer.release();
        }catch (Exception e){
            e.printStackTrace();
        }
        mPlayer = null;
        //showing the play button
        //imageViewPlay.setImageResource(R.drawable.ic_play);
        chronometer.stop();
    }

    private void startPlaying()
    {
        mPlayer = new MediaPlayer();
        Log.d("instartPlaying",fileName);
        try {
            mPlayer.setDataSource(fileName);
            mPlayer.prepare();
            mPlayer.start();
        } catch (IOException e) {
            Log.e("LOG_TAG", "prepare() failed");
        }
        //making the imageview pause button
        imageViewPlay.setImageResource(R.drawable.ic_pause);

        //seekBar.setProgress(lastProgress);
        //mPlayer.seekTo(lastProgress);
        //seekBar.setMax(mPlayer.getDuration());
        //seekUpdation();
        chronometer.start();


/** once the audio is complete, timer is stopped here**/

        mPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                imageViewPlay.setImageResource(R.drawable.ic_play);
                isPlaying = false;
                chronometer.stop();
            }
        });
    }

    private void stopRecording()
    {
        try{
            mRecorder.stop();
            mRecorder.release();
        }catch (Exception e){
            e.printStackTrace();
        }
        mRecorder = null;
        //starting the chronometer
        chronometer.stop();
        chronometer.setBase(SystemClock.elapsedRealtime());
        uploadFile();
        //showing the play button
        //Toast.makeText(this, "Recording saved successfully.", Toast.LENGTH_SHORT).show();
    }

    private void prepareforStop()
    {
        //TransitionManager.beginDelayedTransition(linearLayoutRecorder);
        imageViewRecord.setVisibility(View.VISIBLE);
        imageViewStop.setVisibility(View.GONE);
        //linearLayoutPlay.setVisibility(View.VISIBLE);
    }

    private void startRecording()
    {
        mRecorder = new MediaRecorder();
        mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        //mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
        mRecorder.setAudioEncodingBitRate(196000);
        mRecorder.setAudioSamplingRate(44100);



        File root = android.os.Environment.getExternalStorageDirectory();
        file = new File(root.getAbsolutePath() + "/VoiceRecorder/Audios");
        if (!file.exists()) {
            file.mkdirs();
        }

        fileName =  root.getAbsolutePath() + "/VoiceRecorder/Audios/" + System.currentTimeMillis() + ".mp3";
        Log.d("filename",file.toString());

        mRecorder.setOutputFile(fileName);
        mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
        //mRecorder.setAudioEncoder(MediaRecorder.getAudioSourceMax());


        try {
            mRecorder.prepare();
            mRecorder.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
        //lastProgress = 0;
        //seekBar.setProgress(0);
        stopPlaying();
        //starting the chronometer
        chronometer.setBase(SystemClock.elapsedRealtime());
        chronometer.start();
    }

    private void prepareforRecording()
    {
        //TransitionManager.beginDelayedTransition(linearLayoutRecorder);
        imageViewRecord.setVisibility(View.GONE);
        imageViewStop.setVisibility(View.VISIBLE);
        //linearLayoutPlay.setVisibility(View.GONE);
    }

    private void uploadFile()
    {
        //userID = Objects.requireNonNull(mAuth.getCurrentUser()).getUid();
        filepath     =  mStorageRef.child("Uploads/").child(fileName);
        rawFile     =   Environment.getExternalStorageDirectory().getAbsolutePath() + fileName;

       // rawFile = mStorageRef.child("profile_images/").child("audio");
        Uri uri     =   Uri.fromFile((new File(fileName)));


        filepath.putFile(uri).continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
            @Override
            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                if (!task.isSuccessful()) {
                    throw task.getException();
                }
                return filepath.getDownloadUrl();
            }
        }).addOnCompleteListener(new OnCompleteListener<Uri>() {
            @Override
            public void onComplete(@NonNull Task<Uri> task) {

                if (task.isSuccessful()) {
                    Uri downUri = task.getResult();

                    // setting url in user node

                    mDatabase.child("Name").setValue(downUri.toString()).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                           // img_profilesetup3.setImageURI(imageUri);
                           // mProgressDialog.dismiss();
                            Toast.makeText(MainActivity.this, "Successfully uploaded", Toast.LENGTH_SHORT).show();

                        }
                    });
                } else {

                  //  mProgressDialog.dismiss();
                    Toast.makeText(MainActivity.this, "Error In Uploading...", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }





}
