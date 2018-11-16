package com.unilarm.newscast.orphanage;

import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.os.AsyncTask;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.unilarm.newscast.model.RssData;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class JsonHandle
{
    private String fileName;
    private String response;
    private Context context;
    private ArrayList<RssData> rssDataList = new ArrayList<>();
    private AssetManager assetManager;
    private LocalBroadcastManager jsonBroadcastManager;
    private AsyncTask asyncTask;

    /* CONSTRUCTOR */

    public JsonHandle(Context context, String fileName, String response)
    {
        this.context = context;
        this.fileName = fileName;
        this.response = response;

        assetManager = this.context.getAssets();  //in order to use getAssets(), we need a AssetManger to catch
        jsonBroadcastManager = LocalBroadcastManager.getInstance(this.context);
    }

    /* GETTER / SETTER */

    public ArrayList<RssData> getRssDataList()
    {
        return this.rssDataList;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public void setResponse(String response) {
        this.response = response;
    }

    public void setContext(Context context) {
        this.context = context;

        assetManager = this.context.getAssets();  //in order to use getAssets(), we need a AssetManger to catch
        jsonBroadcastManager = LocalBroadcastManager.getInstance(this.context);
    }

    /* METHODS */

    /*FOR LATER IMPLEMENTATION*/
//    public void fetchThread()
//    {
//        Thread thread = new Thread(new Runnable()
//        {
//            @Override
//            public void run()
//            {
//                try
//                {
//                    //*Using "assets" as JSON file source/
//
//                    InputStream stream = assetManager.open(fileString);    //in order to use .open(), we need a InputStream to catch
//
//                    BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
//
//                    //*----------/
//
//                    StringBuilder sb = new StringBuilder();
//
//                    String str = reader.readLine();
//
//                    while (str != null)
//                    {
//                        sb.append(str + "\n");   //append String myLine to StringBuilder
//
//                        str = reader.readLine(); //read a String line and move to myLine, again...
//                    }
//
//                    //*===JSON resolve from here===/
//
//                    jsonParser(sb);
//
//                    reader.close();
//                    stream.close();
//                    //assetManager.close();    //can not be used for some reason...
//
//                    throwMessage("FETCH");
//                }
//                catch(Exception ex)
//                {
//                    Log.v("JSON_THREAD", ex.getMessage());
//                }
//            }
//
//        });
//
//        thread.start();
//    }

    public void proceedTask()
    {
        asyncTask = new FetchAsyncTask().execute();
    }

    public void cancelTask()
    {
        asyncTask.cancel(true);
    }

    private class FetchAsyncTask extends AsyncTask<String, Void, String>
    {
        @Override
        protected String doInBackground(String... strings)
        {
            try
            {
                //*Using "assets" as JSON file source/

                InputStream stream = assetManager.open(fileName);    //in order to use .open(), we need a InputStream to catch

                BufferedReader reader = new BufferedReader(new InputStreamReader(stream));

                //*----------/

                StringBuilder sb = new StringBuilder();

                String str = reader.readLine();

                while (str != null)
                {
                    sb.append(str + "\n");   //append String myLine to StringBuilder

                    str = reader.readLine(); //read a String line and move to myLine, again...
                }

                //*===JSON resolve from here===/

                jsonParser(sb);

                reader.close();
                stream.close();
                //assetManager.close();    //can not be used for some reason...

            }
            catch(Exception ex)
            {
                Log.v("JSON_TASK", ex.getMessage());
            }

            if(rssDataList.isEmpty())
            {
                return "EMPTY";
            }
            else
            {
                return "FETCHED";
            }
        }

        @Override
        protected void onPostExecute(String message) {
            super.onPostExecute(message);

            throwMessage(message);
        }
    }

    private void jsonParser(StringBuilder builder)
    {
        String jsonString = builder.toString();

        try
        {
            Log.v("JSON_PARSING", "START_DOCUMENT");

            JSONArray jsonArray = new JSONArray(jsonString);

            for(int i=0; i<jsonArray.length(); i++)
            {
                JSONObject jsonObject = jsonArray.getJSONObject(i);

                RssData data = new RssData();

                data.setParent(jsonObject.getString("source"));
                data.setTitle(jsonObject.getString("title"));
                data.setLink(jsonObject.getString("link"));
                data.setDescription(jsonObject.getString("description"));

                Log.v("JSON_PARSING", data.getParent());
                Log.v("JSON_PARSING", data.getTitle());
                Log.v("JSON_PARSING", data.getLink());
                Log.v("JSON_PARSING", data.getDescription());

                rssDataList.add(data);
            }

            Log.v("JSON_PARSING", "LIST SIZE: " + rssDataList.size());

            Log.v("JSON_PARSING", "END_DOCUMENT");
        }
        catch (Exception ex)
        {
            Log.v("JSON_PARSING", ex.getMessage());
        }
    }

    private void throwMessage(String message)
    {
        Intent intent = new Intent(response);  //package path, self defined or follow your JAVA hierarchy ...

        intent.putExtra("JSON_HANDLE", message);   //KEY & VALUE mapping...

        jsonBroadcastManager.sendBroadcast(intent);
    }
}
