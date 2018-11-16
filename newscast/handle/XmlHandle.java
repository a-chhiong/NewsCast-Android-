package com.unilarm.newscast.handle;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.util.Xml;

import com.unilarm.newscast.model.RssData;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import org.xmlpull.v1.XmlPullParser;

import static org.xmlpull.v1.XmlPullParser.END_DOCUMENT;
import static org.xmlpull.v1.XmlPullParser.END_TAG;
import static org.xmlpull.v1.XmlPullParser.START_DOCUMENT;
import static org.xmlpull.v1.XmlPullParser.START_TAG;
import static org.xmlpull.v1.XmlPullParser.TEXT;

public class XmlHandle
{
    private String urlSource;
    private String response;
    private Context context;
    private RssData rssData;
    private ArrayList<RssData> dataList;
    private LocalBroadcastManager xmlBroadcastManager;
    private AsyncTask asyncTask;

    /* CONSTRUCTOR */

    public XmlHandle(Context context, String urlSource, String response)
    {
        this.context = context;
        this.urlSource = urlSource;
        this.response = response;

        xmlBroadcastManager = LocalBroadcastManager.getInstance(this.context);
    }

    /* GETTER / SETTER */

    public ArrayList<RssData> getDataList()
    {
        return this.dataList;
    }

    public void setContext(Context context) {
        this.context = context;

        xmlBroadcastManager = LocalBroadcastManager.getInstance(this.context);
    }

    public void setUrlSource(String urlSource) {
        this.urlSource = urlSource;
    }

    public void setResponse(String response) {
        this.response = response;
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
//                    URL myUrl = new URL(urlSource);
//
//                    HttpURLConnection urlConn = (HttpURLConnection) myUrl.openConnection();
//                    urlConn.setReadTimeout(10000 /* milliseconds */);
//                    urlConn.setConnectTimeout(15000 /* milliseconds */);
//                    urlConn.setRequestMethod("GET");
//                    urlConn.setDoInput(true);
//
//                    urlConn.connect();   // Starts the query
//
//                    InputStream stream = new BufferedInputStream(urlConn.getInputStream());
//
//                    InputStreamReader reader = new InputStreamReader(stream);
//
//                    xmlParser(reader);
//
//                    stream.close();
//
//                    urlConn.disconnect();
//
//                    throwMessage("THREAD");
//                }
//                catch(Exception ex)
//                {
//                    Log.v("XML_THREAD", ex.getMessage());
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

    private class FetchAsyncTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... strings)
        {
            String result = "";

            try
            {
                URL myUrl = new URL(urlSource);

                HttpURLConnection urlConn = (HttpURLConnection) myUrl.openConnection();

                urlConn.setReadTimeout(1000 /* milliseconds */);
                urlConn.setConnectTimeout(15000 /* milliseconds */);
                urlConn.setRequestMethod("GET");
                urlConn.setDoInput(true);

                urlConn.connect();   // Starts the query

                InputStream stream = new BufferedInputStream(urlConn.getInputStream());

                InputStreamReader reader = new InputStreamReader(stream);

                xmlParser(reader);

                stream.close();

                urlConn.disconnect();
            }
            catch(Exception ex)
            {
                Log.v("XML_TASK", ex.getMessage());
            }
            finally
            {

                if(dataList == null)
                {
                    result = "EMPTY";
                }
                else
                {
                    if(dataList.size() > 0)
                    {
                        result = "FETCHED";
                    }
                    else
                    {
                        result = "EMPTY";
                    }
                }
            }

            return result;
        }

        @Override
        protected void onPostExecute(String message) {
            super.onPostExecute(message);

            throwMessage(message);

            Log.v("XML_HANDLE", message);
        }
    }

    private void xmlParser(InputStreamReader reader)
    {
        String tagName = "";
        String tagStore = "";
        String parentOfItemData = "";

        int event;

        boolean rss_head = false;
        boolean rss_item = false;

        try
        {
            XmlPullParser parser = Xml.newPullParser();

            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);

            parser.setInput(reader);

            event = parser.getEventType();

            if(event == START_DOCUMENT)
            {
                Log.v("XML_PARSING", "START_DOCUMENT");

                dataList = new ArrayList<>();

                do
                {
                    switch (event)
                    {
                        case START_TAG:
                            tagName = parser.getName();
                            tagStore = "";
							switch (tagName)
							{
								case "channel":
                                    /*
                                     *  To renew a RssData Object for coming HeadData
                                     *  To renew a RssDataList Object for coming RssDataList
                                     * */
									if(!rss_head)
									{
										rss_head = true;

										rssData = new RssData();
									}
									break;
								case "item":
									if(rss_head)
									{
										rss_head = false;   //to give way for item...

										if(rssData != null)
										{
										    String temp = rssData.getLink();
                                            rssData.setParent(temp);   //data swapping for DB table...
                                            rssData.setLink(urlSource);
                                            String parent =  rssData.getParent();
                                            String link = rssData.getLink();
											dataList.add(rssData);
											int index = dataList.size() - 1;
                                            Log.v("XML_PARSING", "[" + index + "], RssHeadData, parent: " + parent);
                                            Log.v("XML_PARSING", "[" + index + "], RssHeadData, link: " + link);
                                            parentOfItemData = rssData.getTitle(); //for coming items to use...
										}
									}
									/*
									 *  To renew a RssData Object for coming ItemData
                                     * */
                                    if(!rss_item)
                                    {
                                        rss_item = true;

                                        rssData = new RssData();
                                    }
									break;
                                case "enclosure":
                                    rssData.setLink(parser.getAttributeValue(null, "url"));
                                    Log.v("XML_PARSING", "enclosure: " + rssData.getLink());
                                    break;
								default:
									tagStore = tagName;
									break;
							}
                            break;

                        case TEXT:
                            switch(tagStore)
                            {
                                case "title":
                                    rssData.setTitle(parser.getText());
                                    break;
                                case "link":
                                    rssData.setLink(parser.getText());
                                    Log.v("XML_PARSING", "link: " + rssData.getLink());
                                    break;
                                case "description":
                                    rssData.setDescription(parser.getText());
                                    break;
                                case "category":
                                    rssData.setCategory(parser.getText());
                                    break;
                                case "copyright":
                                    rssData.setCopyright(parser.getText());
                                    break;
                                case "lastBuildDate":
                                case "pubDate":
                                    String text = parser.getText();
                                    try
                                    {
                                        DateFormat dateFormat = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss Z", Locale.getDefault());
                                        Date date = (Date) dateFormat.parse(text);
                                        dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                                        text = dateFormat.format(date);
                                    }
                                    catch(Exception ex)
                                    {
                                        Log.v("RssData", ""+ ex.getMessage());
                                    }
                                    rssData.setPubdate(text);
                                    Log.v("RssData", "pubDate: " + text);
                                    break;
                            }
                            break;
                        case END_TAG:
                            tagName = parser.getName();
                            tagStore = "";
                            if(tagName.equals("item"))
                            {
                                if (rss_item)
                                {
                                    rss_item = false;

                                    if(rssData != null)
                                    {
                                        rssData.setParent(parentOfItemData);
                                        String link = rssData.getLink();
                                        dataList.add(rssData);
                                        int index = dataList.size() - 1;
                                        Log.v("XML_PARSING", "[" + index + "], RssItemData, link: " + link);
                                    }
                                }
                            }
                            break;
                    }

                    event = parser.next();
                }
                while (event != END_DOCUMENT);

                Log.v("XML_PARSING", "END_DOCUMENT");

                int size = dataList.size();

                Log.v("XML_PARSING", "RssDataList, size: " + size);
            }
        }
        catch (Exception ex)
        {
            ex.printStackTrace();

            Log.v("XML_PARSING", ex.getMessage());
        }
    }

    private void throwMessage(String message)
    {
        Intent intent = new Intent(response);  //package path, self defined or follow your JAVA hierarchy ...

        intent.putExtra("XML_HANDLE", message);   //KEY & VALUE mapping...

        xmlBroadcastManager.sendBroadcast(intent);
    }
}