package com.unilarm.newscast.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.widget.BaseAdapter;

import java.util.ArrayList;

public abstract class MyBaseAdapter<T> extends BaseAdapter {

    protected Context context;
    protected LayoutInflater inflater;
    protected ArrayList<T> list;

    public MyBaseAdapter(Context context, ArrayList<T> list)
    {
        super();

        this.context = context;
        this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.list = list;
    }

    public ArrayList<T> getList()
    {
        return list;
    }

    public void setList(ArrayList<T> list)
    {
        this.list = list;
        //notifyDataSetChanged();
    }

    protected abstract void markClick(T data, int index);
    protected abstract void coreClick(T data, int index);
    protected abstract void coreLongClick(T data, int index);
    protected abstract void arrowClick(T data, int index);

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public T getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

}
