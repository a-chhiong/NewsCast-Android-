package com.unilarm.newscast.activity;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.unilarm.newscast.R;
import com.unilarm.newscast.adapter.LoadListAdapter;
import com.unilarm.newscast.adapter.LoveListAdapter;
import com.unilarm.newscast.handle.DaoHandle;
import com.unilarm.newscast.model.RssData;

import java.io.File;
import java.util.ArrayList;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static com.unilarm.newscast.handle.DaoHandle.ASC;
import static com.unilarm.newscast.handle.DaoHandle.DATE;
import static com.unilarm.newscast.handle.DaoHandle.DESC;
import static com.unilarm.newscast.handle.DaoHandle.ITEMLIST;
import static com.unilarm.newscast.handle.DaoHandle.MARK;
import static com.unilarm.newscast.handle.DaoHandle.SET;


public class PlayListActivity extends AppCompatActivity {

    /*UI for Content*/
    private ListView lv_play_list;
    private TextView tv_play_list;
    private ProgressBar pb_play_list;

    /*UI for Head Button*/
    private ImageView iv_reload_play_list;
    private ImageView iv_sort_play_list;
    private ImageView iv_load_play_list;
    private ImageView iv_love_play_list;
    
    /*UI for Bar Button*/
    private ImageView iv_channel_play_list;
    private ImageView iv_calendar_play_list;
    private ImageView iv_favourite_play_list;
    private ImageView iv_person_play_list;

    /*UI LOGIC*/
    private boolean arrow_click_en = false;
    private boolean remove_click_en = false;
    private boolean isFavouriteList = false;
    private boolean isDownloadList = false;
    private boolean isFavouriteListDesc = true;
    private boolean isDownloadListDesc = true;

    /*DATA*/
    private RssData rssItemData = new RssData();
    private ArrayList<RssData> rssItemList = new ArrayList<>();

    /*HANDLE*/
    private DaoHandle daoHandle;

    /*BROADCAST*/
    private LocalBroadcastManager broadcastManager;
    private BroadcastReceiver broadcastReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play_list);

        initView();

        initHandle();

        initReceiver();

        initLoading();
    }

    @Override
    protected void onRestart() {
        super.onRestart();

        reLoading();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume()
    {
        super.onResume();
    }

    @Override
    public void onBackPressed() {
        //super.onBackPressed(); <-- this will be used later...

        alertLeaving();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        broadcastManager.unregisterReceiver(broadcastReceiver);
    }

    private void initView()
    {
        /*SET ListView from here*/

        tv_play_list = findViewById(R.id.tv_play_list);
        lv_play_list = findViewById(R.id.lv_play_list);
        pb_play_list = findViewById(R.id.pb_play_list);

        pb_play_list.setMax(10);
        pb_play_list.setProgress(2);

        /*SET Head Button from here*/

        iv_reload_play_list = findViewById(R.id.iv_reload_play_list);
        iv_sort_play_list = findViewById(R.id.iv_sort_play_list);
        iv_love_play_list = findViewById(R.id.iv_love_play_list);
        iv_load_play_list = findViewById(R.id.iv_load_play_list);
        
        /*SET Bar Head Button from here*/

        iv_channel_play_list = findViewById(R.id.iv_channel_play_list);
        iv_calendar_play_list = findViewById(R.id.iv_calendar_play_list);
        iv_favourite_play_list = findViewById(R.id.iv_favourite_play_list);
        iv_person_play_list = findViewById(R.id.iv_person_play_list);

    }

    private void initHandle() {

        iv_reload_play_list.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v) {

                buttonEnabled(false);

                Intent myIntent = new Intent(PlayListActivity.this, ReloadActivity.class);

                startActivity(myIntent);
            }
        });

        iv_sort_play_list.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                initSorting();
            }
        });

        iv_love_play_list.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                /* Love --> Load */

                if(isDownloadList)
                {
                    initFavouriteList();

                    reLoading();
                }
            }
        });

        iv_load_play_list.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                /* Love <-- Load */

                if(isFavouriteList)
                {
                    initDownloadList();

                    reLoading();
                }
            }
        });

        iv_channel_play_list.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                
                buttonEnabled(false);

                Intent myIntent = new Intent(PlayListActivity.this, ChanListActivity.class);

                startActivity(myIntent);

                finish();
            }
        });

        iv_calendar_play_list.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v) {
                
                buttonEnabled(false);

                Intent myIntent = new Intent(PlayListActivity.this, TimeListActivity.class);

                startActivity(myIntent);

                finish();
            }
        });

        iv_favourite_play_list.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


            }
        });

        iv_person_play_list.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                buttonEnabled(false);

                Intent myIntent = new Intent(PlayListActivity.this, PersonalActivity.class);

                startActivity(myIntent);

                finish();
            }
        });
    }

    private void initReceiver()
    {
        broadcastManager = LocalBroadcastManager.getInstance(PlayListActivity.this);

        broadcastReceiver = new BroadcastReceiver()
        {
            @Override
            public void onReceive(Context context, Intent intent)
            {
                if(intent.hasExtra("DAO_HANDLE"))
                {
                    String status = intent.getStringExtra("DAO_HANDLE");

                    Log.v("PlayListActivity", "DAO_HANDLE: " + status);

                    switch(status)
                    {
                        case "FETCHED":
                            rssItemList = daoHandle.getRssDataList();
                            adapterReload();
                            lv_play_list.setVisibility(VISIBLE);
                            tv_play_list.setVisibility(GONE);
                            pb_play_list.setVisibility(GONE);
                            buttonEnabled(true);
                            break;
                        case "EMPTY":
                            lv_play_list.setVisibility(GONE);
                            tv_play_list.setVisibility(VISIBLE);
                            pb_play_list.setVisibility(GONE);
                            tv_play_list.setText(R.string.empty);
                            buttonEnabled(true);
                            break;
                        case "UPDATED":
                            reLoading();
                            break;
                    }
                }
                else if(intent.hasExtra("ARROW_CLICK"))
                {
                    int index = intent.getIntExtra("ARROW_CLICK", -1);

                    Log.v("PlayListActivity", "ARROW_CLICK: " + index);

                    if(index >= 0)
                    {
                        if(arrow_click_en)
                        {
                            buttonEnabled(false);

                            rssItemData = rssItemList.get(index);

                            Intent newIntent = new Intent(PlayListActivity.this, PlaybackActivity.class);
                            Bundle bundle = new Bundle();
                            bundle.putSerializable("RSS_DATA_OBJECT", rssItemData);
                            newIntent.putExtras(bundle);
                            startActivity(newIntent);
                        }
                    }

                }
                else if(intent.hasExtra("REMOVE_CLICK"))
                {
                    int index = intent.getIntExtra("REMOVE_CLICK", -1);

                    Log.v("PlayListActivity", "REMOVE_CLICK: " + index);

                    if(index >= 0)
                    {
                        if(remove_click_en)
                        {
                            buttonEnabled(false);

                            rssItemData = rssItemList.get(index);

                            alertDeleting();
                        }
                    }
                }
            }
        };

        broadcastManager.registerReceiver(broadcastReceiver, new IntentFilter("PlayListActivity"));
    }

    private void initLoading()
    {
        buttonEnabled(false);

        lv_play_list.setVisibility(GONE);
        tv_play_list.setVisibility(GONE);
        pb_play_list.setVisibility(VISIBLE);
        pb_play_list.setMax(10);
        pb_play_list.setProgress(5);

        initFavouriteList();

        daoHandle= null;
        daoHandle = new DaoHandle(PlayListActivity.this, ITEMLIST, "PlayListActivity");
        daoHandle.fetchDataList(MARK, SET, DATE, DESC);
    }

    private void reLoading()
    {
        buttonEnabled(false);

        lv_play_list.setVisibility(GONE);
        tv_play_list.setVisibility(GONE);
        pb_play_list.setVisibility(VISIBLE);
        pb_play_list.setMax(10);
        pb_play_list.setProgress(5);

        if(isFavouriteList)
        {
            showFavouriteList(DESC);
        }
        else if(isDownloadList)
        {
            showDownloadList(DESC);
        }
    }

    private void initSorting()
    {
        if(isFavouriteList)
        {
            if(isFavouriteListDesc)
            {
                showFavouriteList(ASC);
            }
            else
            {
                showFavouriteList(DESC);
            }
        }
        else if(isDownloadList)
        {
            if(isDownloadListDesc)
            {
                showDownloadList(ASC);
            }
            else
            {
                showDownloadList(DESC);
            }
        }
    }

    private void showFavouriteList(String sort)
    {
        if(sort.equals(DESC))
        {
            isFavouriteListDesc = true;
        }
        else
        {
            isFavouriteListDesc = false;
        }
        
        daoHandle.fetchDataList(MARK, SET, DATE, sort);
    }

    private void showDownloadList(String sort)
    {
        if(sort.equals(DESC))
        {
            isDownloadListDesc = true;
        }
        else
        {
            isDownloadListDesc = false;
        }

        daoHandle.fetchDataListMedia(null, null, DATE, sort);
    }

    private void initFavouriteList()
    {
        isFavouriteList = true;
        isDownloadList = false;

        iv_love_play_list.setEnabled(false);
        iv_load_play_list.setEnabled(true);

        iv_love_play_list.setImageResource(R.drawable.ic_favorite);
        iv_load_play_list.setImageResource(R.drawable.ic_offline_pin_white);

        iv_love_play_list.setBackgroundResource(R.color.colorBackWhite);
        iv_load_play_list.setBackgroundResource(R.color.colorHeadBar);
    }

    private void initDownloadList()
    {
        isFavouriteList = false;
        isDownloadList = true;

        iv_love_play_list.setEnabled(true);
        iv_load_play_list.setEnabled(false);

        iv_love_play_list.setImageResource(R.drawable.ic_favorite_white);
        iv_load_play_list.setImageResource(R.drawable.ic_offline_pin);

        iv_love_play_list.setBackgroundResource(R.color.colorHeadBar);
        iv_load_play_list.setBackgroundResource(R.color.colorBackWhite);
    }

    private void adapterReload()
    {
        if(isFavouriteList)
        {
            LoveListAdapter loveListAdapter =  new LoveListAdapter(PlayListActivity.this, rssItemList);

            lv_play_list.setAdapter(loveListAdapter);
        }
        else if(isDownloadList)
        {
            LoadListAdapter loadListAdapter =  new LoadListAdapter(PlayListActivity.this, rssItemList);

            lv_play_list.setAdapter(loadListAdapter);
        }
    }

    private void buttonEnabled(boolean enabled)
    {
        if(enabled)
        {
            iv_reload_play_list.setEnabled(true);
            iv_channel_play_list.setEnabled(true);
            iv_calendar_play_list.setEnabled(true);
            iv_favourite_play_list.setEnabled(true);
            iv_person_play_list.setEnabled(true);

            remove_click_en = true;
            arrow_click_en = true;
        }
        else
        {
            iv_reload_play_list.setEnabled(false);
            iv_channel_play_list.setEnabled(false);
            iv_calendar_play_list.setEnabled(false);
            iv_favourite_play_list.setEnabled(false);
            iv_person_play_list.setEnabled(false);

            remove_click_en = false;
            arrow_click_en = false;
        }
    }

    private void alertLeaving()
    {
        AlertDialog.Builder alert = new AlertDialog.Builder(PlayListActivity.this);

        alert.setTitle(R.string.leaving);
        alert.setMessage(R.string.are_you_sure);
        alert.setCancelable(false);
        alert.setPositiveButton("YES", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                //the super.xxx was in the very first line of this method of onBackPressed...
                //but move to here to co-op with onClick, but need to address its origin, MainActivity, which is added to the beginning...
                PlayListActivity.super.onBackPressed(); // to do whatever the onBackPressed should do originally
            }
        });

        alert.setNegativeButton("NO", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });

        alert.show();
    }

    private void alertDeleting()
    {
        AlertDialog.Builder alert = new AlertDialog.Builder(PlayListActivity.this);

        alert.setTitle(R.string.deleting);
        alert.setMessage(rssItemData.getTitle());
        alert.setCancelable(false);
        alert.setPositiveButton("YES", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                if(isFavouriteList)
                {
                    rssItemData.setMark("0");
                    daoHandle.setTable(ITEMLIST);
                    daoHandle.updateData(rssItemData);
                }
                else if(isDownloadList)
                {
                    deleteFile(rssItemData);

                    rssItemData.setMedia(null);
                    daoHandle.setTable(ITEMLIST);
                    daoHandle.updateData(rssItemData);
                }
            }
        });

        alert.setNegativeButton("NO", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                buttonEnabled(true);
            }
        });

        alert.show();
    }

    private void deleteFile(RssData data)
    {
        File file;
        File folder;

        String fileName = data.getMedia();
        String folderName = data.getParent();

        String directory = PlayListActivity.this.getFilesDir().getAbsolutePath() + folderName;

        folder = new File(directory);

        if(folder.exists())
        {
            file = new File(directory + "/" + fileName);

            if(file.exists())
            {
                if(file.delete())
                {
                    Log.v("PlayListActivity", "file deleted");
                }
                else
                {
                    Log.v("PlayListActivity", "file not deleted");
                }
            }
            else
            {
                Log.v("PlayListActivity", "file not existed");
            }
        }
        else
        {
            Log.v("PlayListActivity", "folder not existed");
        }
    }
}
