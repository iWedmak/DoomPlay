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
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;

public class Download implements Runnable
{

    static interface DoomObserver
    {
        //my own observer with blackjacks and hookers
        // don't ask ,why have i done this;
        void doomUpdate(long aid);
    }
    enum States
    {
        DOWNLOADING,PAUSED,CANCELLED,COMPLETED,ERROR
    }

    private final URL url;
    private long size;
    private long downloadedS;
    private States status;
    private final static byte buffer[] = new byte[1024];
    final String filePath;
    private final long aid;
    private final DoomObserver observer ;

    public Download(URL url,String filePath,long aid,DoomObserver observer)
    {
        this.filePath = filePath;
        this.url = url;
        this.aid = aid;
        this.observer = observer;
        size = -1;
        downloadedS = 0;
        status = States.PAUSED;
    }


    private void download()
    {
        Thread thread = new Thread(this);
        thread.start();
    }

    //TODO: doesn't work , getContentLength always return 172;
    public int getProgress()
    {
        return (int)((downloadedS*100)/size);
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
        observer.doomUpdate(aid);
    }


    @Override
    public void run()
    {
        RandomAccessFile file = null;
        BufferedInputStream inputStream = null;
        HttpURLConnection connection = null;
        try
        {

            connection = (HttpURLConnection)url.openConnection();

            connection.setRequestMethod("GET");
            connection.setDoOutput(true);
            connection.connect();

            String connectLength = connection.getHeaderField("Content-Length");



            if(connectLength == null || Long.parseLong(connectLength) < 1 || connectLength.equals(""))
                error();

            size = Long.parseLong(connectLength);

            file = new RandomAccessFile(filePath,"rw");
            file.seek(downloadedS);


            inputStream = new BufferedInputStream(url.openStream());
            inputStream.skip(downloadedS);


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
}