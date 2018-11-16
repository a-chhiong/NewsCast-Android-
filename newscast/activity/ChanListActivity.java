package com.unilarm.newscast.activity;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.unilarm.newscast.R;
import com.unilarm.newscast.adapter.HeadListAdapter;
import com.unilarm.newscast.adapter.ItemListAdapter;
import com.unilarm.newscast.handle.DaoHandle;
import com.unilarm.newscast.model.RssData;

import java.util.ArrayList;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static com.unilarm.newscast.handle.DaoHandle.ASC;
import static com.unilarm.newscast.handle.DaoHandle.DATE;
import static com.unilarm.newscast.handle.DaoHandle.DESC;
import static com.unilarm.newscast.handle.DaoHandle.FEEDLIST;
import static com.unilarm.newscast.handle.DaoHandle.ITEMLIST;
import static com.unilarm.newscast.handle.DaoHandle.PARENT;
import static com.unilarm.newscast.handle.DaoHandle.SET;
import static com.unilarm.newscast.handle.DaoHandle.UNSET;

public class ChanListActivity extends AppCompatActivity {

    /*UI for Content*/
    private TextView tv_title_chan_list;
    private ListView lv_chan_list;
    private TextView tv_chan_list;
    private ProgressBar pb_chan_list;

    /*UI for Head Button*/
    private ImageView iv_reload_chan_list;
    private ImageView iv_sort_chan_list;
    private ImageView iv_subscribe_chan_list;
    private ImageView iv_back_chan_list;

    /*UI for Bar Button*/
    private ImageView iv_channel_chan_list;
    private ImageView iv_calendar_chan_list;
    private ImageView iv_favourite_chan_list;
    private ImageView iv_person_chan_list;

    /*UI LOGIC*/
    private int progress = 0;
    private boolean arrow_click_en;
    private boolean remove_click_en;
    private boolean mark_click_en;
    private boolean head_list_show;
    private boolean item_list_show;
    private boolean is_loading;
    private boolean is_deleting;
    private boolean is_descending;

    /*DATA*/
    private String feed_parent = "";
    private String feed_title = "";
    private String feed_link = "";
    private RssData rssHeadData = new RssData();
    private RssData rssItemData = new RssData();
    private ArrayList<RssData> rssHeadList = new ArrayList<>();
    private ArrayList<RssData> rssItemList = new ArrayList<>();

    /*HANDLE*/
    private DaoHandle daoHandle;

    /*BROADCAST*/
    private LocalBroadcastManager broadcastManager;
    private BroadcastReceiver broadcastReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chan_list);

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

        buttonEnabled(true);
    }

    @Override
    public void onBackPressed() {

        //super.onBackPressed(); <-- this will be used later...

        if(head_list_show)
        {
            alertLeaving();
        }
        else if(item_list_show)
        {
            head_list_show = true;
            iv_subscribe_chan_list.setVisibility(VISIBLE);

            item_list_show = false;
            iv_sort_chan_list.setVisibility(GONE);
            iv_back_chan_list.setVisibility(GONE);

            reLoading();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        daoHandle = null;

        broadcastManager.unregisterReceiver(broadcastReceiver);
    }


    private void initView()
    {
        /*SET ListView from here*/

        tv_title_chan_list = findViewById(R.id.tv_title_chan_list);
        lv_chan_list = findViewById(R.id.lv_chan_list);
        tv_chan_list = findViewById(R.id.tv_chan_list);
        pb_chan_list = findViewById(R.id.pb_chan_list);

        tv_title_chan_list.setVisibility(GONE);

        /*SET Head Button from here*/

        iv_reload_chan_list = findViewById(R.id.iv_reload_chan_list);
        iv_sort_chan_list = findViewById(R.id.iv_sort_chan_list);
        iv_subscribe_chan_list = findViewById(R.id.iv_subscribe_chan_list);
        iv_back_chan_list = findViewById(R.id.iv_back_chan_list);

        /*SET Bar Head Button from here*/

        iv_channel_chan_list = findViewById(R.id.iv_channel_chan_list);
        iv_calendar_chan_list = findViewById(R.id.iv_calendar_chan_list);
        iv_favourite_chan_list = findViewById(R.id.iv_favourite_chan_list);
        iv_person_chan_list = findViewById(R.id.iv_person_chan_list);

        /*DEFAULT VIEW*/

        head_list_show = true;
        iv_subscribe_chan_list.setVisibility(VISIBLE);

        item_list_show = false;
        iv_sort_chan_list.setVisibility(GONE);
        iv_back_chan_list.setVisibility(GONE);
    }

    private void initHandle()
    {
        iv_back_chan_list.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                if(item_list_show)
                {
                    head_list_show = true;
                    iv_subscribe_chan_list.setVisibility(VISIBLE);

                    item_list_show = false;
                    iv_sort_chan_list.setVisibility(GONE);
                    iv_back_chan_list.setVisibility(GONE);

                    reLoading();
                }
            }
        });

        iv_reload_chan_list.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                buttonEnabled(false);

                Intent myIntent = new Intent(ChanListActivity.this, ReloadActivity.class);

                startActivity(myIntent);
            }
        });

        iv_sort_chan_list.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(item_list_show)
                {
                    daoHandle.setTable(ITEMLIST);

                    if(is_descending)
                    {
                        is_descending = false;

                        daoHandle.fetchDataList(PARENT, feed_title, DATE, ASC);
                    }
                    else
                    {
                        is_descending = true;

                        daoHandle.fetchDataList(PARENT, feed_title, DATE, DESC);
                    }

                    is_loading = true;

                    Log.v("ChanList", "ITEM LIST FETCHING");
                }
            }
        });

        iv_subscribe_chan_list.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v) {

                buttonEnabled(false);

                Intent myIntent = new Intent(ChanListActivity.this, SubscribeActivity.class);

                startActivity(myIntent);
            }
        });

        iv_channel_chan_list.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

//                if(item_list_show)
//                {
//                    head_list_show = true;
//                    iv_subscribe_chan_list.setVisibility(VISIBLE);
//
//                    item_list_show = false;
//                    iv_sort_chan_list.setVisibility(GONE);
//
//                    reLoading();
//                }
            }
        });

        iv_calendar_chan_list.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v) {

                buttonEnabled(false);

                Intent myIntent = new Intent(ChanListActivity.this, TimeListActivity.class);

                startActivity(myIntent);

                finish();
            }
        });

        iv_favourite_chan_list.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                buttonEnabled(false);

                Intent myIntent = new Intent(ChanListActivity.this, PlayListActivity.class);

                startActivity(myIntent);

                finish();
            }
        });

        iv_person_chan_list.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                buttonEnabled(false);

                Intent myIntent = new Intent(ChanListActivity.this, PersonalActivity.class);

                startActivity(myIntent);

                finish();
            }
        });
    }

    private void initReceiver()
    {
        broadcastManager = LocalBroadcastManager.getInstance(ChanListActivity.this);

        broadcastReceiver = new BroadcastReceiver()
        {
            @Override
            public void onReceive(Context context, Intent intent) {

                if(head_list_show)
                {
                    if(intent.hasExtra("DAO_HANDLE"))
                    {
                        String status = intent.getStringExtra("DAO_HANDLE");

                        Log.v("ChanListActivity", "DAO_HANDLE: " + status);

                        switch(status)
                        {
                            case "FETCHED":
                                if(is_loading) {
                                    is_loading = false;
                                    rssHeadList = daoHandle.getRssDataList();
                                    adapterReload();
                                    tv_title_chan_list.setVisibility(GONE);
                                    lv_chan_list.setVisibility(VISIBLE);
                                    tv_chan_list.setVisibility(GONE);
                                    pb_chan_list.setVisibility(GONE);
                                    buttonEnabled(true);
                                }
                                break;
                            case "EMPTY":
                                if(is_loading) {
                                    is_loading = false;
                                    lv_chan_list.setVisibility(GONE);
                                    tv_chan_list.setVisibility(VISIBLE);
                                    pb_chan_list.setVisibility(GONE);
                                    tv_chan_list.setText(R.string.empty);
                                    buttonEnabled(true);
                                }
                                break;
                            case "UPDATED":
                                reLoading();
                                break;
                            case "DELETED": //where head data is deleted...
                                Log.v("CHAN_LIST_ACTIVITY", "items to be deleted: " + feed_title);
                                pb_chan_list.setProgress(progress++);
                                daoHandle.setTable(ITEMLIST);
                                daoHandle.batchDeleteItemDataWithDefaultExceptionAndParentIs(feed_title);
                                break;
                            case "NOTHING_DELETED":
                            case "BATCH_DELETED":
                                reLoading();
                                break;
                        }
                    }
                    else if(intent.hasExtra("DELETED_PROGRESS"))
                    {
                        int progress = intent.getIntExtra("DELETED_PROGRESS", -1);

                        Log.v("ChanListActivity", "DELETED_PROGRESS: " + progress);

                        if(progress > 0)
                        {
                            pb_chan_list.setProgress(progress);
                        }
                    }
                    else if(intent.hasExtra("ARROW_CLICK"))
                    {
                        int index = intent.getIntExtra("ARROW_CLICK", -1);

                        Log.v("ChanListActivity", "ARROW_CLICK: " + index);

                        if(index >= 0)
                        {
                            if(arrow_click_en)
                            {
                                buttonEnabled(false);

                                rssHeadData = rssHeadList.get(index);

                                feed_parent = rssHeadData.getParent();
                                feed_title = rssHeadData.getTitle();
                                feed_link = rssHeadData.getLink();

                                head_list_show = false;
                                iv_subscribe_chan_list.setVisibility(GONE);

                                item_list_show = true;
                                iv_sort_chan_list.setVisibility(VISIBLE);
                                iv_back_chan_list.setVisibility(VISIBLE);

                                reLoading();
                            }
                        }
                    }
                    else if(intent.hasExtra("REMOVE_CLICK"))
                    {
                        int index = intent.getIntExtra("REMOVE_CLICK", -1);

                        Log.v("ChanListActivity", "REMOVE_CLICK: " + index);

                        if(index >= 0) {

                            if(remove_click_en)
                            {
                                buttonEnabled(false);

                                rssHeadData = rssHeadList.get(index);

                                String mark = rssHeadData.getMark();

                                if(mark != null && mark.equals(SET))
                                {
                                    Toast.makeText(ChanListActivity.this, "A preset feed can't be removed", Toast.LENGTH_SHORT).show();

                                    buttonEnabled(true);
                                }
                                else
                                {
                                    alertDeleting();
                                }
                            }
                        }
                    }
                }
                else if(item_list_show)
                {
                    if(intent.hasExtra("DAO_HANDLE"))
                    {
                        String status = intent.getStringExtra("DAO_HANDLE");

                        Log.v("ChanListActivity", "DAO_HANDLE: " + status);

                        switch(status)
                        {
                            case "FETCHED":
                                rssItemList = daoHandle.getRssDataList();
                                adapterReload();
                                tv_title_chan_list.setText(feed_title);
                                tv_title_chan_list.setVisibility(VISIBLE);
                                lv_chan_list.setVisibility(VISIBLE);
                                tv_chan_list.setVisibility(GONE);
                                pb_chan_list.setVisibility(GONE);
                                buttonEnabled(true);
                                break;
                            case "EMPTY":
                                lv_chan_list.setVisibility(GONE);
                                tv_chan_list.setVisibility(VISIBLE);
                                pb_chan_list.setVisibility(GONE);
                                tv_chan_list.setText(R.string.empty);
                                buttonEnabled(true);
                                break;
                            case "UPDATED":
                                Toast.makeText(ChanListActivity.this, status, Toast.LENGTH_SHORT).show();
                                buttonEnabled(true);
                                break;
                        }
                    }
                    else if(intent.hasExtra("MARK_CLICK"))
                    {
                        int index = intent.getIntExtra("MARK_CLICK", -1);

                        Log.v("ChanListActivity", "MARK_CLICK: " + index);

                        if(index >= 0)
                        {
                            if(mark_click_en)
                            {
                                buttonEnabled(false);

                                rssItemData = rssItemList.get(index);
                                rssItemData.setMark(SET);

                                daoHandle.setTable(ITEMLIST);
                                daoHandle.updateData(rssItemData);
                            }
                        }
                    }
                    else if(intent.hasExtra("UNMARK_CLICK"))
                    {
                        int index = intent.getIntExtra("UNMARK_CLICK", -1);

                        Log.v("ChanListActivity", "UNMARK_CLICK: " + index);

                        if(index >= 0)
                        {
                            if(mark_click_en)
                            {
                                buttonEnabled(false);

                                rssItemData = rssItemList.get(index);
                                rssItemData.setMark(UNSET);

                                daoHandle.setTable(ITEMLIST);
                                daoHandle.updateData(rssItemData);
                            }
                        }
                    }
                    else if(intent.hasExtra("ARROW_CLICK"))
                    {
                        int index = intent.getIntExtra("ARROW_CLICK", -1);

                        Log.v("ChanListActivity", "ARROW_CLICK: " + index);

                        if(index >= 0)
                        {
                            if(arrow_click_en)
                            {
                                buttonEnabled(false);

                                rssItemData = rssItemList.get(index);
                                Intent newIntent = new Intent(ChanListActivity.this, PlaybackActivity.class);
                                Bundle bundle = new Bundle();
                                bundle.putSerializable("RSS_DATA_OBJECT", rssItemData);
                                newIntent.putExtras(bundle);
                                startActivity(newIntent);
                            }
                        }
                    }
                }
            }
        };

        broadcastManager.registerReceiver(broadcastReceiver, new IntentFilter("ChanListActivity"));

    }

    private void initLoading()
    {
        buttonEnabled(false);

        arrow_click_en = false;
        remove_click_en = false;

        lv_chan_list.setVisibility(GONE);
        tv_chan_list.setVisibility(GONE);
        pb_chan_list.setVisibility(VISIBLE);

        pb_chan_list.setMax(10);
        pb_chan_list.setProgress(5);

        if(head_list_show)
        {
            Log.v("ChanListActivity", "HEAD LIST FETCH");

            is_loading = true;

            daoHandle = null;
            daoHandle = new DaoHandle(ChanListActivity.this, FEEDLIST, "ChanListActivity");
            daoHandle.fetchDataList();
        }
        else if(item_list_show)
        {
            Log.v("ChanListActivity", "ITEM LIST FETCH");

            is_descending = true;

            is_loading = true;

            daoHandle = null;
            daoHandle = new DaoHandle(ChanListActivity.this, ITEMLIST, "ChanListActivity");
            daoHandle.fetchDataList(PARENT, feed_title, DATE, DESC);
        }
    }

    private void reLoading()
    {
        buttonEnabled(false);

        arrow_click_en = false;
        remove_click_en = false;

        lv_chan_list.setVisibility(GONE);
        tv_chan_list.setVisibility(GONE);
        pb_chan_list.setVisibility(VISIBLE);

        pb_chan_list.setMax(10);
        pb_chan_list.setProgress(5);

        if(head_list_show)
        {
            Log.v("ChanListActivity", "HEAD LIST FETCHING");

            is_loading = true;

            daoHandle.setTable(FEEDLIST);
            daoHandle.fetchDataList();
        }
        else if(item_list_show)
        {
            Log.v("ChanListActivity", "ITEM LIST FETCHING");

            is_descending = true;

            is_loading = true;

            daoHandle.setTable(ITEMLIST);
            daoHandle.fetchDataList(PARENT, feed_title, DATE, DESC);
        }
    }

    private void adapterReload()
    {
        if(head_list_show)
        {
            Log.v("ChanListActivity", "HEAD LIST RELOAD");

            HeadListAdapter headListAdapter =  new HeadListAdapter(ChanListActivity.this, rssHeadList);

            lv_chan_list.setAdapter(headListAdapter);
        }
        else if(item_list_show)
        {
            Log.v("ChanListActivity", "ITEM LIST RELOAD");

            ItemListAdapter itemListAdapter =  new ItemListAdapter(ChanListActivity.this, rssItemList);

            lv_chan_list.setAdapter(itemListAdapter);
        }
    }

    private void buttonEnabled(boolean enabled)
    {
        if(enabled)
        {
            iv_subscribe_chan_list.setEnabled(true);
            iv_reload_chan_list.setEnabled(true);

            iv_channel_chan_list.setEnabled(true);
            iv_calendar_chan_list.setEnabled(true);
            iv_favourite_chan_list.setEnabled(true);
            iv_person_chan_list.setEnabled(true);

            mark_click_en = true;
            remove_click_en = true;
            arrow_click_en = true;
        }
        else
        {
            iv_subscribe_chan_list.setEnabled(false);
            iv_reload_chan_list.setEnabled(false);

            iv_channel_chan_list.setEnabled(false);
            iv_calendar_chan_list.setEnabled(false);
            iv_favourite_chan_list.setEnabled(false);
            iv_person_chan_list.setEnabled(false);

            mark_click_en = false;
            remove_click_en = false;
            arrow_click_en = false;
        }
    }

    private void alertLeaving()
    {
        AlertDialog.Builder alert = new AlertDialog.Builder(ChanListActivity.this);

        alert.setTitle(R.string.leaving);
        alert.setMessage(R.string.are_you_sure);
        alert.setCancelable(false);
        alert.setPositiveButton("YES", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                //the super.xxx was in the very first line of this method of onBackPressed...
                //but move to here to co-op with onClick, but need to address its origin, MainActivity, which is added to the beginning...
                ChanListActivity.super.onBackPressed(); // to do whatever the onBackPressed should do originally
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
        AlertDialog.Builder alert = new AlertDialog.Builder(ChanListActivity.this);

        alert.setTitle(R.string.deleting);
        alert.setMessage(rssHeadData.getTitle());
        alert.setCancelable(false);
        alert.setPositiveButton("YES", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                lv_chan_list.setVisibility(GONE);
                tv_chan_list.setVisibility(GONE);
                pb_chan_list.setVisibility(VISIBLE);

                pb_chan_list.setMax(100);

                feed_title = rssHeadData.getTitle();

                Log.v("ChanListActivity", "head to be deleted: " + feed_title);

                daoHandle.setTable(FEEDLIST);
                daoHandle.deleteData(rssHeadData);
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

}
