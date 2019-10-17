package com.unilarm.newscast.adapter;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.unilarm.newscast.R;
import com.unilarm.newscast.model.RssData;

import java.util.ArrayList;

import static com.unilarm.newscast.handle.DaoHandle.SET;
import static com.unilarm.newscast.handle.DaoHandle.UNSET;

public class TimeListAdapter extends MyBaseAdapter<RssData> {

    private LocalBroadcastManager broadcastManager;

    public TimeListAdapter(Context context, ArrayList<RssData> list)
    {
        super(context, list);

        broadcastManager = LocalBroadcastManager.getInstance(super.context);
    }

    /*Main Goal here is to create A View object which will render a ListView object, and then return to the caller*/

    @Override
    public View getView(final int position, View convertView, ViewGroup parent)
    {
        View view = convertView;

        /*initialize a View object*/

        if(view == null)
        {
            view = inflater.inflate(R.layout.adapter_time_list, null);
        }

        /*initData() from here*/

        final RssData data = list.get(position);   //position is which view is now dealt with

        /*initView() from here*/

        final ImageView iv_mark = view.findViewById(R.id.iv_mark_time_list_adapter);

        boolean tick = data.isTick();

        if(tick)
        {
            iv_mark.setEnabled(true);
            iv_mark.setVisibility(View.VISIBLE);
        }
        else
        {
            iv_mark.setEnabled(false);
            iv_mark.setVisibility(View.GONE);
        }

        LinearLayout ll_core = view.findViewById(R.id.ll_core_time_list_adapter);

        TextView tv_parent = view.findViewById(R.id.tv_parent_time_list_adapter);
        tv_parent.setText(data.getParent());

        TextView tv_title = view.findViewById(R.id.tv_title_time_list_adapter);
        tv_title.setText(data.getTitle());

        TextView tv_category = view.findViewById(R.id.tv_category_time_list_adapter);
        tv_category.setText(data.getCategory());

        TextView tv_pubdate = view.findViewById(R.id.tv_pubdate_time_list_adapter);
        tv_pubdate.setText(data.getPubdate());

        ImageView iv_arrow = view.findViewById(R.id.iv_arrow_time_list_adapter);

        /*initHandler() from here*/

        ll_core.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                boolean tick = data.isTick();

                if(tick)
                {
                    data.setTick(false);

                    iv_mark.setEnabled(false);
                    iv_mark.setVisibility(View.GONE);
                }
                else
                {
                    arrowClick(data, position);
                }
            }
        });

        ll_core.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {

                boolean tick = data.isTick();

                if(!tick)
                {
                    data.setTick(true);

                    iv_mark.setVisibility(View.VISIBLE);
                    iv_mark.setEnabled(true);

                    String mark = data.getMark();

                    if (mark != null && mark.equals(SET)) {
                        iv_mark.setImageResource(R.drawable.ic_favorite);  /// <--- preset in XML
                    } else {
                        iv_mark.setImageResource(R.drawable.ic_favorite_border);  /// <--- preset in XML
                    }
                }

                return true;
            }
        });

        iv_mark.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String mark = data.getMark();

                if(mark != null && mark.equals(SET))
                {
                    data.setMark(UNSET);

                    iv_mark.setImageResource(R.drawable.ic_favorite_border);  /// <--- preset in XML

                    coreClick(data, position);
                }
                else
                {
                    data.setMark(SET);

                    iv_mark.setImageResource(R.drawable.ic_favorite);  /// <--- preset in XML

                    markClick(data, position);
                }
            }
        });

        iv_arrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                boolean tick = data.isTick();

                if(tick)
                {
                    data.setTick(false);

                    iv_mark.setEnabled(false);
                    iv_mark.setVisibility(View.GONE);
                }
                else
                {
                    arrowClick(data, position);
                }
            }
        });

        return view;
    }


    @Override
    protected void coreClick(RssData data, int index)
    {
        // TODO Auto-generated method stub

        Intent intent = new Intent("TimeListActivity"); //always goes to ...
        intent.putExtra("UNMARK_CLICK", index);
        broadcastManager.sendBroadcast(intent);
    }

    @Override
    protected void coreLongClick(RssData data, int index)
    {
        // TODO Auto-generated method stub
    }

    @Override
    protected void markClick(RssData data, int index)
    {
        // TODO Auto-generated method stub

        Intent intent = new Intent("TimeListActivity"); //always goes to ...
        intent.putExtra("MARK_CLICK", index);
        broadcastManager.sendBroadcast(intent);
    }

    @Override
    protected void arrowClick(RssData data, int index)
    {
        // TODO Auto-generated method stub

        Intent intent = new Intent("TimeListActivity"); //always goes to ...
        intent.putExtra("ARROW_CLICK", index);
        broadcastManager.sendBroadcast(intent);
    }
}