package com.unilarm.newscast.adapter;

import android.content.Context;
import android.support.v4.content.LocalBroadcastManager;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.unilarm.newscast.R;
import com.unilarm.newscast.model.RssData;

import java.util.ArrayList;

public class EditListAdapter extends MyBaseAdapter<RssData> {

    private LocalBroadcastManager broadcastManager;

    public EditListAdapter(Context context, ArrayList<RssData> list)
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
            view = inflater.inflate(R.layout.adapter_subscribe, null);
        }

        /*initData() from here*/

        final RssData data = list.get(position);   //position is which view is now dealt with

        /*initView() from here*/

        TextView tv_parent = view.findViewById(R.id.tv_parent_edit_list_adapter);
        tv_parent.setText(data.getParent());

        TextView tv_title = view.findViewById(R.id.tv_title_edit_list_adapter);
        tv_title.setText(data.getTitle());

        TextView tv_category = view.findViewById(R.id.tv_category_edit_list_adapter);
        tv_category.setText(data.getCategory());

        TextView tv_pubdate = view.findViewById(R.id.tv_pubdate_edit_list_adapter);
        tv_pubdate.setText(data.getPubdate());

        /*initHandler() from here*/

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

    }

    @Override
    protected void arrowClick(RssData data, int index)
    {
        // TODO Auto-generated method stub

    }
}