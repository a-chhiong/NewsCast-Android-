package com.unilarm.newscast.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import com.unilarm.newscast.R;
import com.unilarm.newscast.handle.DaoHandle;
import com.unilarm.newscast.handle.XmlHandle;
import com.unilarm.newscast.model.RssData;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import static com.unilarm.newscast.handle.DaoHandle.FEEDLIST;
import static com.unilarm.newscast.handle.DaoHandle.ITEMLIST;

public class ReloadActivity extends AppCompatActivity {

    /*UI*/
    private TextView tv_reload;
    //private ProgressBar //pb_reload;
    private int progress = 0;
    private ImageView iv_reload;
    private int animation = 0;
    private Timer timer;
    private TimerTask timerTask;

    /*DATA*/
    private RssData headDataFromDAO = new RssData();
    private RssData headDataFromXML = new RssData();
    private ArrayList<RssData> dataListFromXML = new ArrayList<>();
    private ArrayList<RssData> dataListFromDAO = new ArrayList<>();
    private int indexOfDataListFromDAO;
    private int sizeOfDataListFromDAO;

    /*HANDLE*/
    private DaoHandle daoHandle;
    private XmlHandle xmlHandle;

    /*BROADCAST*/
    private LocalBroadcastManager broadcastManager;
    private BroadcastReceiver broadcastReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reload);

        Log.v("RELOAD_LIFECYCLE", "onCreate");

        initView();

        initReceiver();

        initLoading();
    }

    @Override
    protected void onRestart() {
        super.onRestart();

        Log.v("RELOAD_LIFECYCLE", "onRestart");

    }

    @Override
    protected void onStart() {
        super.onStart();

        Log.v("RELOAD_LIFECYCLE", "onStart");

    }

    @Override
    protected void onResume() {
        super.onResume();

        Log.v("RELOAD_LIFECYCLE", "onResume");

    }

    @Override
    public void onBackPressed() {

        //super.onBackPressed();

        Log.v("RELOAD_LIFECYCLE", "onBackPressed");
    }

    @Override
    protected void onPause() {
        super.onPause();

        Log.v("RELOAD_LIFECYCLE", "onPause");
    }

    @Override
    protected void onStop() {
        super.onStop();

        Log.v("RELOAD_LIFECYCLE", "onStop");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        Log.v("RELOAD_LIFECYCLE", "onDestroy");

        daoHandle = null;
        xmlHandle = null;

        broadcastManager.unregisterReceiver(broadcastReceiver);

        timer.cancel();
    }

    private void initView() {

        tv_reload = findViewById(R.id.tv_reload);
        //pb_reload = findViewById(R.id.//pb_reload);
        iv_reload= findViewById(R.id.iv_reload);
        
        splash();
    }

    private void splash() {


        timer = new Timer();

        timerTask = new TimerTask() {
            @Override
            public void run() {

                switch(animation)
                {
                    case 0:
                        iv_reload.setImageResource(R.drawable.ic_dashboard);
                        animation++;
                        break;
                    case 1:
                        iv_reload.setImageResource(R.drawable.ic_view_day);
                        animation++;
                        break;
                    case 2:
                        iv_reload.setImageResource(R.drawable.ic_featured_play_list);
                        animation++;
                        break;
                    case 3:
                        iv_reload.setImageResource(R.drawable.ic_person);
                        animation=0;
                        break;
                }
            }
        };

        timer.schedule(timerTask, 0, 125); /// <-- every 125 ms

    }
    
    private void initReceiver()
    {
        broadcastManager = LocalBroadcastManager.getInstance(ReloadActivity.this);

        broadcastReceiver = new BroadcastReceiver()
        {
            @Override
            public void onReceive(Context context, Intent intent) {

                if(intent.hasExtra("DAO_HANDLE"))
                {
                    String status = intent.getStringExtra("DAO_HANDLE");

                    Log.v("ReloadActivity", "DAO_HANDLE: " + status);

                    String link;

                    switch(status)
                    {
                        case "FETCHED":
                            dataListFromDAO = daoHandle.getRssDataList();
                            sizeOfDataListFromDAO = dataListFromDAO.size();
                            indexOfDataListFromDAO = 0;

                            headDataFromDAO = dataListFromDAO.get(indexOfDataListFromDAO);
                            indexOfDataListFromDAO += 1;

                            link = headDataFromDAO.getLink();    //where the source of rss
                            Log.v("RELOAD", "headDataFromDAO, link: " + link);

                            tv_reload.setText(R.string.xml_fetch);

                            xmlHandle = null;
                            xmlHandle = new XmlHandle(ReloadActivity.this, link, "ReloadActivity");
                            xmlHandle.proceedTask();
                            break;
                        case "EMPTY":   //nothing in DATABASE, a whole new start...
                            cleanUpOldItemData(30);
                            break;
                        case "UPDATED": //SINGLE, for head data
                            tv_reload.setText(R.string.dao_insert);
                            daoHandle.setTable(ITEMLIST);
                            daoHandle.batchInsertData(dataListFromXML);
                            break;
                        case "BATCH_INSERTED":   //MULTIPLE, item data
                            if(indexOfDataListFromDAO >= sizeOfDataListFromDAO)
                            {
                                cleanUpOldItemData(30);
                            }
                            else
                            {
                                nextHeadDataFromDao(indexOfDataListFromDAO);
                            }
                            break;
                        case "NOTHING_DELETED":
                        case "BATCH_DELETED":
                            returnToPrevActivity();
                            break;
                    }
                }
                else if(intent.hasExtra("XML_HANDLE"))
                {
                    String status = intent.getStringExtra("XML_HANDLE");

                    Log.v("ReloadActivity", "XML_HANDLE: " + status);

                    String pubdateFromXML;
                    String pubdateFromDAO;

                    switch(status)
                    {
                        case "FETCHED":
                            dataListFromXML = xmlHandle.getDataList();
                            Log.v("RELOAD", "dataListFromXML, size: " + dataListFromXML.size());

                            headDataFromXML = dataListFromXML.remove(0);
                            Log.v("RELOAD", "headDataFromXML, parent: " + headDataFromXML.getParent());

                            pubdateFromXML = headDataFromXML.getPubdate();
                            pubdateFromDAO = headDataFromDAO.getPubdate();

                            if(pubdateFromDAO == null || !pubdateFromDAO.equals(pubdateFromXML))  //pubdate isn't coherent
                            {
                                headDataFromDAO.setPubdate(pubdateFromXML);

                                daoHandle.setTable(FEEDLIST);
                                daoHandle.updateData(headDataFromDAO);

                                tv_reload.setText(R.string.dao_update);
                                //pb_reload.setProgress(progress++);
                            }
                            else
                            {
                                if(indexOfDataListFromDAO >= sizeOfDataListFromDAO)
                                {
                                    cleanUpOldItemData(30);
                                }
                                else
                                {
                                    nextHeadDataFromDao(indexOfDataListFromDAO);
                                }
                            }
                            break;
                        case "EMPTY":
                            if(indexOfDataListFromDAO >= sizeOfDataListFromDAO)
                            {
                                cleanUpOldItemData(30);
                            }
                            else
                            {
                                nextHeadDataFromDao(indexOfDataListFromDAO);
                            }
                            break;
                    }
                }
            }
        };

        broadcastManager.registerReceiver(broadcastReceiver, new IntentFilter("ReloadActivity"));
    }

    private void initLoading()
    {
        tv_reload.setText(R.string.loading);
        tv_reload.setText(R.string.dao_fetch);

        daoHandle = null;
        daoHandle = new DaoHandle(ReloadActivity.this, FEEDLIST, "ReloadActivity");
        daoHandle.fetchDataList();
    }

    private void nextHeadDataFromDao(int index)
    {
        headDataFromDAO = dataListFromDAO.get(index);
        indexOfDataListFromDAO += 1;

        String link = headDataFromDAO.getLink();    //where the source of rss
        Log.v("ReloadActivity", "headDataFromDAO, link: " + link);

        tv_reload.setText(R.string.xml_fetch);

        xmlHandle.setUrlSource(link);
        xmlHandle.proceedTask();
    }

    private void cleanUpOldItemData(int daysAgo)
    {
        Date date = new Date();

        Calendar cal = Calendar.getInstance();

        cal.setTime(date);
        cal.add(Calendar.DAY_OF_MONTH, -daysAgo);
        date = cal.getTime();

        DateFormat dateformat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        String deadline = dateformat.format(date);

        Log.v("ReloadActivity", "before which date: " + deadline);

        daoHandle.setTable(ITEMLIST);
        daoHandle.batchDeleteItemDataWithDefaultExceptionBeforeData(deadline);

        tv_reload.setText(R.string.dao_clean_up);
    }

    private void returnToPrevActivity()
    {
        tv_reload.setText(R.string.ready);

        finish();
    }
}
