package com.example.mediaplayer.Activity.Adapter;

import android.content.Context;
import android.media.MediaPlayer;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.transition.TransitionManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.example.mediaplayer.Activity.Model.Model;
import com.example.mediaplayer.R;

import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Handler;
import java.util.logging.LogRecord;

public class RecordingAdapter  extends RecyclerView.Adapter<RecordingAdapter.ViewHolder>
{
    private ArrayList<Model> recordingArrayList;
    private Context context;
    private MediaPlayer mPlayer;
    private boolean isPlaying = false;
    private int last_index = -1;


    public RecordingAdapter(Context context, ArrayList<Model> recordingArrayList)
    {
        this.context = context;
        this.recordingArrayList = recordingArrayList;
    }

    @NonNull
    @Override
    public RecordingAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i)
    {
        View view = LayoutInflater.from(context).inflate(R.layout.item_record_list,viewGroup,false);
        return  new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecordingAdapter.ViewHolder viewHolder, int i)
    {
        setUpData(viewHolder,i);
    }

    private void setUpData(ViewHolder viewHolder, int i)
    {
        Model recording = recordingArrayList.get(i);
        viewHolder.textViewName.setText(recording.getFileName());

        if(recording.isPlaying())
        {
            viewHolder.imageViewPlay.setImageResource(R.drawable.ic_pause);
            TransitionManager.beginDelayedTransition((ViewGroup) viewHolder.itemView);
            viewHolder.seekBar.setVisibility(View.VISIBLE);
            viewHolder.seekUpdation(viewHolder);
        }
        else
            {
                viewHolder.imageViewPlay.setImageResource(R.drawable.ic_play);
                TransitionManager.beginDelayedTransition((ViewGroup) viewHolder.itemView);
                viewHolder.seekBar.setVisibility(View.GONE);
            }

        viewHolder.manageSeekBar(viewHolder);
    }

    @Override
    public int getItemCount()
    {
        return recordingArrayList.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder
    {
        private ImageView   imageViewPlay;
        private SeekBar     seekBar;
        private TextView    textViewName;
        private String      recordingUri;
        private int         lastProgress    = 0;


        private Handler mHandler = new Handler()
        {

            @Override
            public void publish(LogRecord record)
            { }

            @Override
            public void flush()
            { }

            @Override
            public void close() throws SecurityException
            { }

        };

        ViewHolder viewHolder;

        ViewHolder(View itemView)
        {
            super(itemView);

            imageViewPlay   = itemView.findViewById(R.id.play_view_);
            seekBar         = itemView.findViewById(R.id.seek_bar_);
            textViewName    = itemView.findViewById(R.id.text_view_recording_name_);

            imageViewPlay.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View view)
                {
                    int position    = getAdapterPosition();
                    Model recording = recordingArrayList.get(position);

                    recordingUri = recording.getUri();

                    if( isPlaying )
                    {
                        stopPlaying();
                        if( position == last_index )
                        {
                            recording.setPlaying(false);
                            stopPlaying();
                            notifyItemChanged(position);
                        }
                        else
                            {
                                markAllPaused();
                                recording.setPlaying(true);
                                notifyItemChanged(position);
                                startPlaying(recording,position);
                                last_index = position;
                            }
                    }
                    else
                        {
                            startPlaying(recording,position);
                            recording.setPlaying(true);
                            seekBar.setMax(mPlayer.getDuration());
                            Log.d("isPlayin","False");
                            notifyItemChanged(position);
                            last_index = position;
                        }
                }

                private void startPlaying(final Model recording, final int position)
                {
                    mPlayer = new MediaPlayer();
                    try
                    {
                        mPlayer.setDataSource(recordingUri);
                        mPlayer.prepare();
                        mPlayer.start();
                    }
                    catch (IOException e)
                    {
                        Log.e("LOG_TAG", "prepare() failed");
                    }

                    //showing the pause button
                    seekBar.setMax(mPlayer.getDuration());
                    isPlaying = true;

                    mPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener()
                    {
                        @Override
                        public void onCompletion(MediaPlayer mp)
                        {
                            recording.setPlaying(false);
                            notifyItemChanged(position);
                        }
                    });
                }

                private void markAllPaused()
                {
                    for( int i=0; i < recordingArrayList.size(); i++ )
                    {
                        recordingArrayList.get(i).setPlaying(false);
                        recordingArrayList.set(i,recordingArrayList.get(i));
                    }
                    notifyDataSetChanged();
                }

               /* Runnable runnable = new Runnable()
                {
                    @Override
                    public void run()
                    {
                        seekUpdation(viewHolder);
                    }

                };*/

                private void stopPlaying()
                {
                    try
                    {
                        mPlayer.release();
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                    mPlayer = null;
                    isPlaying = false;
                }

            });
        }



        void manageSeekBar(ViewHolder viewHolder)
        {
            viewHolder.seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener()
            {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser)
                {
                    if( mPlayer!=null && fromUser )
                    {
                        mPlayer.seekTo(progress);
                    }
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar)
                {
                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar)
                {
                }
            });
        }

        void seekUpdation(ViewHolder viewHolder)
        {
            this.viewHolder = viewHolder;
            if(mPlayer != null)
            {
                int mCurrentPosition = mPlayer.getCurrentPosition() ;
                viewHolder.seekBar.setMax(mPlayer.getDuration());
                viewHolder.seekBar.setProgress(mCurrentPosition);
                lastProgress = mCurrentPosition;
            }
            //mHandler.postDelayed(runnable, 100);
        }
    }
}
