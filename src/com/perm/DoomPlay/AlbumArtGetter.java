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
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashSet;

public abstract class AlbumArtGetter extends AsyncTask<Void,Void,Bitmap>
{
    public final static String lastFmApiId = "2827ff9b2eb0158b80e7c6d0b511f25d";
    boolean isLoading = false;
    String artist;
    String title;
    long albumId;
    static HashSet<Long> set = new HashSet<Long>();

    abstract void onGetBitmap(Bitmap bitmap);


    public AlbumArtGetter(long albumId,String artist,String title)
    {
        super();
        this.albumId = albumId;
        this.artist = artist;
        this.title = title;
        set.add(albumId);
    }

    @Override
    protected Bitmap doInBackground(Void... params)
    {
        isLoading = true;
        String src ;
        try {
            src = findSrc(artist,title);




        } catch (ParserConfigurationException e) {
            e.printStackTrace();   return null;
        } catch (SAXException e) {
            e.printStackTrace();  return null;
        } catch (IOException e) {
            e.printStackTrace();  return null;
        }
        if(src == null || src.equals(""))
            return null;
        Log.d("TAG URL", src);
        return downloadBitmap(src);
    }

    @Override
    protected void onPostExecute(Bitmap bitmap)
    {
        super.onPostExecute(bitmap);
        isLoading = false;
        set.remove(albumId);
        if(bitmap != null)
        {
            onGetBitmap(bitmap);
            if(SettingActivity.getPreferences(MyApplication.getInstance(),"saveart"))
            {
                new AsyncTask<Bitmap,Void,Void>()
                {
                    @Override
                    protected Void doInBackground(Bitmap... params)
                    {
                        insertBitmapInMediaStore(params[0],albumId);
                        return null;
                    }

                }.execute(bitmap);
            }

        }


    }


    static String findSrc(String artist,String title) throws ParserConfigurationException, SAXException, IOException
    {

        URL url = new URL("http://ws.audioscrobbler.com/2.0/?method=track.search&limit=1&track="
                +URLEncoder.encode(title,"utf-8")+"&artist="
                +URLEncoder.encode(artist,"utf-8")+"&api_key=" +lastFmApiId);

        HttpURLConnection connection=null;
        try
        {
            connection = (HttpURLConnection)url.openConnection();
            connection.setConnectTimeout(50000);
            connection.setReadTimeout(50000);
            connection.setUseCaches(false);
            connection.setDoInput(true);
            connection.setRequestMethod("GET");

            DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
            Document doc = docBuilder.parse(connection.getInputStream());
            connection.disconnect();

            doc.getDocumentElement().normalize();
            NodeList nodeList = doc.getElementsByTagName("image");

            if(nodeList != null && nodeList.getLength() != 0)
                return nodeList.item(1).getTextContent();
            else
                return null;


        }
        finally
        {
            if(connection!=null)
                connection.disconnect();
        }

    }

    static Bitmap downloadBitmap(String src)
    {

        HttpURLConnection connection = null;
        InputStream in = null;
        try
        {

            URL url = new URL(src);
            connection =(HttpURLConnection)url.openConnection();
            connection.setDoInput(true);
            connection.connect();
            in = connection.getInputStream();
            BitmapFactory.decodeStream(in);
            return  BitmapFactory.decodeStream(in);
        }
        catch (IOException e)
        {
            Log.e("TAG EXCEPTION",e.toString());
            return null;
        }
        finally
        {
            if(connection != null)
                connection.disconnect();
            if(in != null)
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
        }
    }
    public static void insertBitmapInMediaStore(Bitmap bitmap,long albumId)
    {
        OutputStream stream;
        String path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/download/" + String.valueOf(albumId);
        try
        {
            stream = new FileOutputStream(path);
        }
        catch (FileNotFoundException e)
        {
            e.printStackTrace();
            Log.e("FILENOTFOUND",e.toString());
            return;
        }
        bitmap.compress(Bitmap.CompressFormat.JPEG, 300, stream);
        try
        {
            stream.flush();
            stream.close();
        } catch (IOException e) {
            e.printStackTrace(); Log.e("EXCEPTION",e.toString());
        }



        ContentValues cv = new ContentValues();
        cv.put("album_id", albumId);
        cv.put("_data", path);
        MyApplication.getInstance().getContentResolver().insert(Song.artworkUri, cv);
    }

    public static Bitmap getBitmapById(long id,Context context)
    {
        Uri uri = ContentUris.withAppendedId(Song.artworkUri, id);
        try
        {
            return MediaStore.Images.Media.getBitmap(context.getContentResolver(),uri);
        }
        catch (IOException e)
        {
            return null;
        }
    }
}
