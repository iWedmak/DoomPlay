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
import android.support.v4.util.LruCache;
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

/*
      class for find and save cover art
 */

abstract class AlbumArtGetter extends AsyncTask<Void,Void,Void>
{
    private final static String lastFmApiId = "2827ff9b2eb0158b80e7c6d0b511f25d";
    private static final Uri artworkUri = Uri.parse("content://media/external/audio/albumart");
    private final String artist;
    private final String title;
    private final long albumId;
    private static final HashSet<Long> set = new HashSet<Long>();

    protected abstract void onBitmapSaved(long albumId);

    // checks whether the cover is downloading
    static boolean isLoadById(long id)
    {
        return set.contains(id);
    }


    public AlbumArtGetter(long albumId,String artist,String title)
    {
        super();
        this.albumId = albumId;
        this.artist = artist;
        this.title = title;
        set.add(albumId);
    }

    //find , download and save
    @Override
    protected Void doInBackground(Void... params)
    {
        String src ;
        try
        {
            src = findSrc(artist,title);



        } catch (ParserConfigurationException e) {
            e.printStackTrace(); cancel(true);  return null;
        } catch (SAXException e) {
            e.printStackTrace(); cancel(true);  return null;
        } catch (IOException e) {
            e.printStackTrace(); cancel(true);  return null;
        }
        if(src == null || src.equals(""))
        {
            cancel(false);
            return null;
        }

        Bitmap bitmap = downloadBitmap(src);
        if(bitmap != null)
        {
             insertBitmapInMediaStore(bitmap,albumId);
        }

        return null;
    }

    @Override
    protected void onCancelled()
    {
        super.onCancelled();
        set.remove(albumId);
    }

    @Override
    protected void onPostExecute(Void aVoid)
    {
        super.onPostExecute(aVoid);

        onBitmapSaved(albumId);
        set.remove(albumId);
    }


    // return cover art's url was found from lastFm or return null if wasn't
    private static String findSrc(String artist,String title) throws ParserConfigurationException, SAXException, IOException
    {
        title ="&track=" + URLEncoder.encode(title,"utf-8");
        if(artist != null && !artist.equals("")  && !artist.equals("<unknown>"))
            artist ="&artist=" + URLEncoder.encode(artist,"utf-8");
        else
            artist = "";

        URL url = new URL("http://ws.audioscrobbler.com/2.0/?method=track.search&limit=1"+artist+title+"&api_key=" +lastFmApiId);

        HttpURLConnection connection=null;
        try
        {
            connection = (HttpURLConnection)url.openConnection();
            connection.setConnectTimeout(50000);
            connection.setReadTimeout(50000);
            connection.setUseCaches(false);
            connection.setDoInput(true);
            connection.setRequestMethod("GET");

            DocumentBuilder docBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Document doc = docBuilder.parse(connection.getInputStream());
            connection.disconnect();

            doc.getDocumentElement().normalize();

            NodeList nodeList = doc.getElementsByTagName("image");

            if(nodeList != null && nodeList.getLength() != 0)
                return nodeList.item(SettingActivity.getPreference("qualityart")+1).getTextContent();
            else
                return null;


        }
        finally
        {
            if(connection!=null)
                connection.disconnect();
        }

    }

    // return the cover art downloaded from source
    private static Bitmap downloadBitmap(String source)
    {

        HttpURLConnection connection = null;
        InputStream in = null;
        try
        {

            URL url = new URL(source);
            connection =(HttpURLConnection)url.openConnection();
            connection.setDoInput(true);
            connection.connect();
            in = connection.getInputStream();
            return  BitmapFactory.decodeStream(in);
        }
        catch (IOException e)
        {
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

    //insert the cover art into mediaStore
    private static void insertBitmapInMediaStore(Bitmap bitmap, long albumId)
    {
        OutputStream stream;
        File file  = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/download/albumArts/");
        if(!file.exists())
            file.mkdir();
        String path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/download/albumArts/" + String.valueOf(albumId) + ".jpeg";

        try
        {
            stream = new FileOutputStream(path);
        }
        catch (FileNotFoundException e)
        {
            e.printStackTrace();
            return;
        }
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
        try
        {
            stream.flush();
            stream.close();
        } catch (IOException e) {}



        ContentValues cv = new ContentValues();
        cv.put("album_id", albumId);
        cv.put("_data", path);
        MyApplication.getInstance().getContentResolver().insert(artworkUri, cv);
    }

    //return the cover art by id

    private static LruCache<Long,Bitmap> bitmapCache = new LruCache<Long,Bitmap>(2 * 1024 * 1024)
    {
        @Override
        protected int sizeOf(Long key, Bitmap value)
        {
            return value.getByteCount();
        }

        @Override
        protected Bitmap create(Long key)
        {
            if(key < 1)
                return null;

            Bitmap bitmap = AlbumArtGetter.getBitmapFromStore(key, MyApplication.getInstance());

            if(bitmap != null)
                put(key,bitmap);

            return bitmap;
        }
    };
    private static Bitmap getBitmapFromStore(long id, Context context)
    {
        Uri uri = ContentUris.withAppendedId(artworkUri, id);
        try
        {
            return MediaStore.Images.Media.getBitmap(context.getContentResolver(),uri);
        }
        catch (IOException e)
        {
            return null;
        }
    }
    public static Bitmap getCoverArt(long id)
    {
        return bitmapCache.get(id);
    }
}
