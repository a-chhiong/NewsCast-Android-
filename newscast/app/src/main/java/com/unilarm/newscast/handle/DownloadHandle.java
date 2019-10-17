package com.unilarm.newscast.handle;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.PowerManager;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.unilarm.newscast.model.RssData;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class DownloadHandle
{

    /*ATTRIBUTES*/

    private Context context;
    private String fileLink;
    private String fileName;
    private String response;
    private File localFile;
    private LocalBroadcastManager downloadBroadcastManager;
    private AsyncTask asyncTask;

    public static String MEDIA_FOLDER = "media";

    /* CONSTRUCTOR */

//    public DownloadHandle(Context context, RssData rssData, String response)
//    {
//        this.context = context;
//        this.fileLink = rssData.getLink();
//        this.fileName = rssData.getMedia();
//        this.response = response;
//
//        downloadBroadcastManager = LocalBroadcastManager.getInstance(this.context);
//    }

    public DownloadHandle(Context context, String fileLink, String fileName, String response)
    {
        this.context = context;
        this.fileLink = fileLink;
        this.fileName = fileName;
        this.response = response;

        downloadBroadcastManager = LocalBroadcastManager.getInstance(this.context);
    }

    /* GETTER / SETTER */

    public File getLocalFile() {
        return localFile;
    }

    public void setContext(Context context) {
        this.context = context;

        downloadBroadcastManager = LocalBroadcastManager.getInstance(this.context);
    }

    public void setFileLink(String fileLink) {
        this.fileLink = fileLink;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public void setResponse(String response) {
        this.response = response;
    }

    /* METHODS */

    public void proceedTask()
    {
        asyncTask = new downloadAsyncTask().execute();
    }

    public void cancelTask()
    {
        asyncTask.cancel(true);
    }

    /* https://stackoverflow.com/questions/3028306/download-a-file-with-android-and-showing-the-progress-in-a-progressdialog/3028660 */

    private class downloadAsyncTask extends AsyncTask<String, Integer, String>
    {
        PowerManager powerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        PowerManager.WakeLock wakeLock;

        @Override
        protected void onPreExecute()
        {
            super.onPreExecute();

            Log.v("DOWNLOAD_HANDLE", "onPreExecute");

            /*
             *  take CPU lock to prevent CPU from going off if the user
             *  presses the power button during download
             */

            if (powerManager != null)
            {
                wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, getClass().getName());

                wakeLock.acquire(100000L);
            }
        }

        @Override
        protected String doInBackground(String... strings)
        {
            Log.v("DOWNLOAD_HANDLE", "doInBackground");

            InputStream input = null;

            OutputStream output = null;

            HttpURLConnection connection = null;

            File file;

            File folder;

            try
            {
                URL url = new URL(fileLink);

                connection = (HttpURLConnection) url.openConnection();

                connection.connect();

                /*
                 *   expect HTTP 200 OK, so we don't mistakenly save error report
                 *    instead of the file
                 */

                if (connection.getResponseCode() != HttpURLConnection.HTTP_OK)
                {
                    return "HTTP: " + connection.getResponseCode() + ", " + connection.getResponseMessage();
                }

                /*
                 *  this will be useful to display download percentage
                 *  might be -1: server did not report the length
                 */

                int fileLength = connection.getContentLength();

                /* download the file */

                input = connection.getInputStream();

                /* https://developer.android.com/guide/topics/data/data-storage */

                /* https://developer.android.com/training/permissions/requesting#java */

                /*
                if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState()))    /// <-- check if external storage is available
                {
                    file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), fileName);
                }
                else    /// <-- otherwise directing to APP's own folder
                {
                    file = new File(context.getFilesDir(), fileName);
                }
                */

                /*
                *  every media file is stored in .../app_directory/named_folder/named_file.mp4 or .mp3
                * **/

                Log.v("DOWNLOAD_HANDLE", "Folder: " + MEDIA_FOLDER);

                String directory = context.getFilesDir().getAbsolutePath() + "/" + MEDIA_FOLDER;

                Log.v("DOWNLOAD_HANDLE", "Directory: " + directory);

                folder = new File(directory);

                if(folder.exists())
                {
                    /* file = new File(context.getFilesDir(), fileName); */

                    file = new File(directory + "/" + fileName);

                    Log.v("DOWNLOAD_HANDLE", "Path: " + file.toString());
                }
                else
                {
                    if(folder.mkdirs())
                    {
                        file = new File(directory + "/" + fileName);

                        Log.v("DOWNLOAD_HANDLE", "Path: " + file.toString());
                    }
                    else
                    {
                        return "Directory_Error";
                    }
                }

                if(file.exists())
                {
                    Log.v("DOWNLOAD_HANDLE", "File Path: " + file.toString());

                    if(!file.delete())
                    {
                        return "File_Existed";
                    }
                }

                /*
                *   Real Stuff From HERE...
                * **/


                Log.v("DOWNLOAD_HANDLE", "Start Downloading");

                output = new FileOutputStream(file);

                byte data[] = new byte[4096];   /// <-- buffer

                long total = 0;

                int count;

                while ((count = input.read(data)) != -1)
                {
                    /*  allow canceling with back button */

                    if (isCancelled())
                    {
                        input.close();

                        return "Download_Cancelled";
                    }

                    total += count;

                    /*  publishing the progress.... */

                    if (fileLength > 0) /// <-- only if total length is known
                    {
                        publishProgress((int) (total * 100 / fileLength));
                    }

                    output.write(data, 0, count);
                }

                localFile = file;
            }
            catch (Exception ex)
            {
                return ex.getMessage();
            }
            finally
            {
                try
                {
                    if (output != null)
                    {
                        output.close();
                    }
                    if (input != null)
                    {
                        input.close();
                    }
                }
                catch (IOException ignored)
                {

                }

                if (connection != null)
                {
                    connection.disconnect();
                }
            }

            return null;
        }

        @Override
        protected void onProgressUpdate(Integer... progress) {
            super.onProgressUpdate(progress);

            Log.v("DOWNLOAD_HANDLE", "onProgressUpdate: " + progress[0]);

            throwProgress(progress[0]);
        }

        @Override
        protected void onPostExecute(String result) {

            Log.v("DOWNLOAD_HANDLE", "onPostExecute");

            wakeLock.release();

            if(result == null)    /// <-- "File Downloaded"
            {
                throwMessage("File_Downloaded");
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
        intent.putExtra("DOWNLOAD_PROGRESS", progress);   //KEY & VALUE mapping...
        Log.v("DOWNLOAD_PROGRESS", "" + progress);
        downloadBroadcastManager.sendBroadcast(intent);
    }

    private void throwMessage(String message)
    {
        Intent intent = new Intent(response);  //package path, self defined or follow your JAVA hierarchy ...
        intent.putExtra("DOWNLOAD_STATUS", message);   //KEY & VALUE mapping...
        Log.v("DOWNLOAD_STATUS", message);
        downloadBroadcastManager.sendBroadcast(intent);
    }
}
