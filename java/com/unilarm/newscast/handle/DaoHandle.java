package com.unilarm.newscast.handle;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.unilarm.newscast.model.RssData;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

public class DaoHandle
{
    /*CONSTANT*/
    private static final String DATABASE = "newscast.sqlite";
    public static final String FEEDLIST = "FEEDLIST";
    public static final String ITEMLIST = "ITEMLIST";
    public static final String PARENT = "parent";
    public static final String TITLE = "title";
    public static final String LINK = "link";
    public static final String DATE = "pubdate";
    public static final String MARK = "mark";
    public static final String MEDIA = "media";
    public static final String SET = "1";
    public static final String UNSET = "0";
    public static final String ASC = "ASC";
    public static final String DESC = "DESC";

    /*ATTRIBUTES*/
    private Context context;
    private String table;
    private String response;
    private RssData rssData = new RssData();
    private ArrayList<RssData> rssDataList = new ArrayList<>();
    private LocalBroadcastManager daoBroadcastManager;
    private AsyncTask asyncTask;
    private Thread myThread;

    /* CONSTRUCTOR */

    public DaoHandle(Context context, String table, String response)
    {
        this.context = context;
        this.table = table;
        this.response = response;

        daoBroadcastManager = LocalBroadcastManager.getInstance(this.context);
    }

    /* GETTER / SETTER */

    public ArrayList<RssData> getRssDataList()
    {
        return this.rssDataList;
    }

    public void setContext(Context context) {
        this.context = context;

        daoBroadcastManager = LocalBroadcastManager.getInstance(this.context);
    }

    public void setTable(String table) {
        this.table = table;
    }

    public void setResponse(String response) {
        this.response = response;
    }

    /* METHODS */

    /*
     *  SQL ACCESS Public Method
     * */

    public void fetchDataList()
    {
        myThread = new Thread(new Runnable() {
            @Override
            public void run() {

                String message;

                rssDataList = getDataList();

                if(rssDataList.isEmpty())
                {
                    message = "EMPTY";
                }
                else
                {
                    Log.v("DAO_HANDLE", "List Size: " + rssDataList.size());

                    message = "FETCHED";
                }

                throwMessage(message);
            }
        });

        myThread.start();
    }

    public void fetchDataList(final String where, final String like, final String order, final String sort)
    {
        myThread = new Thread(new Runnable() {
            @Override
            public void run() {

                String message;

                rssDataList = getDataList(where, like, order, sort);

                if(rssDataList.isEmpty())
                {
                    message = "EMPTY";
                }
                else
                {
                    message = "FETCHED";
                }

                throwMessage(message);
            }
        });

        myThread.start();
    }

    public void fetchDataListFuzzy(final String where, final String like)
    {
        myThread = new Thread(new Runnable() {
            @Override
            public void run() {

                String message;

                rssDataList = getDataListFuzzy(where, like);

                if(rssDataList.isEmpty())
                {
                    message = "EMPTY";
                }
                else
                {
                    Log.v("DAO_HANDLE", "List Size: " + rssDataList.size());

                    message = "FETCHED";
                }

                throwMessage(message);
            }
        });

        myThread.start();
    }


    public void fetchDataListFuzzy(final String where, final String like, final String order, final String sort)
    {
        myThread = new Thread(new Runnable() {
            @Override
            public void run() {

                String message;

                rssDataList = getDataListFuzzy(where, like, order, sort);

                if(rssDataList.isEmpty())
                {
                    message = "EMPTY";
                }
                else
                {
                    message = "FETCHED";
                }

                throwMessage(message);
            }
        });

        myThread.start();
    }

    public void fetchDataListMedia(final String where, final String like, final String order, final String sort)
    {
        myThread = new Thread(new Runnable() {
            @Override
            public void run() {

                String message;

                rssDataList = getDataListMedia(where, like, order, sort);

                if(rssDataList.isEmpty())
                {
                    message = "EMPTY";
                }
                else
                {
                    message = "FETCHED";
                }

                throwMessage(message);
            }
        });

        myThread.start();
    }

    public void insertData(final RssData data)
    {
        myThread = new Thread(new Runnable() {
            @Override
            public void run() {

                int sid = checkData(data);

                String message;

                if(sid > 0)
                {
                    message = "EXISTED";
                }
                else
                {
                    insert(data);

                    message = "INSERTED";
                }

                throwMessage(message);
            }
        });

        myThread.start();
    }

    public void insertDataWithUpdate(final RssData data)
    {
        myThread = new Thread(new Runnable() {
            @Override
            public void run() {

                int sid = checkData(data);

                String message;

                if(sid > 0)
                {
                    update(data);

                    message = "UPDATED_INSTEAD";
                }
                else
                {
                    insert(data);

                    message = "INSERTED";
                }

                throwMessage(message);
            }
        });

        myThread.start();
    }

    public void updateData(final RssData data)
    {
        myThread = new Thread(new Runnable() {
            @Override
            public void run() {

                String message;

                int sid = checkData(data);

                if(sid > 0)
                {
                    update(data);

                    message = "UPDATED";
                }
                else
                {
                    insert(data);

                    message = "INSERTED_INSTEAD";
                }

                throwMessage(message);

            }
        });

        myThread.start();
    }

    public void batchInsertData(final ArrayList<RssData> list)
    {
        myThread = new Thread(new Runnable() {
            @Override
            public void run() {

                String message;

                int sid;

                if(list == null || list.size() == 0)
                {
                    message = "NOTHING_INSERTED";
                }
                else
                {
                    for(RssData data: list)
                    {
                        sid = checkData(data);

                        if(sid < 0)
                        {
                            insert(data);
                        }
                    }

                    message = "BATCH_INSERTED";
                }

                throwMessage(message);
            }
        });

        myThread.start();
    }

    public void batchInsertDataWithUpdate(final ArrayList<RssData> list)
    {
        myThread = new Thread(new Runnable() {
            @Override
            public void run() {

                String message;

                int sid;

                if(list == null || list.size() == 0)
                {
                    message = "NOTHING_INSERTED";
                }
                else
                {
                    for(RssData data: list)
                    {
                        sid = checkData(data);

                        if(sid > 0)
                        {
                            update(data);
                        }
                        else
                        {
                            insert(data);
                        }
                    }

                    message = "BATCH_INSERTED";
                }

                throwMessage(message);
            }
        });

        myThread.start();
    }

    public void batchUpdateData(final ArrayList<RssData> list)
    {
        myThread = new Thread(new Runnable() {
            @Override
            public void run() {

                String message;

                if(list == null || list.size() == 0)
                {
                    message = "NOTHING_UPDATED";
                }
                else
                {
                    for(RssData data: list)
                    {
                        update(data);
                    }

                    message = "BATCH_UPDATED";
                }


                throwMessage(message);
            }
        });

        myThread.start();

    }

    public void deleteData(final RssData data)
    {
        myThread = new Thread(new Runnable() {
            @Override
            public void run() {

                int sid_ext = data.getSid();
                int sid_int = checkData(data);

                String message;

                if(sid_ext == sid_int)
                {
                    delete(data);

                    message = "DELETED";
                }
                else
                {
                    message = "NOTHING_DELETED";
                }

                throwMessage(message);
            }
        });

        myThread.start();

    }

    public void batchDeleteItemDataWithDefaultExceptionBeforeData(final String date)
    {
        myThread = new Thread(new Runnable() {
            @Override
            public void run() {

                String message;

                rssDataList = getItemDataWithDefaultExceptionBeforeDate(date);

                if(rssDataList == null || rssDataList.size() == 0)
                {
                    message =  "NOTHING_DELETED";
                }
                else
                {
                    for(RssData data: rssDataList)
                    {
                        delete(data);
                    }

                    message = "BATCH_DELETED";
                }


                throwMessage(message);
            }
        });

        myThread.start();

    }

    public void batchDeleteItemDataWithDefaultExceptionAndParentIs(final String parent)
    {
        myThread = new Thread(new Runnable() {
            @Override
            public void run() {

                String message;

                rssDataList = getItemDataWithDefaultExceptionAndParentIs(parent);

                if(rssDataList == null || rssDataList.size() == 0)
                {
                    message=  "NOTHING_DELETED";
                }
                else
                {
                    for(RssData data: rssDataList)
                    {
                        delete(data);
                    }

                    message = "BATCH_DELETED";
                }

                throwMessage(message);
            }
        });


        myThread.start();

    }

    private void throwMessage(String message)
    {
        Intent intent = new Intent(response);  //package path, self defined or follow your JAVA hierarchy ...

        intent.putExtra("DAO_HANDLE", message);   //KEY & VALUE mapping...

        Log.v("DAO_HANDLE", message);

        daoBroadcastManager.sendBroadcast(intent);
    }

    /*
     *  SQL ACCESS Extended Method
     * */

    private ArrayList<RssData> getDataList()
    {
        /*USING Cursor to receive the model's raw data by SQL command*/

        SQLiteDatabase db = openReadDatabase();

        String sql = "SELECT * FROM " + table;

        Cursor cursor = db.rawQuery(sql, null);

        ArrayList<RssData> dataList = new ArrayList<>();

        if(cursor.moveToFirst()) //是否需要移動到第一筆。
        {
            do
            {
                RssData data = new RssData();

                data.fromCursor(cursor);

                dataList.add(data);
            }
            while(cursor.moveToNext()); //是否需要移動到下一筆。
        }

        cursor.close();

        db.close();

        return dataList;
    }

    private ArrayList<RssData> getDataList(String where, String like, String order, String sort)
    {
        /*USING Cursor to receive the model's raw data by SQL command*/

        if(like.contains("'"))
        {
            like = like.replace("'", "_");
        }

        SQLiteDatabase db = openReadDatabase();

        String sql = "SELECT * FROM " + table + " WHERE " + where +" LIKE '" + like + "' ORDER BY " + order + " " + sort;

        Cursor cursor = db.rawQuery(sql, null);

        ArrayList<RssData> dataList = new ArrayList<>();

        if(cursor.moveToFirst()) //是否需要移動到第一筆。
        {
            do
            {
                RssData data = new RssData();

                data.fromCursor(cursor);

                dataList.add(data);
            }
            while(cursor.moveToNext()); //是否需要移動到下一筆。
        }

        cursor.close();

        db.close();

        return dataList;
    }

    private ArrayList<RssData> getDataListMedia(String where, String like, String order, String sort)
    {
        /*USING Cursor to receive the model's raw data by SQL command*/

        SQLiteDatabase db = openReadDatabase();

        String sql;

        ArrayList<RssData> list = new ArrayList<>();

        if(where != null && like != null)
        {
            if(like.contains("'"))
            {
                like = like.replace("'", "_");
            }

            sql = "SELECT * FROM " + table + " WHERE " + where + " LIKE '" + like + "' AND ( " +  DaoHandle.MEDIA + " LIKE '%.mp3' OR " + DaoHandle.MEDIA + " LIKE '%.mp4' ) ";
        }
        else if(order != null && sort != null)
        {
            sql = "SELECT * FROM " + table + " WHERE ( " + DaoHandle.MEDIA + " LIKE '%.mp3' OR " + DaoHandle.MEDIA + " LIKE '%.mp4' ) ORDER BY " +  order + " " + sort;
        }
        else
        {
            return list;
        }

        Cursor cursor = db.rawQuery(sql, null);

        if(cursor.moveToFirst()) //是否需要移動到第一筆。
        {
            do
            {
                RssData data = new RssData();

                data.fromCursor(cursor);

                list.add(data);
            }
            while(cursor.moveToNext()); //是否需要移動到下一筆。
        }

        cursor.close();

        db.close();

        return list;
    }

    private ArrayList<RssData> getDataListFuzzy(String where, String like, String order, String sort)
    {
        /*USING Cursor to receive the model's raw data by SQL command*/

        if(like.contains("'"))
        {
            like = like.replace("'", "_");
        }

        SQLiteDatabase db = openReadDatabase();

        String sql = "SELECT * FROM " + table + " WHERE " + where +" LIKE '%" + like + "%' ORDER BY " + order + " " + sort;

        Cursor cursor = db.rawQuery(sql, null);

        ArrayList<RssData> dataList = new ArrayList<>();

        if(cursor.moveToFirst()) //是否需要移動到第一筆。
        {
            do
            {
                RssData data = new RssData();

                data.fromCursor(cursor);

                dataList.add(data);
            }
            while(cursor.moveToNext()); //是否需要移動到下一筆。
        }

        cursor.close();

        db.close();

        return dataList;
    }

    private ArrayList<RssData> getDataListFuzzy(String where, String like)
    {
        /*USING Cursor to receive the model's raw data by SQL command*/

        if(like.contains("'"))
        {
            like = like.replace("'", "_");
        }

        SQLiteDatabase db = openReadDatabase();

        String sql = "SELECT * FROM " + table + " WHERE " + where + " LIKE '%" + like + "%'";

        Cursor cursor = db.rawQuery(sql, null);

        ArrayList<RssData> dataList = new ArrayList<>();

        if(cursor.moveToFirst()) //是否需要移動到第一筆。
        {
            do
            {
                RssData data = new RssData();

                data.fromCursor(cursor);

                dataList.add(data);
            }
            while(cursor.moveToNext()); //是否需要移動到下一筆。
        }

        cursor.close();

        db.close();

        return dataList;
    }

    private ArrayList<RssData> getItemDataWithDefaultExceptionBeforeDate(String date)
    {
        /*USING Cursor to receive the model's raw data by SQL command*/

        SQLiteDatabase db = openReadDatabase();

        String sql = "SELECT * FROM " + ITEMLIST + " WHERE pubdate < '" + date + "' AND ( mark IS NULL OR mark NOT LIKE '" + SET + "' ) AND ( media IS NULL OR media NOT LIKE '%.mp4' OR media NOT LIKE '%.mp3' )";

        Cursor cursor = db.rawQuery(sql, null);

        ArrayList<RssData> dataList = new ArrayList<>();

        if(cursor.moveToFirst()) //是否需要移動到第一筆。
        {
            do
            {
                RssData data = new RssData();

                data.fromCursor(cursor);

                dataList.add(data);
            }
            while(cursor.moveToNext()); //是否需要移動到下一筆。
        }

        cursor.close();

        db.close();

        return dataList;
    }

    private ArrayList<RssData> getItemDataWithDefaultExceptionAndParentIs(String parent)
    {
        /*USING Cursor to receive the model's raw data by SQL command*/

        if(parent.contains("'"))
        {
            parent = parent.replace("'", "_");
        }

        SQLiteDatabase db = openReadDatabase();

        String sql = "SELECT * FROM " + ITEMLIST + " WHERE " + DaoHandle.PARENT + " LIKE '" + parent + "' AND ( mark IS NULL OR mark NOT LIKE '" + SET + "' ) AND ( media IS NULL OR media NOT LIKE '%.mp4' OR media NOT LIKE '%.mp3' )";

        Cursor cursor = db.rawQuery(sql, null);

        ArrayList<RssData> dataList = new ArrayList<>();

        if(cursor.moveToFirst()) //是否需要移動到第一筆。
        {
            do
            {
                RssData data = new RssData();

                data.fromCursor(cursor);

                dataList.add(data);
            }
            while(cursor.moveToNext()); //是否需要移動到下一筆。
        }

        cursor.close();

        db.close();

        return dataList;
    }

    /*
     *  SQL ACCESS Extended Method
     * */

    private int checkData(RssData data)
    {
        String title = data.getTitle();
        String link = data.getLink();

        data = getOneBy(title, link);

        if(data != null)
        {
            return data.getSid();
        }
        else
        {
            return -1;
        }
    }

    /*
     *  SQL ACCESS Basic Method
     * */

    private RssData getOneBy(int sid)
    {
        /*USING Cursor to receive the database's raw data by SQL command*/

        SQLiteDatabase db = openReadDatabase();

        String sql = "SELECT * FROM " + table + " WHERE sid LIKE '" + sid + "'";

        Log.v("DAO_HANDLE", "SQL COMMAND: " + sql);

        Cursor cursor = db.rawQuery(sql, null);  //USING Cursor to acquire the database's raw data, by using SQL command

        int count = cursor.getCount();

        Log.v("DAO_HANDLE", "CURSOR COUNT: " + count);

        RssData data = null;

        if(count > 0)
        {
            if(cursor.moveToFirst()) //是否需要移動到第一筆。
            {
                data = new RssData();

                data.fromCursor(cursor);
            }
        }

        cursor.close();

        Log.v("DAO_HANDLE", "CURSOR_CLOSE");

        db.close();

        Log.v("DAO_HANDLE", "DB_CLOSE");

        return data;
    }

    private RssData getOneBy(String title, String link)
    {
        /*USING Cursor to receive the database's raw data by SQL command*/

        if(title.contains("'"))
        {
            title = title.replace("'", "_");
        }

        SQLiteDatabase db = openReadDatabase();

        String sql = "SELECT * FROM " + table + " WHERE " + DaoHandle.TITLE + " LIKE '" + title + "' AND " + DaoHandle.LINK + " LIKE '" + link + "'";

        Log.v("DAO_HANDLE", "SQL COMMAND: " + sql);

        Cursor cursor = db.rawQuery(sql, null);  //USING Cursor to acquire the database's raw data, by using SQL command

        int count = cursor.getCount();

        Log.v("DAO_HANDLE", "CURSOR COUNT: " + count);

        RssData data = null;

        if(count > 0)
        {
            if(cursor.moveToFirst()) //是否需要移動到第一筆。
            {
                data = new RssData();

                data.fromCursor(cursor);
            }
        }

        cursor.close();

        Log.v("DAO_HANDLE", "CURSOR_CLOSE");

        db.close();

        Log.v("DAO_HANDLE", "DB_CLOSE");

        return data;
    }


    private void insert(RssData data)
    {
        Log.v("DAO_HANDLE", "DB_OPEN");

        SQLiteDatabase db = openWriteDatabase();

        //this will return a sid value, but it's ignored now.

        Log.v("DAO_HANDLE", "INSERT_START");

        db.insert(table, null, data.toContentValues(table));

        Log.v("DAO_HANDLE", "INSERT_END");

        db.close();

        Log.v("DAO_HANDLE", "DB_CLOSE");
    }

    private void update(RssData data)
    {
        Log.v("DAO_HANDLE", "DB_OPEN");

        SQLiteDatabase db = openWriteDatabase();

        //這個方法，會自動從 ContactData 物件裏面，取得作爲 id 的值，然後以此值去更新對應的條目。

        Log.v("DAO_HANDLE", "UPDATE_START");

        db.update(table, data.toContentValues(table), "sid=?", new String[]{"" + data.getSid()});

        Log.v("DAO_HANDLE", "UPDATE_END");

        db.close();

        Log.v("DAO_HANDLE", "DB_CLOSE");
    }

    private void delete(RssData data)
    {
        Log.v("DAO_HANDLE", "DB_OPEN");

        SQLiteDatabase db = openWriteDatabase();

        //這個方法，會自動從 ContactData 物件裏面，取得作爲 id 的值，然後以此值去更新對應的條目。

        Log.v("DAO_HANDLE", "DELETE_START");

        db.delete(table, "sid=?", new String[]{"" + data.getSid()});

        Log.v("DAO_HANDLE", "DELETE_END");

        db.close();

        Log.v("DAO_HANDLE", "DB_CLOSE");
    }

    /*
     *  SQL ACCESS CORE Method
     * */

    private void copyDatabase(File dbFile) throws IOException
    {
        InputStream is = context.getAssets().open(DATABASE);
        OutputStream os = new FileOutputStream(dbFile);

        byte[] buffer = new byte[1024];

        int read = is.read(buffer);

        while (read != -1)
        {
            os.write(buffer, 0, read);

            read = is.read(buffer);
        }

        os.flush();
        os.close();
        is.close();
    }

    public SQLiteDatabase openReadDatabase()
    {
        File dbFile = context.getDatabasePath(DATABASE);

        if (!dbFile.exists())
        {
            try
            {
                File parentDir = new File(dbFile.getParent());

                if (!parentDir.exists())
                {
                    parentDir.mkdir();
                }

                copyDatabase(dbFile);
            }
            catch (IOException e)
            {
                throw new RuntimeException("Error creating source model", e);
            }
        }

        return SQLiteDatabase.openDatabase(dbFile.getPath(), null, SQLiteDatabase.OPEN_READONLY);
    }

    public SQLiteDatabase openWriteDatabase()
    {
        File dbFile = context.getDatabasePath(DATABASE);

        if (!dbFile.exists())
        {
            try
            {
                File parentDir = new File(dbFile.getParent());

                if (!parentDir.exists())
                {
                    parentDir.mkdir();
                }

                copyDatabase(dbFile);
            }
            catch (IOException e)
            {
                throw new RuntimeException("Error creating source model", e);
            }
        }

        return SQLiteDatabase.openDatabase(dbFile.getPath(), null, SQLiteDatabase.OPEN_READWRITE);
    }

}
