package com.unilarm.newscast.orphanage;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.unilarm.newscast.model.RssData;

import java.io.File;
import java.util.ArrayList;

import static com.unilarm.newscast.handle.DownloadHandle.MEDIA_FOLDER;

public class DeleteHandle {
    
    /*ATTRIBUTES*/

    private Context context;
    private String response;
    private ArrayList<RssData> rssDataList;
    private LocalBroadcastManager broadcastManager;
    private AsyncTask asyncTask;

    /* CONSTRUCTOR */

    public DeleteHandle(Context context, ArrayList<RssData> rssDataList, String response)
    {
        this.context = context;
        this.rssDataList = rssDataList;
        this.response = response;

        broadcastManager = LocalBroadcastManager.getInstance(this.context);
    }

    /* GETTER / SETTER */
    
    public void setContext(Context context) {
        this.context = context;

        broadcastManager = LocalBroadcastManager.getInstance(this.context);
    }

    public void setRssDataList(ArrayList<RssData> rssDataList) {
        this.rssDataList = rssDataList;
    }

    public void setResponse(String response) {
        this.response = response;
    }

    /* METHODS */

    public void proceedTask()
    {
        asyncTask = new deleteAsyncTask().execute();
    }

    public void cancelTask()
    {
        asyncTask.cancel(true);
    }

    /* https://stackoverflow.com/questions/3028306/download-a-file-with-android-and-showing-the-progress-in-a-progressdialog/3028660 */

    private class deleteAsyncTask extends AsyncTask<String, Integer, String>
    {
        
        @Override
        protected void onPreExecute()
        {
            super.onPreExecute();

            Log.v("DELETE_HANDLE", "onPreExecute");
            
        }

        @Override
        protected String doInBackground(String... strings)
        {
            Log.v("DELETE_HANDLE", "doInBackground");

            File file;

            String fileName;

            String directory = context.getFilesDir().getAbsolutePath() + "/" + MEDIA_FOLDER;

            File folder = new File(directory);

            long listSize = rssDataList.size();

            long count = 0;

            if(folder.exists())
            {
                for(RssData data: rssDataList)
                {
                    fileName = data.getMedia();

                    if(fileName != null && fileName.length() > 0)
                    {
                        file = new File(directory + "/" + fileName);

                        if(file.exists())
                        {
                            if(file.delete())
                            {
                                Log.v("DELETE_HANDLE", "file deleted");
                            }
                            else
                            {
                                Log.v("DELETE_HANDLE", "file not deleted");
                            }
                        }
                    }

                    publishProgress((int) (count * 100 / listSize));

                    if (isCancelled())
                    {
                        return "CANCEL_DELETE";
                    }
                }
            }
            else
            {
                return "FILES_NOT_EXISTED";
            }

            return null;
        }

        @Override
        protected void onProgressUpdate(Integer... progress) {
            super.onProgressUpdate(progress);

            Log.v("DELETE_HANDLE", "onProgressUpdate: " + progress[0]);

            throwProgress(progress[0]);
        }

        @Override
        protected void onPostExecute(String result) {

            Log.v("DELETE_HANDLE", "onPostExecute");

            if(result == null)    /// <-- "File Downloaded"
            {
                throwMessage("FILES_DELETED");
            }
            else    /// <-- some errors occur
            {
                throwMessage(result);
            }
        }
    }

    private void throwProgress(int progress)
    {
        Intent intent = new Intent(response);  //package path, self defined or follow your JAVA hierarchy ...
        intent.putExtra("DELETED_PROGRESS", progress);   //KEY & VALUE mapping...
        Log.v("DELETED_PROGRESS", "" + progress);
        broadcastManager.sendBroadcast(intent);
    }

    private void throwMessage(String message)
    {
        Intent intent = new Intent(response);  //package path, self defined or follow your JAVA hierarchy ...
        intent.putExtra("DELETED_STATUS", message);   //KEY & VALUE mapping...
        Log.v("DELETED_STATUS", message);
        broadcastManager.sendBroadcast(intent);
    }
}
