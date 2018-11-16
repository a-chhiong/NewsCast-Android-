package com.unilarm.newscast.model;

import android.content.ContentValues;
import android.database.Cursor;

import android.util.Log;

import java.io.Serializable;

import static com.unilarm.newscast.handle.DaoHandle.FEEDLIST;
import static com.unilarm.newscast.handle.DaoHandle.ITEMLIST;

public class RssData  implements Serializable
{

    /*ATTRIBUTES*/
    private boolean tick;
    private int sid;
    private String parent;
    private String title;
    private String link;
    private String description;
    private String pubdate;
    private String copyright;
    private String category;
    private String mark;
    private String script;
    private String media;

    public RssData() {
    }

    /*BASIC GETTER*/

    public boolean isTick() {
        return tick;
    }

    public int getSid() {
        return sid;
    }

    public String getParent() {
        return parent;
    }

    public String getTitle()
    {
        return title;
    }

    public String getLink() {
        return link;
    }

    public String getDescription() {
        return description;
    }

    public String getPubdate() {
        return pubdate;
    }

    public String getCopyright() {
        return copyright;
    }

    public String getCategory() {
        return category;
    }

    public String getMark() {
        return mark;
    }

    public String getMedia() {
        return media;
    }

    public String getScript() {
        return script;
    }

    /*BASIC SETTER*/

    public void setTick(boolean tick) {
        this.tick = tick;
    }

    public void setSid(int sid) {
        this.sid = sid;
    }

    public void setParent(String parent) {
        this.parent = parent;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setPubdate(String pubdate)
    {
        this.pubdate = pubdate;
    }

    public void setCopyright(String copyright) {
        this.copyright = copyright;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public void setMark(String mark) {
        this.mark = mark;
    }

    public void setMedia(String media) {
        this.media = media;
    }

    public void setScript(String script) {
        this.script = script;
    }

    /*ADVANCED GETTER*/

    public ContentValues toContentValues(String table) {

        ContentValues cv = new ContentValues();

        switch(table)
        {
            case FEEDLIST:
                cv.put("copyright", copyright);
                Log.v("DATABASE", "copyright: " + copyright);
                break;
            case ITEMLIST:
                cv.put("category", category);
                Log.v("DATABASE", "category: " + category);
                cv.put("media", media);
                Log.v("DATABASE", "media: " + media);
                cv.put("script", script);
                Log.v("DATABASE", "script: " + script);
                break;
        }

        cv.put("parent", parent);
        Log.v("DATABASE", "parent: " + parent);
        cv.put("title", title);
        Log.v("DATABASE", "title: " + title);
        cv.put("link", link);
        Log.v("DATABASE", "link: " + link);
        cv.put("description", description);
        Log.v("DATABASE", "description: " + description);
        cv.put("pubdate", pubdate);
        Log.v("DATABASE", "pubdate: " + pubdate);
        cv.put("mark", mark);
        Log.v("DATABASE", "mark: " + mark);

        return cv;
    }

    /*ADVANCED SETTER*/

    public void fromCursor(Cursor cursor)
    {
        Log.v("DATABASE", "cursor position: " + cursor.getPosition());

        int count = cursor.getColumnCount();

        for(int index = 0; index < count; index++)
        {
            String name = cursor.getColumnName(index);

            switch(name)
            {
                case "sid":
                    sid = cursor.getInt(cursor.getColumnIndex(name));
                    Log.v("DATABASE", name + ": " + sid);
                    break;
                case "parent":
                    parent = cursor.getString(cursor.getColumnIndex(name));
                    Log.v("DATABASE", name + ": " + parent);
                    break;
                case "copyright":
                    copyright = cursor.getString(cursor.getColumnIndex(name));
                    Log.v("DATABASE", name + ": " + copyright);
                    break;
                case "category":
                    category = cursor.getString(cursor.getColumnIndex(name));
                    Log.v("DATABASE", name + ": " + category);
                    break;
                case "title":
                    title = cursor.getString(cursor.getColumnIndex(name));
                    Log.v("DATABASE", name + ": " + title);
                    break;
                case "link":
                    link = cursor.getString(cursor.getColumnIndex(name));
                    Log.v("DATABASE", name + ": " + link);
                    break;
                case "description":
                    description = cursor.getString(cursor.getColumnIndex(name));
                    Log.v("DATABASE", name + ": " + description);
                    break;
                case "pubdate":
                    pubdate = cursor.getString(cursor.getColumnIndex(name));
                    Log.v("DATABASE", name + ": " + pubdate);
                    break;
                case "mark":
                    mark = cursor.getString(cursor.getColumnIndex(name));
                    Log.v("DATABASE", name + ": " + mark);
                    break;
                case "script":
                    script = cursor.getString(cursor.getColumnIndex(name));
                    Log.v("DATABASE", name + ": " + script);
                    break;
                case "media":
                    media = cursor.getString(cursor.getColumnIndex(name));
                    Log.v("DATABASE", name + ": " + media);
                    break;
            }
        }
    }
}
