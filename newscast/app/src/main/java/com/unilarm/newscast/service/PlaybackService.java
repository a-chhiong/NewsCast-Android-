package com.unilarm.newscast.service;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.unilarm.newscast.model.RssData;

import java.io.File;
import java.util.Timer;
import java.util.TimerTask;

import static android.media.AudioManager.STREAM_MUSIC;
import static com.unilarm.newscast.handle.DownloadHandle.MEDIA_FOLDER;

public class PlaybackService extends Service
{

    /*ATTRIBUTES*/
    private String fileName;
    private String folderName;
    private String streamLink;

    /*CORE CLASS*/
    private MediaPlayer mediaPlayer;   //Having MediaPlayer in service is to save more resource...
    private AudioAttributes attributes;
    private int position;
    private int duration;
    private boolean positionBroadcastEnabled;
    private boolean everCompleted = false;
    private boolean everRewinded = false;

    /*BROADCAST*/
    private LocalBroadcastManager broadcastManager;
    private BroadcastReceiver broadcastReceiver;

    /*TIMER CONTROL*/
    private Timer timer;
    private TimerTask timerTask;

    public PlaybackService()
    {

    }

    @Override
    public void onCreate() {
        super.onCreate();

        Log.v("PLAYBACK_SERVICE", "onCreate");

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        Log.v("PLAYBACK_SERVICE", "onStartCommand");

        mediaPlayer = null;
        mediaPlayer = new MediaPlayer();
        attributes = null;
        attributes = new AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_MEDIA).setContentType(AudioAttributes.CONTENT_TYPE_MUSIC).build();
        mediaPlayer.setAudioAttributes(attributes);
        mediaPlayer.setLooping(false);

        broadcastManager = LocalBroadcastManager.getInstance(PlaybackService.this);

        if(intent.hasExtra("FILE"))
        {
            fileName = intent.getStringExtra("FILE");
            streamLink = null;

        }
        else if(intent.hasExtra("LINK"))
        {
            streamLink = intent.getStringExtra("LINK");
            fileName = null;
        }

        /*
         *  the downloaded file path is defined in Download Handle !
         *
         * **/

        intent = new Intent("PLAYBACK_ACTIVITY");
        intent.putExtra("PLAYER_STATUS", "LOADING");   //KEY & VALUE mapping...
        broadcastManager.sendBroadcast(intent);

        Log.v("PLAYBACK_SERVICE", "LOADING");

        if(fileName != null)
        {
            Log.v("PLAYBACK_SERVICE", "offline");

            String directory = PlaybackService.this.getFilesDir().getAbsolutePath() + "/" + MEDIA_FOLDER;

            Log.v("PLAYBACK_SERVICE", "Directory: " + directory);

            File file = new File(directory + "/" + fileName);

            Log.v("PLAYBACK_SERVICE", "Path: " + file.toString());

            Uri uri =  Uri.fromFile(file);

            try
            {
                mediaPlayer.setDataSource(PlaybackService.this, uri);
            }
            catch(Exception ex)
            {
                Log.v("PLAYBACK_SERVICE", ex.getMessage());
            }

            mediaPlayer.prepareAsync(); // might take long! (for buffering, etc)
        }
        else if(streamLink != null)
        {
            Log.v("PLAYBACK_SERVICE", "streaming");

            try
            {
                mediaPlayer.setDataSource(streamLink);
            }
            catch(Exception ex)
            {
                Log.v("PLAYBACK_SERVICE", ex.getMessage());
            }

            mediaPlayer.prepareAsync(); // might take long! (for buffering, etc)
        }

        timer = new Timer();

        timerTask = new TimerTask() {
            @Override
            public void run() {

                if(positionBroadcastEnabled) {
                    position = mediaPlayer.getCurrentPosition();

                    Log.v("PLAYBACK_SERVICE", "position: " + position);

                    Intent intent = new Intent("PLAYBACK_ACTIVITY");
                    intent.putExtra("PLAYER_POSITION", position);   //KEY & VALUE mapping...
                    broadcastManager.sendBroadcast(intent);
                }
            }
        };

        timer.schedule(timerTask, 0, 100); /// <-- every 100 ms

        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

                if(intent.hasExtra("PLAYER_CMD"))
                {
                    String command = intent.getStringExtra("PLAYER_CMD");

                    switch (command) {
                        case "PLAY":
                            Log.v("PLAYBACK_SERVICE", "PLAY");
                            mediaPlayer.start();
                            positionBroadcastEnabled = true;
                            break;
                        case "PAUSE":
                            Log.v("PLAYBACK_SERVICE", "PAUSE");
                            mediaPlayer.pause();
                            positionBroadcastEnabled = false;
                            break;
                        case "STOP":
                            Log.v("PLAYBACK_SERVICE", "STOP");
                            mediaPlayer.pause();
                            mediaPlayer.seekTo(0);
                            positionBroadcastEnabled = false;
                            break;
                    }
                }
                else if(intent.hasExtra("PLAYER_SEEK"))
                {
                    int position = intent.getIntExtra("PLAYER_SEEK", -1);

                    if(position > 0)
                    {
                        Log.v("PLAYBACK_SERVICE", "SEEK: " + position);

                        mediaPlayer.seekTo(position);
                    }
                }
            }
        };

        broadcastManager.registerReceiver(broadcastReceiver, new IntentFilter("PLAYBACK_SERVICE"));

        /*
         *  https://developer.android.com/images/mediaplayer_state_diagram.gif
         * */

        mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mediaPlayer) {

                Log.v("PLAYBACK_SERVICE", "READY");

                Intent intent = new Intent("PLAYBACK_ACTIVITY");
                intent.putExtra("PLAYER_STATUS", "READY");   //KEY & VALUE mapping...
                broadcastManager.sendBroadcast(intent);

                duration = mediaPlayer.getDuration();

                Log.v("PLAYBACK_SERVICE", "duration: " + duration);

                intent = new Intent("PLAYBACK_ACTIVITY");
                intent.putExtra("PLAYER_DURATION", duration);   //KEY & VALUE mapping...
                broadcastManager.sendBroadcast(intent);
            }
        });

        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {

                positionBroadcastEnabled = false;

                everCompleted = true;

                Log.v("PLAYBACK_SERVICE", "COMPLETED");

                Intent intent = new Intent("PLAYBACK_ACTIVITY");
                intent.putExtra("PLAYER_STATUS", "COMPLETED");   //KEY & VALUE mapping...
                broadcastManager.sendBroadcast(intent);

                mediaPlayer.seekTo(0);
            }
        });

        mediaPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mediaPlayer, int i, int i1) {

                Log.v("PLAYBACK_SERVICE", "ERROR");

                Intent intent = new Intent("PLAYBACK_ACTIVITY");
                intent.putExtra("PLAYER_STATUS", "UNAVAILABLE");   //KEY & VALUE mapping...
                broadcastManager.sendBroadcast(intent);

                mediaPlayer.reset();

                return true;
            }
        });

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        Log.v("PLAYBACK_SERVICE", "onDestroy");

        mediaPlayer.reset();

        mediaPlayer = null;

        attributes = null;

        broadcastManager.unregisterReceiver(broadcastReceiver);

        timer.cancel();
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");

    }
}
