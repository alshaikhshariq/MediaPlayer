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
import com.google.android.gms.common.internal.service.Common;
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
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;


public class MainActivity extends AppCompatActivity implements View.OnClickListener
{
    private Button              viewRecordings;
    private ImageView           imageViewRecord,
                                imageViewPlay,
                                imageViewStop;

    private MediaRecorder       mRecorder;
    private MediaPlayer         mPlayer;
    private Chronometer         chronometer;

    private boolean             isPlaying   =   false;
    private String              fileName    =   null;

    private DatabaseReference   mDatabase;
    private StorageReference    mStorageRef;
    private StorageReference    filepath;

    private File                file;
    private int                 RECORD_AUDIO_REQUEST_CODE   =   123;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Function for initializing all the views
        viewInit();

        //Function for calling all the views
        viewCalls();

        //Use the support library version ContextCompat.checkSelfPermission(...) to avoid
        // checking the build version since Context.checkSelfPermission(...) is only available
        // in Marshmallow
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            getPermissionToRecordAudio();
        }


    }

    //View Initialization function
    private void viewInit()
    {
        viewRecordings  =   findViewById(R.id.view_recordings_);
        chronometer     =   findViewById(R.id.chronotimer_);

        //Setting system clock on chronometer
        chronometer.setBase(SystemClock.elapsedRealtime());

        imageViewRecord     =   findViewById(R.id.record_icon_);
        imageViewStop       =   findViewById(R.id.stop_icon_);
        imageViewPlay       =   findViewById(R.id.play_icon_);

        //Disabling "Play" and "Stop" button on ViewCreate
        imageViewPlay.setVisibility(View.GONE);
        imageViewStop.setVisibility(View.GONE);
    }

    //View Calling function
    private void viewCalls()
    {
        imageViewRecord.setOnClickListener(this);
        imageViewStop.setOnClickListener(this);
        imageViewPlay.setOnClickListener(this);

        //onClickListener {...} for Recording Button
        viewRecordings.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Intent intent = new Intent(MainActivity.this, ListRecordings.class);
                startActivity(intent);
            }
        });

        //Getting instances for FirebaseDatabase and FirebaseStorage
        mDatabase       =   FirebaseDatabase.getInstance().getReference();
        mStorageRef     =   FirebaseStorage.getInstance().getReference();
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
                Toast.makeText(this, "Record Audio permission granted", Toast.LENGTH_SHORT).show();
            }
            else
                {
                    Toast.makeText(this, "You must give permissions to use this app. App is exiting.", Toast.LENGTH_SHORT).show();
                    finishAffinity();
                }
        }

    }

    //onClick {...} function for all the buttons
    @Override
    public void onClick(View v)
    {
        if( v == imageViewRecord )
        {
            //Function call for prepare to record
            prepareForRecording();

            //Function call for start recording
            startRecording();
        }
        else if( v == imageViewStop )
        {
            //Function call for prepare to record
            prepareforStop();

            //Function call for stop recording
            stopRecording();
        }
        else if( v == imageViewPlay )
        {
            //Checking if the recorded file is playing
            if( !isPlaying && fileName != null )
            {
                isPlaying = true;

                //Function call for start playing
                startPlaying();
            }
            else
                {
                    isPlaying = false;

                    //Function call for stop playing
                    stopRecording();
                }
        }
    }

    //Function for prepare recording
    private void prepareForRecording()
    {
        imageViewRecord.setVisibility(View.GONE);
        imageViewStop.setVisibility(View.VISIBLE);
    }

    //Function for start recording
    private void startRecording()
    {
        imageViewStop.setVisibility(View.VISIBLE);

        //Calling the built-in MediaRecorder class
        mRecorder = new MediaRecorder();
        mRecorder.setAudioSource(MediaRecorder.AudioSource.VOICE_RECOGNITION);
        mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mRecorder.setOutputFile(fileName);
        mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
        mRecorder.setAudioEncodingBitRate(96000);
        mRecorder.setAudioSamplingRate(44100);

        //Setting the environment variable for class
        File root   =   android.os.Environment.getExternalStorageDirectory();
        file        =   new File(root.getAbsolutePath() + "/VoiceRecorder/Audios");

        //Checking if the file exist or not
        if (!file.exists())
        {
            file.mkdirs();
        }

        //Setting the path to save the recorded audio in device
        fileName =  root.getAbsolutePath() + "/VoiceRecorder/Audios/" + System.currentTimeMillis() + ".mp3";
        Log.d("filename",file.toString());

        //Catching the exception, if any...
        try
        {
            mRecorder.prepare();
            mRecorder.start();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        //Function call for
        stopRecodring();

        //starting the chronometer
        chronometer.setBase(SystemClock.elapsedRealtime());
        chronometer.start();
    }

    //Function for stop recording
    private void stopRecodring()
    {
        //Catching the exception, if any...
        try
        {
            mPlayer.release();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        mPlayer = null;
        chronometer.stop();
    }

    //Function for start playing
    private void startPlaying()
    {
        //Calling the built-in class for MediaPlayer
        mPlayer = new MediaPlayer();
        Log.d("instartPlaying",fileName);

        //Catching the exception, if any...
        try
        {
            mPlayer.setDataSource(fileName);
            mPlayer.prepare();
            mPlayer.start();
        }
        catch (IOException e)
        {
            Log.e("LOG_TAG", "prepare() failed");
        }

        chronometer.start();

        /** once the audio is complete, timer will stop here**/

        //Setting the MediaPlayer onCompleteListener {...}
        mPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener()
        {
            @Override
            public void onCompletion(MediaPlayer mp)
            {
                imageViewPlay.setImageResource(R.drawable.ic_play);
                isPlaying = false;
                chronometer.stop();
            }
        });
    }

    //Function for start recording
    private void stopRecording()
    {
        //Catching the exception, if any...
        try
        {
            mRecorder.stop();
            mRecorder.release();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        mRecorder = null;

        //starting the chronometer
        chronometer.stop();
        chronometer.setBase(SystemClock.elapsedRealtime());

        //Calling the function to upload the file into FirebaseStorage
        uploadFile();

        Toast.makeText(this, "Voice has been recorded.", Toast.LENGTH_SHORT).show();
    }

    //Function for preparing to stop record
    private void prepareforStop()
    {
        imageViewRecord.setVisibility(View.VISIBLE);
        imageViewStop.setVisibility(View.GONE);
    }


    private void uploadFile()
    {
        //Setting the filePath for FirebaseStorage
        filepath     =  mStorageRef.child("Uploads/").child(fileName);

        //Converting the path into Uniform resource Identifier (Makes the URL of the FirebaseStorage path)
        Uri uri     =   Uri.fromFile((new File(fileName)));

        //Saving the file with taskContinuation in FirebaseDatabse
        filepath.putFile(uri).continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>()
        {
            @Override
            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception
            {
                if (!task.isSuccessful())
                {
                    throw Objects.requireNonNull(task.getException());
                }
                return filepath.getDownloadUrl();
            }

            //Adding the onCompleteListener {...} following the upload task
        }).addOnCompleteListener(new OnCompleteListener<Uri>()
        {
            @Override
            public void onComplete(@NonNull Task<Uri> task)
            {
                final Uri downUri = task.getResult();

                mDatabase.child("Name").setValue(downUri.toString()).addOnCompleteListener(new OnCompleteListener<Void>()
                {
                    @Override
                    public void onComplete(@NonNull Task<Void> task)
                    {
                        // img_profilesetup3.setImageURI(imageUri);
                        // mProgressDialog.dismiss();
                        Toast.makeText(MainActivity.this, "Successfully uploaded", Toast.LENGTH_SHORT).show();

                    }
                });

               /* mDatabase.setValue(downUri.toString()).addOnCompleteListener(new OnCompleteListener<Void>()
                {
                    @Override
                    public void onComplete(@NonNull Task<Void> task)
                    {
                        Map newAudio = new HashMap();
                        newAudio.put("Voice", downUri.toString());
                        mDatabase.updateChildren(newAudio);
                        Toast.makeText(MainActivity.this, "Uploaded Successfully...", Toast.LENGTH_SHORT).show();

                        finish();
                    }
                });*/


               /* if (task.isSuccessful())
                {
                    final Uri downUri = task.getResult();

                    //Saving the URL in FirebaseDatabase
                    assert downUri != null;
                    mDatabase.setValue(downUri.toString()).addOnCompleteListener(new OnCompleteListener<Void>()
                    {
                        @Override
                        public void onComplete(@NonNull Task<Void> task)
                        {
                            Map newAudio = new HashMap();
                            newAudio.put("Voice", downUri.toString());
                            mDatabase.updateChildren(newAudio);
                            Toast.makeText(MainActivity.this, "Uploaded Successfully...", Toast.LENGTH_SHORT).show();

                            finish();
                        }
                    });
                }
                else
                    {
                        Toast.makeText(MainActivity.this, "Error In Uploading...", Toast.LENGTH_SHORT).show();
                    }*/
            }
        });
    }
}
