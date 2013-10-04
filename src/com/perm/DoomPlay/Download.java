package com.perm.DoomPlay;

/*
 *    Copyright 2013 Vladislav Krot
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 *
 *    You can contact me <DoomPlaye@gmail.com>
 */
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class Download implements Runnable
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

        notifyObservers();
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

    private List<DoomObserver> observers = new ArrayList<DoomObserver>();

    public void addObserver(DoomObserver observer)
    {
        observers.add(observer);
    }

    protected void notifyObservers()
    {
        for(DoomObserver observer : observers)
        {
            observer.doomUpdate(this,aid);
        }
    }
}