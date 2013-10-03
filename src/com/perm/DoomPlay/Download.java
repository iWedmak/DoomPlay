package com.perm.DoomPlay;

import android.util.Log;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Observable;

public class Download extends Observable implements Runnable
{
    private final static byte buffer[]= new byte[1024];

    private final String filePath;
    private final long aid;
    enum States
    {
        DOWNLOADING,PAUSED,CANCELLED,COMPLETED,ERROR
    }

    private URL url;


    private int size;
    private int downloadedS;
    private States status;


    public Download(URL url,String filePath,long aid)
    {
        this.filePath = filePath;
        this.url = url;
        this.aid = aid;
        size = -1;
        downloadedS = 0;
    }


    private void download()
    {
        Thread thread = new Thread(this);
        thread.start();
    }
    public float getProgress()
    {
        return ((float)downloadedS/size)*100;
    }

    public String getUrl()
    {
        return url.toString();
    }
    public void pause()
    {
        status = States.PAUSED;
        stateChanged();
    }

    public void resume()
    {
        status = States.DOWNLOADING;
        stateChanged();
        download();
    }
    public void cancel()
    {
        status = States.CANCELLED;
        stateChanged();
    }

    private void error()
    {
        status = States.ERROR;
        stateChanged();
    }

    private void stateChanged()
    {
        setChanged();
        notifyObservers(aid);
    }


    @Override
    public void run()
    {
        RandomAccessFile file = null;
        InputStream inputStream = null;
        HttpURLConnection connection = null;
        try
        {

            connection = (HttpURLConnection)url.openConnection();

            connection.setRequestMethod("GET");
            connection.setDoOutput(true);
            connection.connect();

            int connectLength = connection.getContentLength();

            if(connectLength < 1)
                error();

            size = connectLength;

            file = new RandomAccessFile(filePath,"rw");
            file.seek(downloadedS);

            inputStream = new BufferedInputStream(url.openStream());

            int read ;

            while((read = inputStream.read(buffer)) > 0 && status == States.DOWNLOADING)
            {
                file.write(buffer,0,read);
                downloadedS += read;
            }


            if(status == States.DOWNLOADING)
            {
                status = States.COMPLETED;
                stateChanged();
            }

        }
        catch (IOException e)
        {
            error();
            e.printStackTrace();
            Log.e("TAG AUDIO",e.toString());
        }
        finally
        {


            if(file != null)
                try {
                    file.close();
                } catch (IOException e) {}
            if(inputStream != null)
                try {
                    inputStream.close();
                } catch (IOException e) {}
            if(connection != null)
                connection.disconnect();
        }


    }

    public States getStatus()
    {
        return status;
    }
    public long getSize()
    {
        return size;
    }
}