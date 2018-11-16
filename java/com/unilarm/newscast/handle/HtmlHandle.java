package com.unilarm.newscast.handle;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.webkit.URLUtil;

import com.unilarm.newscast.model.RssData;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class HtmlHandle
{
    /*RECEIVER*/
    private Context context;
    private String response;
    private LocalBroadcastManager htmlBroadcastManager;
    private AsyncTask asyncTask;

    /*DATA CLASS*/
    private RssData rssDataFromExt = new RssData();

    /*DATA*/
    private String rawLink;
    private String webLink;
    private String webScript;
    private String webMediaLink;
    private String webMediaName;
    //private static final String browserAgent = "Mozilla/5.0 (Linux; Android 7.1.1; MI MAX 2 Build/NMF26F) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/58.0.3029.83 Mobile Safari/537.36";
    //private static final String browserAgent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/66.0.3359.117 Safari/537.36";
    private static final String browserAgent = "Mozilla/5.0 (Android 8.0.0; Mobile; rv:62.0) Gecko/62.0 Firefox/62.0";

    /* CONSTRUCTOR */

    public HtmlHandle(Context context, RssData data, String response)
    {
        this.context = context;
        this.rssDataFromExt = data;
        this.response = response;

        htmlBroadcastManager = LocalBroadcastManager.getInstance(this.context);
    }

    /* GETTER / SETTER */

    public String getWebLink() {
        return webLink;
    }

    public String getWebScript() {
        return webScript;
    }

    public String getWebMediaLink() {
        return webMediaLink;
    }

    public String getWebMediaFile() {
        return webMediaName;
    }

    public void setContext(Context context) {
        this.context = context;

        htmlBroadcastManager = LocalBroadcastManager.getInstance(this.context);
    }

    public void setLink(String link) {
        this.rawLink = link;
    }

    public void setResponse(String response) {
        this.response = response;
    }

    /* METHODS */

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
                htmlParser();
            }
            catch(Exception ex)
            {
                Log.v("HTML_TASK", ex.getMessage());
            }

            if(webScript == null) {
                if (webMediaName == null && webMediaLink == null) {
                    return "NOTHING_FETCHED";
                } else {

                    return "ONLY_MEDIA_FETCHED";
                }
            }
            else
            {
                if (webMediaName == null && webMediaLink == null) {
                return "ONLY_SCRIPT_FETCHED";
                } else {

                    return "COMPLETELY_FETCHED";
                }
            }
        }

        @Override
        protected void onPostExecute(String message) {
            super.onPostExecute(message);

            throwMessage(message);
        }
    }

    private String checkURL(String source)
    {
        if(source == null)
        {
            return null;
        }

        int length = source.length();

        Log.v("URL", "LENGTH: " + length);

        boolean valid = URLUtil.isValidUrl(source);

        Log.v("URL", "VALID: " + valid);

        if(length > 0 && valid)
        {
            String lastPathSeg = Uri.parse(source).getLastPathSegment();

            Log.v("URL", "PATH: " + lastPathSeg);

            if(lastPathSeg == null)
            {
                lastPathSeg = "";   /// <-- to prevent it from null
            }

            if(lastPathSeg.endsWith(".mp3") || lastPathSeg.endsWith(".mp4"))
            {
                return lastPathSeg;
            }
            else
            {
                return "";
            }
        }
        else
        {
            return "";
        }
    }

    private void htmlParser()
    {
        Uri uri;
        String text;
        Document document;
        Element element;
        Elements elements;
        String protocol;
        String server;
        String path;
        String pathSeg;
        String selector;

        rawLink = this.rssDataFromExt.getLink();

        Log.v("HTML_PARSER", "rawLink: " + rawLink);

        uri = Uri.parse(rawLink);

        protocol = uri.getScheme();
        server = uri.getAuthority();
        path = uri.getPath();

        Log.v("HTML_PARSER", "protocol: " + protocol);
        Log.v("HTML_PARSER", "server: " + server);
        Log.v("HTML_PARSER", "path: " + path);

        if(server == null)
        {
            Log.v("HTML_PARSER", "find no server");

            return;
        }

        switch (server) {
            case "www.fnn-news.com":
                /*
                 *  script source: http://www.fnn-news.com/sp/news/headlines/articles/CONN00401204.html
                 *  hint from description: http://www.fnn-news.com/news/jpg/sn2018091813_51.jpg
                 *  player source: https://ios-video.fnn-news.com/mpeg/sn2018091813_hd_300.mp4
                 * */
                pathSeg = uri.getQueryParameter("url");
                if(pathSeg == null)
                {
                    return;
                }
                if(pathSeg.contains("localtime"))
                {
                    pathSeg = pathSeg.replace("localtime", "localtime/sp");

                    webLink = "http://" + server + pathSeg;
                }
                else
                {
                    webLink = "http://" + server + "/sp/" + pathSeg;
                }
                Log.v("HTML_PARSER", "clean link: " + webLink);
                if(webLink.contains("localtime"))
                {
                    Log.v("HTML_PARSER", "not yet supported");
                }
                else
                {
                    document = Jsoup.parse(rssDataFromExt.getDescription());
                    elements = document.getElementsByTag("img"); // <-- otherwise, Elements ele = doc.select("a").select("img");
                    webMediaLink = elements.attr("src");
                    uri = Uri.parse(webMediaLink);
                    pathSeg = uri.getLastPathSegment();
                    if (pathSeg != null && pathSeg.contains("_51.jpg")) {
                        pathSeg = pathSeg.replace("_51.jpg", "_hd_300.mp4");
                    }
                    webMediaLink = "https://ios-video.fnn-news.com/mpeg/" + pathSeg;
                    Log.v("HTML_PARSER", "file path: " + webMediaLink);
                    try {
                        document = Jsoup.connect(webLink).userAgent(browserAgent).maxBodySize(50000).timeout(5000).get();
                        /*LOCATE MEDIA*/
                        //                    element = document.selectFirst("#video_html5player");
                        //                    text = element.html();
                        //                    Log.v("HTML_PARSER", "media: " + text);
                        /*LOCATE SCRIPTS*/
                        element = document.selectFirst("#content > div.mainBox > div.mainNews > div.read");
                        text = element.html();
                        Log.v("HTML_PARSER", "script: " + text);
                        Log.v("HTML_PARSER", text);
                        webScript = text.replace("<br>", " ");
                    } catch (Exception ex) {
                        Log.v("HTML_PARSER", "" + ex.getMessage());
                    }
                }
                break;
            case "news.tv-asahi.co.jp":
                /*
                 *  script source: http://news.tv-asahi.co.jp/news-international/articles/000136013.html
                 *  player source: https://ex-ann-w.webcdn.stream.ne.jp/www11/ex-ann-w/000136013.mp4
                 * */
                webLink = "http://" + server + path;
                Log.v("HTML_PARSER", "clean link: " + webLink);
                pathSeg = uri.getLastPathSegment();
                if (pathSeg != null && pathSeg.contains(".html")) {
                    pathSeg = pathSeg.replace("html", ".mp4");
                }
                webMediaLink = "https://ex-ann-w.webcdn.stream.ne.jp/www11/ex-ann-w/" + pathSeg;
                Log.v("HTML_PARSER", "file path: " + webMediaLink);
                try
                {
                    document = Jsoup.connect(webLink).userAgent(browserAgent).maxBodySize(50000).timeout(5000).get();
                    /*LOCATE MEDIA*/
                    //                    element = document.selectFirst("#videoplayer > source:nth-child(1)");
                    //                    text = element.html();
                    //                    Log.v("HTML_PARSER", "media: " + text);
                    /*LOCATE SCRIPTS*/
                    element = document.selectFirst("#news_body");
                    //element = document.selectFirst("#contents-wrap > div.con > div.wrap-container > div > section.second-box > div.maintext");
                    text = element.html();
                    Log.v("HTML_PARSER", "script: " + text);
                    webScript = text.replace("<br>", " ");
                }
                catch (Exception ex)
                {
                    Log.v("HTML_PARSER", "" + ex.getMessage());
                }
                break;
            case "www3.nhk.or.jp":
                webLink = rawLink;
                Log.v("HTML_PARSER", "clean link: " + webLink);
                webMediaLink = null;
                Log.v("HTML_PARSER", "file path: " + webMediaLink);
                try
                {
                    document = Jsoup.connect(webLink).userAgent(browserAgent).maxBodySize(50000).timeout(5000).get();
//                        selector = document.body().html();
//                        Log.v("HTML_PARSER", "selector: " + selector);
                    /*LOCATE MEDIA*/
                    //                    element = document.selectFirst("#video_html5player");
                    //                    text = element.html();
                    //                    Log.v("HTML_PARSER", "media: " + text);
                    /*LOCATE SCRIPTS*/
                    element = document.body().selectFirst("#news_textbody");
                    text = element.html() + "\n\r";
                    element = document.body().selectFirst("#news_textmore");
                    text += element.html();
                    Log.v("HTML_PARSER", "script: " + text);
                    webScript = text.replace("<br>", " ");
                }
                catch (Exception ex)
                {
                    Log.v("HTML_PARSER", "" + ex.getMessage());
                }
                break;
            case "www9.nhk.or.jp":
                webLink = rawLink;
                webMediaLink = rawLink;
                webScript = null;
                break;
            default:
                webLink = rawLink;
                webMediaLink = null;
                webScript = "No Yet Supported…";
                break;
        }

        webMediaName = checkURL(webMediaLink);

        Log.v("HTML_PARSER", "file name: " + webMediaName);
    }

    private void throwMessage(String message)
    {
        Intent intent = new Intent(response);  //package path, self defined or follow your JAVA hierarchy ...
        intent.putExtra("HTML_HANDLE", message);   //KEY & VALUE mapping...
        Log.v("HTML_HANDLE", message);
        htmlBroadcastManager.sendBroadcast(intent);
    }
}
