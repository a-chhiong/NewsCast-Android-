package com.unilarm.newscast.activity;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.inputmethod.InputMethodManager;
import android.webkit.URLUtil;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.unilarm.newscast.R;
import com.unilarm.newscast.adapter.EditListAdapter;
import com.unilarm.newscast.handle.DaoHandle;
import com.unilarm.newscast.handle.XmlHandle;
import com.unilarm.newscast.model.RssData;

import java.util.ArrayList;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static com.unilarm.newscast.handle.DaoHandle.FEEDLIST;
import static com.unilarm.newscast.handle.DaoHandle.ITEMLIST;

public class SubscribeActivity extends AppCompatActivity {

    /*UI for Contents*/
    private TextView tv_title_subscribe;
    private ListView lv_core_subscribe;
    private TextView tv_core_subscribe;
    private ConstraintLayout cl_core_subscribe;
    private LinearLayout ll_toolbar_subscribe;
    private ProgressBar pb_core_subscribe;
    private EditText et_input_subscribe;

    /*UI Logic*/
    private boolean isKeyboardShown;
    private int thisHeightOfCoreFrame;
    private int lastHeightOfCoreFrame;

    /*UI for Buttons*/
    private ImageView iv_close_subscribe;
    private ImageView iv_undo_subscribe;
    private ImageView iv_save_subscribe;
    private ImageView iv_fetch_subscribe;

    /*DATA*/
    private RssData rssHeadData = new RssData();
    private ArrayList<RssData> rssDataList = new ArrayList<>();

    /*HANDLE*/
    private DaoHandle daoHandle;
    private XmlHandle xmlHandle;

    /*BROADCAST*/
    private LocalBroadcastManager broadcastManager;
    private BroadcastReceiver broadcastReceiver;

    /*ATTRIBUTES*/
    private String feed_source;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_subscribe);

        initView();

        initHandle();

        initReceiver();
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
    protected void onResume()
    {
        super.onResume();

        hideKeyboard(SubscribeActivity.this);

        cl_core_subscribe.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {

                Log.v("SubscribeActivity", "core frame trigger");

                thisHeightOfCoreFrame = cl_core_subscribe.getMeasuredHeight();

                Log.v("SubscribeActivity", "core frame height, is: " + thisHeightOfCoreFrame + ", was: " + lastHeightOfCoreFrame);

                if (thisHeightOfCoreFrame - lastHeightOfCoreFrame > 300)
                {
                    Log.v("SubscribeActivity", "core frame growing");

                    if(isKeyboardShown)
                    {
                        isKeyboardShown = false;
                    }
                }
                else if(thisHeightOfCoreFrame - lastHeightOfCoreFrame < -300)
                {
                    Log.v("SubscribeActivity", "core frame shrinking");

                    if(!isKeyboardShown)
                    {
                        isKeyboardShown = true;
                    }
                }
                else
                {
                    Log.v("SubscribeActivity", "core frame stable");
                }

                lastHeightOfCoreFrame = thisHeightOfCoreFrame;
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        finish();
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();

        daoHandle = null;
        xmlHandle = null;

        broadcastManager.unregisterReceiver(broadcastReceiver);
    }

    private void initView()
    {
        tv_title_subscribe = findViewById(R.id.tv_title_subscribe);
        lv_core_subscribe = findViewById(R.id.lv_core_subscribe);
        tv_core_subscribe = findViewById(R.id.tv_core_subscribe);
        pb_core_subscribe = findViewById(R.id.pb_core_subscribe);
        cl_core_subscribe = findViewById(R.id.cl_core_subscribe);
        ll_toolbar_subscribe = findViewById(R.id.ll_toolbar_subscribe);

        lv_core_subscribe.setVisibility(GONE);
        tv_core_subscribe.setVisibility(VISIBLE);
        pb_core_subscribe.setVisibility(GONE);

        et_input_subscribe = findViewById(R.id.et_input_subscribe);

        iv_close_subscribe = findViewById(R.id.iv_close_subscribe);
        iv_undo_subscribe = findViewById(R.id.iv_undo_subscribe);
        iv_save_subscribe = findViewById(R.id.iv_save_subscribe);
        iv_fetch_subscribe = findViewById(R.id.iv_fetch_subscribe);

        tv_title_subscribe.setText(R.string.title_subscribe);

        saveButtonEnabled(false);
    }

    private void initHandle() {


        iv_close_subscribe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(isKeyboardShown)
                {
                    hideKeyboard(SubscribeActivity.this);
                }
                else
                {
                    finish();
                }
            }
        });

        iv_undo_subscribe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                clearXmlResult();

                saveButtonEnabled(false);

                hideKeyboard(SubscribeActivity.this);
            }
        });

        iv_save_subscribe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                saveButtonEnabled(false);
                fetchButtonEnabled(false);

                lv_core_subscribe.setVisibility(GONE);
                tv_core_subscribe.setVisibility(GONE);
                pb_core_subscribe.setVisibility(VISIBLE);

                pb_core_subscribe.setMax(10);
                pb_core_subscribe.setProgress(3);

                Log.v("SubscribeActivity", "RSS Head Data: " + rssHeadData);

                daoHandle = null;
                daoHandle = new DaoHandle(SubscribeActivity.this, FEEDLIST, "SubscribeActivity");
                daoHandle.insertData(rssHeadData);
            }
        });

        iv_fetch_subscribe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String source = et_input_subscribe.getText().toString().trim();

                //clearXmlResult();

                hideKeyboard(SubscribeActivity.this);

                if(checkURL(source))
                {
                    feed_source = source;

                    fetchButtonEnabled(false);

                    Log.v("SubscribeActivity", "source: " + feed_source);

                    tv_title_subscribe.setText(R.string.title_subscribe);

                    lv_core_subscribe.setVisibility(GONE);
                    tv_core_subscribe.setVisibility(GONE);
                    pb_core_subscribe.setVisibility(VISIBLE);

                    pb_core_subscribe.setMax(10);
                    pb_core_subscribe.setProgress(5);

                    xmlHandle = null;
                    xmlHandle = new XmlHandle(SubscribeActivity.this, feed_source, "SubscribeActivity");
                    xmlHandle.proceedTask();
                }
                else
                {
                    Toast.makeText(SubscribeActivity.this, "INVALID ADDRESS", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void initReceiver()
    {
        broadcastManager = LocalBroadcastManager.getInstance(SubscribeActivity.this);

        broadcastReceiver = new BroadcastReceiver()
        {
            @Override
            public void onReceive(Context context, Intent intent) {

                if(intent.hasExtra("DAO_HANDLE"))
                {
                    String title;

                    String status = intent.getStringExtra("DAO_HANDLE");

                    Log.v("SubscribeActivity", "DAO_HANDLE: " + status);

                    switch(status)
                    {
                        case "INSERTED": //SINGLE, for head data
                            pb_core_subscribe.setProgress(8);
                            daoHandle = null;
                            daoHandle = new DaoHandle(SubscribeActivity.this, ITEMLIST, "SubscribeActivity");
                            daoHandle.batchInsertData(rssDataList);
                            break;
                        case "BATCH_INSERTED":  //MULTIPLE, for item data
                            lv_core_subscribe.setVisibility(VISIBLE);
                            tv_core_subscribe.setVisibility(GONE);
                            pb_core_subscribe.setVisibility(GONE);
                            Toast.makeText(SubscribeActivity.this, "ADDED", Toast.LENGTH_SHORT).show();
                            fetchButtonEnabled(true);
                            break;
                        case "EXISTED":
                            lv_core_subscribe.setVisibility(VISIBLE);
                            tv_core_subscribe.setVisibility(GONE);
                            pb_core_subscribe.setVisibility(GONE);
                            title = rssHeadData.getTitle();
                            Toast.makeText(SubscribeActivity.this, title + ", " + status, Toast.LENGTH_SHORT).show();
                            saveButtonEnabled(false);
                            break;
                    }
                }
                else if(intent.hasExtra("XML_HANDLE"))
                {
                    String status = intent.getStringExtra("XML_HANDLE");

                    Log.v("SubscribeActivity", "XML_HANDLE: " + status);

                    switch(status)
                    {
                        case "FETCHED":
                            rssDataList = xmlHandle.getDataList();
                            if(rssDataList != null) {
                                rssHeadData = rssDataList.remove(0);
                                tv_title_subscribe.setText(rssHeadData.getTitle());
                                adapterReload(rssDataList);
                                lv_core_subscribe.setVisibility(VISIBLE);
                                tv_core_subscribe.setVisibility(GONE);
                                pb_core_subscribe.setVisibility(GONE);
                                saveButtonEnabled(true);
                            }
                            else
                            {
                                clearXmlResult();
                                lv_core_subscribe.setVisibility(GONE);
                                tv_core_subscribe.setVisibility(VISIBLE);
                                pb_core_subscribe.setVisibility(GONE);
                                tv_core_subscribe.setText(R.string.not_supported);
                                saveButtonEnabled(false);
                            }
                            break;
                        case "EMPTY":
                            clearXmlResult();
                            lv_core_subscribe.setVisibility(GONE);
                            tv_core_subscribe.setVisibility(VISIBLE);
                            pb_core_subscribe.setVisibility(GONE);
                            tv_core_subscribe.setText(R.string.not_supported);
                            saveButtonEnabled(false);
                            break;
                    }

                    fetchButtonEnabled(true);
                }
            }
        };

        broadcastManager.registerReceiver(broadcastReceiver, new IntentFilter("SubscribeActivity"));
    }

    private void saveButtonEnabled(boolean enable)
    {
        if(enable)
        {
            iv_save_subscribe.setEnabled(true);
            iv_save_subscribe.setAlpha(1.0F);
        }
        else
        {
            iv_save_subscribe.setEnabled(false);
            iv_save_subscribe.setAlpha(0.25F);
        }
    }

    private void fetchButtonEnabled(boolean enable)
    {
        if(enable)
        {
            iv_fetch_subscribe.setEnabled(true);
            iv_fetch_subscribe.setAlpha(1.0F);
        }
        else
        {
            iv_fetch_subscribe.setEnabled(false);
            iv_fetch_subscribe.setAlpha(0.25F);
        }
    }

    private void clearXmlResult()
    {
        lv_core_subscribe.setVisibility(GONE);
        tv_core_subscribe.setVisibility(VISIBLE);
        pb_core_subscribe.setVisibility(GONE);
        et_input_subscribe.setText("");
        tv_title_subscribe.setText(R.string.title_subscribe);
    }

    private boolean checkURL(String source)
    {
        if(source == null)
        {
            return false;
        }
        else
        {
            int length = source.length();

            Log.v("SubscribeActivity", "LENGTH: " + length);

            boolean valid = URLUtil.isValidUrl(source);

            Log.v("SubscribeActivity", "VALID: " + valid);

            if (length > 0 && valid) {
                String lastPathSeg = Uri.parse(source).getLastPathSegment();

                Log.v("SubscribeActivity", "PATH: " + lastPathSeg);

                if (lastPathSeg == null) {
                    lastPathSeg = "";   /// <-- to prevent it from null
                }

                if (lastPathSeg.endsWith(".xml") || lastPathSeg.endsWith(".rss")) {
                    return true;
                } else {
                    return false;
                }
            } else {
                return false;
            }
        }
    }

    private void hideKeyboard(Activity activity)
    {
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);

        //Find the currently focused view, so we can grab the correct window token from it.
        View view = activity.getCurrentFocus();

        //If no view currently has focus, create a new one, just so we can grab a window token from it
        if (view == null)
        {
            view = new View(activity);
        }

        //If really nothing is found
        if(imm != null)
        {
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    private void adapterReload(ArrayList<RssData> datalist)
    {
        EditListAdapter editListAdapter =  new EditListAdapter(SubscribeActivity.this, datalist);

        lv_core_subscribe.setAdapter(editListAdapter);
    }
}
