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

public class WelcomeActivity extends AppCompatActivity {

    /*UI*/
    private TextView tv_welcome;
    //private ProgressBar //pb_welcome;
    private int progress = 0;
    private ImageView iv_welcome;
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
        setContentView(R.layout.activity_welcome);

        initView();

        initReceiver();

        initLoading();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        daoHandle = null;
        xmlHandle = null;

        broadcastManager.unregisterReceiver(broadcastReceiver);
        
        timer.cancel();
    }

    private void initView() {

        tv_welcome = findViewById(R.id.tv_welcome);
        //pb_welcome = findViewById(R.id.//pb_welcome);
        iv_welcome= findViewById(R.id.firebase_image_logo);
        
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
                        iv_welcome.setImageResource(R.drawable.ic_newscast_globe_no01);
                        animation++;
                        break;
                    case 1:
                        iv_welcome.setImageResource(R.drawable.ic_newscast_globe_no02);
                        animation++;
                        break;
                    case 2:
                        iv_welcome.setImageResource(R.drawable.ic_newscast_globe_no03);
                        animation++;
                        break;
                    case 3:
                        iv_welcome.setImageResource(R.drawable.ic_newscast_globe_no04);
                        animation=0;
                        break;
                }
            }
        };

        timer.schedule(timerTask, 0, 125); /// <-- every 125 ms

    }

    private void initReceiver()
    {
        broadcastManager = LocalBroadcastManager.getInstance(WelcomeActivity.this);

        broadcastReceiver = new BroadcastReceiver()
        {
            @Override
            public void onReceive(Context context, Intent intent) {

                if(intent.hasExtra("DAO_HANDLE"))
                {
                    String status = intent.getStringExtra("DAO_HANDLE");

                    Log.v("WelcomeActivity", "DAO_HANDLE: " + status);

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
                            Log.v("WELCOME", "headDataFromDAO, link: " + link);

                            tv_welcome.setText(R.string.xml_fetch);

                            xmlHandle = null;
                            xmlHandle = new XmlHandle(WelcomeActivity.this, link, "WelcomeActivity");
                            xmlHandle.proceedTask();
                            break;
                        case "EMPTY":   //nothing in DATABASE, a whole new start...
                            cleanUpOldItemData(30);
                            break;
                        case "UPDATED": //SINGLE, for head data
                            tv_welcome.setText(R.string.dao_insert);
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
                            boundForNextActivity();
                            break;
                    }
                }
                else if(intent.hasExtra("XML_HANDLE"))
                {
                    String status = intent.getStringExtra("XML_HANDLE");

                    Log.v("WelcomeActivity", "XML_HANDLE: " + status);

                    String pubdateFromXML;
                    String pubdateFromDAO;

                    switch(status)
                    {
                        case "FETCHED":
                            dataListFromXML = xmlHandle.getDataList();
                            Log.v("WelcomeActivity", "dataListFromXML, size: " + dataListFromXML.size());

                            headDataFromXML = dataListFromXML.remove(0);
                            Log.v("WelcomeActivity", "headDataFromXML, parent: " + headDataFromXML.getParent());

                            pubdateFromXML = headDataFromXML.getPubdate();
                            pubdateFromDAO = headDataFromDAO.getPubdate();

                            if(pubdateFromDAO == null || !pubdateFromDAO.equals(pubdateFromXML))  //pubdate isn't coherent
                            {
                                headDataFromDAO.setPubdate(pubdateFromXML);

                                tv_welcome.setText(R.string.dao_update);

                                daoHandle.setTable(FEEDLIST);
                                daoHandle.updateData(headDataFromDAO);
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

        broadcastManager.registerReceiver(broadcastReceiver, new IntentFilter("WelcomeActivity"));
    }

    private void initLoading()
    {
        tv_welcome.setText(R.string.loading);
        tv_welcome.setText(R.string.dao_fetch);

        daoHandle = null;
        daoHandle = new DaoHandle(WelcomeActivity.this, FEEDLIST, "WelcomeActivity");
        daoHandle.fetchDataList();
    }

    private void nextHeadDataFromDao(int index)
    {
        headDataFromDAO = dataListFromDAO.get(index);
        indexOfDataListFromDAO += 1;

        String link = headDataFromDAO.getLink();    //where the source of rss
        Log.v("WelcomeActivity", "headDataFromDAO, link: " + link);

        tv_welcome.setText(R.string.xml_fetch);

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

        Log.v("WelcomeActivity", "before which date: " + deadline);

        tv_welcome.setText(R.string.dao_clean_up);

        daoHandle.setTable(ITEMLIST);
        daoHandle.batchDeleteItemDataWithDefaultExceptionBeforeData(deadline);
    }

    private void boundForNextActivity()
    {
        tv_welcome.setText(R.string.ready);

        Intent intent = new Intent(WelcomeActivity.this, ChanListActivity.class);

        startActivity(intent);

        finish();
    }
}
