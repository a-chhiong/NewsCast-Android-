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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.unilarm.newscast.R;
import com.unilarm.newscast.adapter.TimeListAdapter;
import com.unilarm.newscast.handle.DaoHandle;
import com.unilarm.newscast.handle.XmlHandle;
import com.unilarm.newscast.model.RssData;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static com.unilarm.newscast.handle.DaoHandle.DESC;
import static com.unilarm.newscast.handle.DaoHandle.ITEMLIST;
import static com.unilarm.newscast.handle.DaoHandle.SET;
import static com.unilarm.newscast.handle.DaoHandle.UNSET;

public class TimeListActivity extends AppCompatActivity {

    /*UI for Content*/
    private Spinner spn_time_list;
    private TextView tv_time_list;
    private ListView lv_time_list;
    private ProgressBar pb_time_list;

    /*UI for Head Button*/
    private ImageView iv_reload_time_list;
    //private ImageView iv_sort_time_list;

    /*UI for Bar Button*/
    private ImageView iv_channel_time_list;
    private ImageView iv_calendar_time_list;
    private ImageView iv_favourite_time_list;
    private ImageView iv_person_time_list;

    /*UI LOGIC*/
    private int progress = 0;
    private boolean arrow_click_en = false;
    private boolean mark_click_en = false;

    /*DATA*/
    private String feed_parent = "";
    private String feed_title = "";
    private String which_date = "";
    private RssData rssHeadData = new RssData();
    private RssData rssItemData = new RssData();
    private ArrayList<RssData> rssItemList = new ArrayList<>();
    private List<String> pastFewDays = new ArrayList<>();

    /*HANDLE*/
    private XmlHandle xmlHandle;
    private DaoHandle daoHandle;

    /*BROADCAST*/
    private LocalBroadcastManager broadcastManager;
    private BroadcastReceiver broadcastReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_time_list);

        initData();

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

    private void initData()
    {
        Intent intent = getIntent();

        Bundle bundle = intent.getExtras();

        if (bundle != null)
        {
            RssData data = (RssData) bundle.getSerializable("RSS_DATA_OBJECT");

            if(data != null)
            {
                feed_parent = data.getParent();
                feed_title = data.getTitle();
            }
        }

        Log.v("TimeListActivity", "feed_parent: " + feed_parent);
        Log.v("TimeListActivity", "feed_title: " + feed_title);
    }

    private void initView() {
        /*SET ListView from here*/

        lv_time_list = findViewById(R.id.lv_time_list);
        tv_time_list = findViewById(R.id.tv_time_list);
        pb_time_list = findViewById(R.id.pb_time_list);
        spn_time_list = findViewById(R.id.spn_time_list);

        /*SET Head Button from here*/

        iv_reload_time_list = findViewById(R.id.iv_reload_time_list);
        //iv_sort_time_list = findViewById(R.id.iv_sort_time_list);

        /*SET Bar Head Button from here*/

        iv_channel_time_list = findViewById(R.id.iv_channel_time_list);
        iv_calendar_time_list = findViewById(R.id.iv_calendar_time_list);
        iv_favourite_time_list = findViewById(R.id.iv_favourite_time_list);
        iv_person_time_list = findViewById(R.id.iv_person_time_list);

        /* SET SPINNER */

        DateFormat dateformat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

        List<Date> pastWeek = new ArrayList<>();

        Date date = new Date();

        which_date = dateformat.format(date);

        pastWeek.add(date);

        Calendar cal = Calendar.getInstance();

        for (int i = 0; i < 10; i++) {
            cal.setTime(date);
            cal.add(Calendar.DAY_OF_MONTH, -1);
            date = cal.getTime();
            pastWeek.add(date);
        }

        pastFewDays = new ArrayList<>();

        for (int i = 0; i < 10; i++)
        {
            pastFewDays.add(dateformat.format(pastWeek.get(i)));
        }

        ArrayAdapter<String> dateAdapter = new ArrayAdapter<>(TimeListActivity.this, R.layout.view_spinner, pastFewDays);
        dateAdapter.setDropDownViewResource(R.layout.view_spinner_drop);
        spn_time_list.setAdapter(dateAdapter);
    }

    private void initHandle()
    {
        iv_reload_time_list.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v) {

                buttonEnabled(false);

                Intent myIntent = new Intent(TimeListActivity.this, ReloadActivity.class);

                startActivity(myIntent);
            }
        });

//        iv_sort_time_list.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//
//
//            }
//        });

        spn_time_list.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {

                which_date = pastFewDays.get(position);

                reLoading();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {


            }
        });
        
        iv_channel_time_list.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                buttonEnabled(false);

                Intent myIntent = new Intent(TimeListActivity.this, ChanListActivity.class);

                startActivity(myIntent);

                finish();
            }
        });

        iv_calendar_time_list.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v) {

                
            }
        });

        iv_favourite_time_list.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                buttonEnabled(false);

                Intent myIntent = new Intent(TimeListActivity.this, PlayListActivity.class);

                startActivity(myIntent);

                finish();
            }
        });

        iv_person_time_list.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                buttonEnabled(false);

                Intent myIntent = new Intent(TimeListActivity.this, PersonalActivity.class);

                startActivity(myIntent);

                finish();
            }
        });

    }

    private void initReceiver()
    {
        broadcastManager = LocalBroadcastManager.getInstance(TimeListActivity.this);

        broadcastReceiver = new BroadcastReceiver()
        {
            @Override
            public void onReceive(Context context, Intent intent)
            {

                if(intent.hasExtra("DAO_HANDLE"))
                {
                    String status = intent.getStringExtra("DAO_HANDLE");

                    Log.v("TimeListActivity", "DAO_HANDLE: " + status);

                    switch(status)
                    {
                        case "FETCHED":
                            rssItemList = daoHandle.getRssDataList();
                            adapterReload();
                            lv_time_list.setVisibility(VISIBLE);
                            tv_time_list.setVisibility(GONE);
                            pb_time_list.setVisibility(GONE);
                            buttonEnabled(true);
                            break;
                        case "EMPTY":
                            lv_time_list.setVisibility(GONE);
                            tv_time_list.setVisibility(VISIBLE);
                            pb_time_list.setVisibility(GONE);
                            tv_time_list.setText(R.string.empty);
                            buttonEnabled(true);
                            break;
                        case "UPDATED":
                            Toast.makeText(TimeListActivity.this, status, Toast.LENGTH_SHORT).show();
                            buttonEnabled(true);
                            break;
                    }
                }
                else if(intent.hasExtra("MARK_CLICK"))
                {
                    int index = intent.getIntExtra("MARK_CLICK", -1);

                    Log.v("TimeListActivity", "MARK_CLICK: " + index);

                    if(index >= 0)
                    {
                        if(mark_click_en) {
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

                    Log.v("TimeListActivity", "UNMARK_CLICK: " + index);

                    if(index >= 0)
                    {
                        if(mark_click_en) {
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

                    Log.v("TimeListActivity", "ARROW_CLICK: " + index);

                    if(index >= 0)
                    {
                        if(arrow_click_en) {
                            buttonEnabled(false);

                            rssItemData = rssItemList.get(index);

                            Intent newIntent = new Intent(TimeListActivity.this, PlaybackActivity.class);
                            Bundle bundle = new Bundle();
                            bundle.putSerializable("RSS_DATA_OBJECT", rssItemData);
                            newIntent.putExtras(bundle);
                            startActivity(newIntent);
                        }
                    }
                }
            }
        };

        broadcastManager.registerReceiver(broadcastReceiver, new IntentFilter("TimeListActivity"));
    }

    private void initLoading()
    {
        buttonEnabled(false);

        lv_time_list.setVisibility(GONE);
        tv_time_list.setVisibility(GONE);
        pb_time_list.setVisibility(VISIBLE);

        pb_time_list.setMax(10);
        pb_time_list.setProgress(5);

        daoHandle = null;
        daoHandle = new DaoHandle(TimeListActivity.this, ITEMLIST, "TimeListActivity");
        daoHandle.fetchDataListFuzzy(DaoHandle.DATE, which_date, DaoHandle.DATE, DaoHandle.DESC);
    }

    private void reLoading()
    {
        buttonEnabled(false);

        lv_time_list.setVisibility(GONE);
        tv_time_list.setVisibility(GONE);
        pb_time_list.setVisibility(VISIBLE);

        pb_time_list.setMax(10);
        pb_time_list.setProgress(5);

        daoHandle.setTable(ITEMLIST);
        daoHandle.fetchDataListFuzzy(DaoHandle.DATE, which_date, DaoHandle.DATE, DaoHandle.DESC);
    }

    private void adapterReload()
    {
        TimeListAdapter timeListAdapter = new TimeListAdapter(TimeListActivity.this, rssItemList);

        lv_time_list.setAdapter(timeListAdapter);
    }

    private void buttonEnabled(boolean enabled)
    {
        if(enabled)
        {
            iv_reload_time_list.setEnabled(true);
            iv_channel_time_list.setEnabled(true);
            iv_calendar_time_list.setEnabled(true);
            iv_favourite_time_list.setEnabled(true);
            iv_person_time_list.setEnabled(true);

            mark_click_en = true;
            arrow_click_en = true;
        }
        else
        {
            iv_reload_time_list.setEnabled(false);
            iv_channel_time_list.setEnabled(false);
            iv_calendar_time_list.setEnabled(false);
            iv_favourite_time_list.setEnabled(false);
            iv_person_time_list.setEnabled(false);

            mark_click_en = false;
            arrow_click_en = false;
        }
    }

    private void alertLeaving()
    {
        AlertDialog.Builder alert = new AlertDialog.Builder(TimeListActivity.this);

        alert.setTitle(R.string.leaving);
        alert.setMessage(R.string.are_you_sure);
        alert.setCancelable(false);
        alert.setPositiveButton("YES", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                //the super.xxx was in the very first line of this method of onBackPressed...
                //but move to here to co-op with onClick, but need to address its origin, MainActivity, which is added to the beginning...
                TimeListActivity.super.onBackPressed(); // to do whatever the onBackPressed should do originally
            }
        });

        alert.setNegativeButton("NO", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });

        alert.show();
    }

}
