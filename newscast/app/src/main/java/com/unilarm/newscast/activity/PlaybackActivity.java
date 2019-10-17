package com.unilarm.newscast.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.webkit.URLUtil;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;

import com.unilarm.newscast.R;
import com.unilarm.newscast.handle.DaoHandle;
import com.unilarm.newscast.handle.DownloadHandle;
import com.unilarm.newscast.handle.HtmlHandle;
import com.unilarm.newscast.model.RssData;
import com.unilarm.newscast.service.PlaybackService;

import java.io.File;
import java.util.Timer;
import java.util.TimerTask;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static com.unilarm.newscast.handle.DaoHandle.ITEMLIST;
import static com.unilarm.newscast.handle.DownloadHandle.MEDIA_FOLDER;

public class PlaybackActivity extends AppCompatActivity {

    /*UI for Content*/
    private ImageView iv_share_playback;
    private ImageView iv_download_playback;
    private ImageView iv_reload_playback;
    private TextView tv_title_playback;
    private TextView tv_parent_playback;
    private TextView tv_category_playback;
    private TextView tv_pubdate_playback;

    /*UI for ACTION*/
    private TextView sv_playback;   /// <-- inside scroll view
    private TextView tv_playback;   /// <-- together with seek bar
    private SeekBar sb_playback;
    private ProgressBar pb_playback;

    /*UI for PLAYER*/
    private ImageView iv_eject_playback;
    private ImageView iv_play_playback;
    private ImageView iv_pause_playback;
    private ImageView iv_stop_playback;

    /* Timer */
    private Timer timer;
    private TimerTask timerTask;

    /*HANDLE*/
    private DaoHandle daoHandle;
    private HtmlHandle htmlHandle;
    private DownloadHandle downloadHandle;

    /*RECEIVER*/
    private LocalBroadcastManager broadcastManager;
    private BroadcastReceiver broadcastReceiver;

    /*DATA*/
    private String rawLink;
    private String webMediaLink;
    private String webScript;
    private String webMediaFile;
    private String localScript;
    private String localMediaFile;
    private RssData itemDataFromEXT = new RssData();

    /*Media Player*/
    private Intent MyMediaPlayer;
    private int duration;
    private int position_player;
    private int position_user;
    private boolean isDownloaded;
    private boolean isDownloading;
    private boolean isPlaying;
    private boolean willStartServiceByLink = false;
    private boolean willStartServiceByFile = false;

    /*constant for Media Player Button*/
    private static final int toPlay = 0;
    private static final int toPause = 1;
    private static final int toStop = 2;
    private static final int toEject = 3;
    private static final int toInsert = 4;

    /*constant for Download Button*/
    private static final int downloadable = 0;
    private static final int downloading = 1;
    private static final int downloaded = 2;
    private static final int unavailable = 3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_playback);

        Log.v("PLAYBACK_ACTIVITY", "onCreate");

        initData();

        initView();

        initHandle();

        initReceiver();

        initLoading();
    }

    @Override
    protected void onRestart() {
        super.onRestart();

        Log.v("PLAYBACK_ACTIVITY", "onRestart");
    }

    @Override
    protected void onStart() {
        super.onStart();

        Log.v("PLAYBACK_ACTIVITY", "onStart");
    }

    @Override
    protected void onResume()
    {
        super.onResume();

        Log.v("PLAYBACK_ACTIVITY", "onResume");

    }

    @Override
    public void onBackPressed() {

        super.onBackPressed();

        Log.v("PLAYBACK_ACTIVITY", "onBackPressed");
    }

    @Override
    protected void onPause() {
        super.onPause();

        Log.v("PLAYBACK_ACTIVITY", "onPause");
    }

    @Override
    protected void onStop() {
        super.onStop();

        Log.v("PLAYBACK_ACTIVITY", "onStop");

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        Log.v("PLAYBACK_ACTIVITY", "onDestroy");

        stopMediaPlayerService();

        broadcastManager.unregisterReceiver(broadcastReceiver);

        if(downloadHandle != null)
        {
            downloadHandle.cancelTask();
            downloadHandle = null;
        }

        if(daoHandle != null) {
            daoHandle = null;
        }

        if(htmlHandle != null) {
            htmlHandle.cancelTask();
            htmlHandle = null;
        }

        setProgressBar(-666);
        setPlayerButton(toEject);
    }

    private void initData()
    {
        Intent intent = getIntent();

        Bundle bundle = intent.getExtras();

        if (bundle != null)
        {
            itemDataFromEXT = (RssData) bundle.getSerializable("RSS_DATA_OBJECT");
        }
    }

    private void initView() {

        /* RssData Show */

        iv_share_playback = findViewById(R.id.iv_share_playback);
        iv_download_playback = findViewById(R.id.iv_download_playback);
        tv_title_playback = findViewById(R.id.tv_title_playback);
        tv_parent_playback = findViewById(R.id.tv_parent_playback);
        tv_category_playback = findViewById(R.id.tv_category_playback);
        tv_pubdate_playback = findViewById(R.id.tv_pubdate_playback);
        sv_playback = findViewById(R.id.sv_playback);

        /* Control Bar Show */

        pb_playback = findViewById(R.id.pb_playback);
        sb_playback = findViewById(R.id.sb_playback);
        tv_playback = findViewById(R.id.tv_playback);

        iv_eject_playback = findViewById(R.id.iv_eject_playback);
        iv_play_playback = findViewById(R.id.iv_play_playback);
        iv_pause_playback = findViewById(R.id.iv_pause_playback);
        iv_stop_playback = findViewById(R.id.iv_stop_playback);

        iv_reload_playback = findViewById(R.id.iv_reload_playback);

        /* RssData initialised */

        if(itemDataFromEXT != null)
        {
            String title = itemDataFromEXT.getTitle();
            String parent = itemDataFromEXT.getParent();
            String link = itemDataFromEXT.getLink();
            String category = itemDataFromEXT.getCategory();
            String pubdate = itemDataFromEXT.getPubdate();
            String media = itemDataFromEXT.getMedia();
            String script = itemDataFromEXT.getScript();

            localScript = script;
            localMediaFile = media;
            rawLink = link;
            
            tv_title_playback.setText(title);
            tv_parent_playback.setText(parent);
            tv_category_playback.setText(category);
            tv_pubdate_playback.setText(pubdate);

            if(localScript != null && localScript.length() > 0)
            {
                sv_playback.setText(localScript);
            }
        }
    }

    private void initHandle()
    {
        iv_share_playback.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(isPlaying)
                {
                    setPlayerButton(toPause);

                    Log.v("PLAYBACK_ACTIVITY", "PAUSE");

                    Intent intent = new Intent("PLAYBACK_SERVICE");  //action works like certain freq. to the radio player...
                    intent.putExtra("PLAYER_CMD", "PAUSE");   //KEY & VALUE mapping...
                    broadcastManager.sendBroadcast(intent);    //cf. startActivity()...
                }
                
                // Always use string resources for UI text. This says something like "Share this photo with"
                
                String title = (String) getResources().getText(R.string.open_in);

                /* https://developer.android.com/training/sharing/send */

//                Intent send = new Intent();
//                send.setAction(Intent.ACTION_SEND);
//                send.putExtra(Intent.EXTRA_TEXT, rawLink);
//                send.setType("text/plain");
//                startActivity(Intent.createChooser(send, title) );

                /* https://stackoverflow.com/questions/17400621/show-browser-list-when-opening-a-link-in-android */


                Log.v("PLAYBACK_ACTIVITY", "Share Button, Raw Link: "+ rawLink);

                Intent send = new Intent(Intent.ACTION_VIEW);
                send.setData(Uri.parse(rawLink));
                Intent chooser = Intent.createChooser(send, title);
                startActivity(chooser);
            }
        });

        iv_download_playback.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                setDownloadButton(downloading);

                setReloadButton(false);
                setShareButton(false);

                setPlayerButton(toEject);
                
                stopMediaPlayerService();

//                RssData data = new RssData();
//
//                data.setLink(webMediaLink);
//                data.setMedia(webMediaFile);

                Log.v("PLAYBACK_ACTIVITY", "Download Button, Link: "+ webMediaLink);
                Log.v("PLAYBACK_ACTIVITY", "Download Button, File : "+ webMediaFile);

                downloadHandle = null;
                downloadHandle = new DownloadHandle(PlaybackActivity.this, webMediaLink, webMediaFile, "PLAYBACK_ACTIVITY");
//                downloadHandle = new DownloadHandle(PlaybackActivity.this, data, "PLAYBACK_ACTIVITY");
                downloadHandle.proceedTask();
            }
        });

        iv_reload_playback.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Log.v("PLAYBACK_ACTIVITY", "Reload Button");

                setReloadButton(false);

                setShareButton(false);

                setDownloadButton(unavailable);

                setPlayerButton(toEject);

                stopMediaPlayerService();

                reLoading();
            }
        });

        sb_playback.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

                if(fromUser)
                {
                    position_user = progress;
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

                Log.v("PLAYBACK_ACTIVITY", "Seek Button, SEEK: " + position_user);

                Intent intent = new Intent("PLAYBACK_SERVICE");  //action works like certain freq. to the radio player...
                intent.putExtra("PLAYER_SEEK", position_user);   //KEY & VALUE mapping...
                broadcastManager.sendBroadcast(intent);    //cf. startActivity()...
            }
        });

        iv_eject_playback.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v) {

                Log.v("PLAYBACK_ACTIVITY", "EJECT");

                finish();
            }
        });

        iv_play_playback.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v) {

                Log.v("PLAYBACK_ACTIVITY", "PLAY");

                setPlayerButton(toPlay);

                Intent intent = new Intent("PLAYBACK_SERVICE");  //package path, self defined or follow your JAVA hierarchy ...
                intent.putExtra("PLAYER_CMD", "PLAY");   //KEY & VALUE mapping...
                broadcastManager.sendBroadcast(intent);    //cf. startActivity()...
            }
        });

        iv_pause_playback.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Log.v("PLAYBACK_ACTIVITY", "PAUSE");

                setPlayerButton(toPause);

                Intent intent = new Intent("PLAYBACK_SERVICE");  //action works like certain freq. to the radio player...
                intent.putExtra("PLAYER_CMD", "PAUSE");   //KEY & VALUE mapping...
                broadcastManager.sendBroadcast(intent);    //cf. startActivity()...
            }
        });

        iv_stop_playback.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Log.v("PLAYBACK_ACTIVITY", "STOP");

                setPlayerButton(toStop);

                sb_playback.setProgress(position_user = 0);

                Intent intent = new Intent("PLAYBACK_SERVICE");
                intent.putExtra("PLAYER_CMD", "STOP");   //KEY & VALUE mapping...
                broadcastManager.sendBroadcast(intent);    //cf. startActivity()...
            }
        });
    }

    private void initReceiver()
    {
        broadcastManager = LocalBroadcastManager.getInstance(PlaybackActivity.this);

        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

                if(intent.hasExtra("HTML_HANDLE"))
                {
                    String status = intent.getStringExtra("HTML_HANDLE");

                    Log.v("PLAYBACK_ACTIVITY", "HTML_HANDLE: " + status);

                    showMessage(status);
                    setReloadButton(false);
                    setShareButton(false);
                    setDownloadButton(unavailable);
                    setProgressBar(-666);
                    setPlayerButton(toEject);

                    switch(status)
                    {
                        case "NOTHING_FETCHED":
                            setReloadButton(true);
                            showMessage(getString(R.string.not_supported));
                            break;
                        case "COMPLETELY_FETCHED":
                            webScript = htmlHandle.getWebScript();
                            webMediaLink = htmlHandle.getWebMediaLink();
                            webMediaFile = htmlHandle.getWebMediaFile();

                            localScript = webScript;

                            //localMediaFile = webMediaFile;\

                            sv_playback.setText(localScript);   /// <---
                            willStartServiceByLink = true;
                            willStartServiceByFile = false;

                            showMessage(null);
                            setProgressBar(666);

                            itemDataFromEXT.setScript(localScript);
                            daoHandle = null;
                            daoHandle = new DaoHandle(PlaybackActivity.this, ITEMLIST, "PLAYBACK_ACTIVITY");
                            daoHandle.updateData(itemDataFromEXT);
                            break;
                        case "ONLY_MEDIA_FETCHED":
                            webMediaLink = htmlHandle.getWebMediaLink();
                            webMediaFile = htmlHandle.getWebMediaFile();

                            if(localScript == null || localScript.length() == 0)
                            {
                                localScript = "No Content Provided";
                            }
                            localMediaFile = webMediaFile;

                            sv_playback.setText(localScript);   /// <---

                            willStartServiceByLink = true; /// because play it directly...
                            willStartServiceByFile = false;

                            showMessage(null);
                            setProgressBar(666);

                            startOnlineMediaPlayerService(webMediaLink);
                            break;
                        case "ONLY_SCRIPT_FETCHED":
                            webScript = htmlHandle.getWebScript();
                            localScript = webScript;

                            sv_playback.setText(localScript);   /// <---

                            showMessage(getString(R.string.only_text_available));
                            setProgressBar(666);

                            willStartServiceByLink = false;
                            willStartServiceByFile = false;

                            itemDataFromEXT.setScript(localScript);
                            daoHandle = null;
                            daoHandle = new DaoHandle(PlaybackActivity.this, ITEMLIST, "PLAYBACK_ACTIVITY");
                            daoHandle.updateData(itemDataFromEXT);
                            break;
                    }

                    Log.v("PLAYBACK_ACTIVITY",status);
                }
                else if(intent.hasExtra("DAO_HANDLE"))
                {
                    String status = intent.getStringExtra("DAO_HANDLE");

                    Log.v("PLAYBACK_ACTIVITY", "DAO_HANDLE: " + status);

                    showMessage(status);
                    setReloadButton(false);
                    setShareButton(false);
                    setDownloadButton(unavailable);
                    setProgressBar(-666);
                    setPlayerButton(toEject);

                    switch(status)
                    {
                        case "UPDATED":
                        case "INSERTED_INSTEAD":
                            if(willStartServiceByLink)
                            {
                                showMessage(null);
                                setDownloadButton(downloadable);

                                Log.v("PLAYBACK_ACTIVITY", "WebMediaLink: " + webMediaLink);

                                stopMediaPlayerService();
                                startOnlineMediaPlayerService(webMediaLink);
                            }
                            else if(willStartServiceByFile)
                            {
                                showMessage(null);
                                setDownloadButton(downloaded);

                                Log.v("PLAYBACK_ACTIVITY", "LocalMediaFile: " + localMediaFile);

                                stopMediaPlayerService();
                                startOfflineMediaPlayerService(localMediaFile);
                            }
                            else
                            {
                                showMessage("unavailable");

                                setReloadButton(true);

                                Log.v("PLAYBACK_SERVICE", "IDLING");
                            }
                            break;
                    }
                }
                else if(intent.hasExtra("DOWNLOAD_STATUS"))
                {
                    String status = intent.getStringExtra("DOWNLOAD_STATUS");

                    Log.v("PLAYBACK_ACTIVITY", "DOWNLOAD_STATUS: " + status);

                    showMessage(status);
                    setPlayerButton(toEject);
                    setShareButton(false);
                    setReloadButton(false);
                    setProgressBar(-666);

                    switch(status)
                    {
                        case "File_Downloaded":
                        case "File_Existed":
                            localMediaFile = webMediaFile;
                            Log.v("PLAYBACK_ACTIVITY", "LocalMediaFile: " + localMediaFile);
                            willStartServiceByLink = false;
                            willStartServiceByFile = true;
                            showMessage(null);
                            setProgressBar(666);
                            itemDataFromEXT.setMedia(localMediaFile);
                            daoHandle = null;
                            daoHandle = new DaoHandle(PlaybackActivity.this, ITEMLIST, "PLAYBACK_ACTIVITY");
                            daoHandle.updateData(itemDataFromEXT);
                            break;
                        case "Directory_Error":
                        case "Download_Cancelled":
                            setDownloadButton(downloadable);
                            if(willStartServiceByLink)
                            {
                                showMessage(null);
                                startOnlineMediaPlayerService(webMediaLink);
                            }
                            break;
                        default:
                            setReloadButton(true);
                            setDownloadButton(unavailable);
                            break;
                    }
                }
                else if(intent.hasExtra("DOWNLOAD_PROGRESS"))
                {
                    int progress = intent.getIntExtra("DOWNLOAD_PROGRESS", -1);

                    Log.v("PLAYBACK_ACTIVITY", "DOWNLOAD_PROGRESS: " + progress);

                    setProgressBar(progress);

                    showMessage(null);
                }
                else if(intent.hasExtra("PLAYER_STATUS"))
                {
                    String status = intent.getStringExtra("PLAYER_STATUS");

                    Log.v("PLAYBACK_ACTIVITY", "PLAYER_STATUS: " + status);

                    setProgressBar(-666);

                    showMessage(null);

                    switch (status) {
                        case "LOADING":
                            setProgressBar(666);
                            setPlayerButton(toEject);
                            break;
                        case "READY":
                            setReloadButton(true);
                            setShareButton(true);
                            setPlayerButton(toInsert);
                            break;
                        case "COMPLETED":
                            setPlayerButton(toStop);
                            break;
                        case "UNAVAILABLE":
                            showMessage(status);
                            setPlayerButton(toEject);
                            stopMediaPlayerService();
                            break;
                    }
                }
                else if(intent.hasExtra("PLAYER_DURATION"))
                {
                    duration = intent.getIntExtra("PLAYER_DURATION", -1);

                    Log.v("PLAYBACK_ACTIVITY", "PLAYER_DURATION: " + duration);

                    if(duration > 0)
                    {
                        sb_playback.setMax(duration);
                    }
                }
                else if(intent.hasExtra("PLAYER_POSITION"))
                {
                    position_player = intent.getIntExtra("PLAYER_POSITION", -1);

                    Log.v("PLAYBACK_ACTIVITY", "PLAYER_POSITION: " + position_player);

                    sb_playback.setProgress(position_player);
                }
            }
        };

        broadcastManager.registerReceiver(broadcastReceiver, new IntentFilter("PLAYBACK_ACTIVITY"));
    }

    private void initLoading()
    {
        setProgressBar(-666);
        showMessage(null);
        setDownloadButton(unavailable);
        setPlayerButton(toEject);
        setReloadButton(false);
        setShareButton(false);

        if(localMediaFile != null && localMediaFile.length() > 0)
        {
            showMessage(getString(R.string.initialising));
            setDownloadButton(downloaded);
            setPlayerButton(toInsert);

            if(!startOfflineMediaPlayerService(localMediaFile))
            {
                showMessage(getString(R.string.fetching));
                showMessage(null);
                setProgressBar(666);
                setDownloadButton(unavailable);
                setPlayerButton(toEject);

                htmlHandle = null;
                htmlHandle = new HtmlHandle(PlaybackActivity.this, itemDataFromEXT, "PLAYBACK_ACTIVITY");
                htmlHandle.proceedTask();
            }
        }
        else
        {
            showMessage(getString(R.string.fetching));
            showMessage(null);
            setProgressBar(666);
            setDownloadButton(unavailable);
            setPlayerButton(toEject);

            htmlHandle = null;
            htmlHandle = new HtmlHandle(PlaybackActivity.this, itemDataFromEXT, "PLAYBACK_ACTIVITY");
            htmlHandle.proceedTask();
        }
    }
    
    private void reLoading()
    {
        showMessage(getString(R.string.fetching));
        showMessage(null);
        setProgressBar(666);
        setDownloadButton(unavailable);
        setPlayerButton(toEject);
        setReloadButton(false);
        setShareButton(false);
        
        willStartServiceByFile = false;
        willStartServiceByLink = false;

        htmlHandle = null;
        htmlHandle = new HtmlHandle(PlaybackActivity.this, itemDataFromEXT, "PLAYBACK_ACTIVITY");
        htmlHandle.proceedTask();
    }

    private void setReloadButton(boolean enabled)
    {
        if (enabled)
        {
            iv_reload_playback.setEnabled(true);
            iv_reload_playback.setAlpha(1.0F);
        }
        else
        {
            iv_reload_playback.setEnabled(false);
            iv_reload_playback.setAlpha(0.5F);
        }
    }

    private void setShareButton(boolean enabled)
    {
        if (enabled)
        {
            iv_share_playback.setEnabled(true);
            iv_share_playback.setAlpha(1.0F);
        }
        else
        {
            iv_share_playback.setEnabled(false);
            iv_share_playback.setAlpha(0.5F);
        }
    }

    private void setDownloadButton(int status)
    {
        isDownloading = false;
        isDownloaded = false;

        switch (status)
        {
            case downloadable:
                iv_download_playback.setEnabled(true);
                iv_download_playback.setImageResource(R.drawable.ic_file_download);
                iv_download_playback.setAlpha(1.0F);
                iv_share_playback.setEnabled(true);
                iv_share_playback.setAlpha(1.0F);
                break;
            case downloading:
                iv_download_playback.setEnabled(false);
                iv_download_playback.setImageResource(R.drawable.ic_arrow_drop_down_circle);
                iv_download_playback.setAlpha(1.0F);
                isDownloading = true;
                iv_share_playback.setEnabled(false);
                iv_share_playback.setAlpha(0.5F);
                break;
            case downloaded:
                iv_download_playback.setEnabled(false);
                iv_download_playback.setImageResource(R.drawable.ic_offline_pin_white);
                iv_download_playback.setAlpha(1.0F);
                isDownloaded = true;
                iv_share_playback.setEnabled(true);
                iv_share_playback.setAlpha(1.0F);
                break;
            case unavailable:
                iv_download_playback.setEnabled(false);
                iv_download_playback.setImageResource(R.drawable.ic_arrow_downward);
                iv_download_playback.setAlpha(0.5F);
                iv_share_playback.setEnabled(false);
                iv_share_playback.setAlpha(0.5F);
                break;
            default:
                iv_download_playback.setEnabled(false);
                iv_download_playback.setImageResource(R.drawable.ic_arrow_downward);
                iv_download_playback.setAlpha(0.5F);
                iv_share_playback.setEnabled(false);
                iv_share_playback.setAlpha(0.5F);
                break;
        }
    }

    private void showMessage(String message)
    {
        if (message != null && message.length() > 0)
        {
            tv_playback.setVisibility(VISIBLE);
            tv_playback.setText(message);
        }
        else
        {
            tv_playback.setVisibility(GONE);
            tv_playback.setText("");
        }
    }

    private void setProgressBar(int progress)
    {
        if (progress > 100)    // automatically
        {
            pb_playback.setMax(10);
            pb_playback.setVisibility(VISIBLE);
            pb_playback.setProgress(0);
            
            timer = new Timer();

            timerTask = new TimerTask() {

                int value = 0;

                @Override
                public void run() {

                    value ++;

                    if(value >= 10)
                    {
                        value = 0;
                    }

                    pb_playback.setProgress(value);
                }
            };

            timer.schedule(timerTask, 0, 1000);
        }
        else if(progress > 0)  //programmatically
        {
            pb_playback.setMax(100);

            if(pb_playback.getVisibility() == GONE) {
                pb_playback.setVisibility(View.VISIBLE);
            }

            pb_playback.setProgress(progress);
        }
        else    //hidden
        {
            pb_playback.setVisibility(GONE);
            pb_playback.setProgress(0);

            if(timer != null && timerTask != null)
            {
                timerTask.cancel();
                timer.cancel();
                
                timer = null;
                timerTask = null;
            }
        }
    }

    private void setPlayerButton(int status) {

        isPlaying = false;
        
        switch(status)
        {
            case toPlay:
                isPlaying = true;
                sb_playback.setVisibility(VISIBLE);
                sb_playback.setEnabled(true);
                iv_play_playback.setEnabled(false);
                iv_pause_playback.setEnabled(true);
                iv_stop_playback.setEnabled(true);
                iv_play_playback.setAlpha(0.3F);
                iv_pause_playback.setAlpha(1.0F);
                iv_stop_playback.setAlpha(1.0F);
                break;
            case toPause:
                sb_playback.setVisibility(VISIBLE);
                sb_playback.setEnabled(true);
                iv_play_playback.setEnabled(true);
                iv_pause_playback.setEnabled(false);
                iv_stop_playback.setEnabled(true);
                iv_play_playback.setAlpha(1.0F);
                iv_pause_playback.setAlpha(0.3F);
                iv_stop_playback.setAlpha(1.0F);
                break;
            case toStop:
                sb_playback.setProgress(0);
                sb_playback.setVisibility(VISIBLE);
                sb_playback.setEnabled(false);
                iv_play_playback.setEnabled(true);
                iv_pause_playback.setEnabled(false);
                iv_stop_playback.setEnabled(false);
                iv_play_playback.setAlpha(1.0F);
                iv_pause_playback.setAlpha(0.3F);
                iv_stop_playback.setAlpha(0.3F);
                break;
            case toEject:;
                sb_playback.setProgress(0);
                sb_playback.setVisibility(GONE);
                sb_playback.setEnabled(false);
                iv_play_playback.setEnabled(false);
                iv_pause_playback.setEnabled(false);
                iv_stop_playback.setEnabled(false);
                iv_play_playback.setAlpha(0.3F);
                iv_pause_playback.setAlpha(0.3F);
                iv_stop_playback.setAlpha(0.3F);
                break;
            case toInsert:
                sb_playback.setProgress(0);
                sb_playback.setVisibility(VISIBLE);
                sb_playback.setEnabled(false);
                iv_play_playback.setEnabled(true);
                iv_pause_playback.setEnabled(false);
                iv_stop_playback.setEnabled(false);
                iv_play_playback.setAlpha(1.0F);
                iv_pause_playback.setAlpha(0.3F);
                iv_stop_playback.setAlpha(0.3F);
                break;
            default:
                sb_playback.setProgress(0);
                sb_playback.setVisibility(GONE);
                sb_playback.setEnabled(false);
                iv_play_playback.setEnabled(false);
                iv_pause_playback.setEnabled(false);
                iv_stop_playback.setEnabled(false);
                iv_play_playback.setAlpha(0.3F);
                iv_pause_playback.setAlpha(0.3F);
                iv_stop_playback.setAlpha(0.3F);
                break;
        }
    }

    private boolean startOnlineMediaPlayerService(String link)
    {
        Log.v("PLAYBACK_ACTIVITY", "Online Service, link: " + link);

        boolean valid = URLUtil.isValidUrl(link);

        if(valid)
        {
            stopMediaPlayerService();

            Log.v("PLAYBACK_ACTIVITY", "starting service");

            MyMediaPlayer = null;
            MyMediaPlayer = new Intent(PlaybackActivity.this, PlaybackService.class);   // put Service into Intent, to tell Intent where it should go afterwards.
            MyMediaPlayer.putExtra("LINK", link);
            startService(MyMediaPlayer); //cf. startActivity(...)\

            return true;
        }
        else
        {
            Log.v("PLAYBACK_ACTIVITY", "invalid link");

            return false;
        }
    }

    private boolean startOfflineMediaPlayerService(String file)
    {
        Log.v("PLAYBACK_ACTIVITY", "Offline Service, file: " + file);

        String directory = PlaybackActivity.this.getFilesDir().getAbsolutePath() + "/" + MEDIA_FOLDER + "/" + file;

        File path = new File(directory);

        if(path.exists())
        {
            stopMediaPlayerService();

            Log.v("PLAYBACK_ACTIVITY", "starting service");

            MyMediaPlayer = null;
            MyMediaPlayer = new Intent(PlaybackActivity.this, PlaybackService.class);   // put Service into Intent, to tell Intent where it should go afterwards.
            MyMediaPlayer.putExtra("FILE", file);
            startService(MyMediaPlayer); //cf. startActivity(...)\

            return true;
        }
        else
        {
            Log.v("PLAYBACK_ACTIVITY", "invalid file");

            return false;
        }
    }


    private void stopMediaPlayerService()
    {
        if(MyMediaPlayer != null) {

            Log.v("PLAYBACK_ACTIVITY", "stopping service");

            stopService(MyMediaPlayer);
            MyMediaPlayer = null;
        }
    }
}
