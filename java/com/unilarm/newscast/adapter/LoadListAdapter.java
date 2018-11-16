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

public class LoadListAdapter extends MyBaseAdapter<RssData>
{
    private LocalBroadcastManager broadcastManager;

    public LoadListAdapter(Context context, ArrayList<RssData> list)
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
            view = inflater.inflate(R.layout.adapter_load_list, null);
        }

        /*initData() from here*/

        final RssData data = list.get(position);   //position is which view is now dealt with

        /*initView() from here*/

        final ImageView iv_remove = view.findViewById(R.id.iv_remove_load_list_adapter);

        boolean tick = data.isTick();

        if(tick)
        {
            iv_remove.setEnabled(true);
            iv_remove.setVisibility(View.VISIBLE);
            //iv_remove.setImageResource(R.drawable.ic_remove_circle_outline);  /// <--- preset in XML
        }
        else
        {
            iv_remove.setEnabled(false);
            iv_remove.setVisibility(View.GONE);
        }

        LinearLayout ll_core = view.findViewById(R.id.ll_core_load_list_adapter);

        TextView tv_title = view.findViewById(R.id.tv_title_load_list_adapter);
        tv_title.setText(data.getTitle());

        TextView tv_parent = view.findViewById(R.id.tv_parent_load_list_adapter);
        tv_parent.setText(data.getParent());

        TextView tv_category = view.findViewById(R.id.tv_category_load_list_adapter);
        tv_category.setText(data.getCategory());

        TextView tv_pubdate = view.findViewById(R.id.tv_pubdate_load_list_adapter);
        tv_pubdate.setText(data.getPubdate());

        ImageView iv_arrow = view.findViewById(R.id.iv_arrow_load_list_adapter);

        /*initHandler() from here*/

        ll_core.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                boolean tick = data.isTick();

                if(tick)
                {
                    data.setTick(false);

                    iv_remove.setEnabled(false);
                    iv_remove.setVisibility(View.GONE);
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

                    iv_remove.setEnabled(true);
                    iv_remove.setVisibility(View.VISIBLE);
                    //iv_remove.setImageResource(R.drawable.ic_remove_circle_outline);  /// <--- preset in XML
                }

                return true;
            }
        });

        iv_remove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                boolean tick = data.isTick();

                if(tick)
                {
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

                    iv_remove.setEnabled(false);
                    iv_remove.setVisibility(View.GONE);
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

        Intent intent = new Intent("PlayListActivity"); //always goes to ...
        intent.putExtra("REMOVE_CLICK", index);
        broadcastManager.sendBroadcast(intent);
    }

    @Override
    protected void arrowClick(RssData data, int index)
    {
        // TODO Auto-generated method stub

        Intent intent = new Intent("PlayListActivity"); //always goes to ...
        intent.putExtra("ARROW_CLICK", index);
        broadcastManager.sendBroadcast(intent);
    }
}
