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

import android.content.Context;
import android.database.Cursor;
import android.provider.MediaStore;
import com.api.AudioAlbum;
import com.api.Group;
import com.api.User;

import java.util.ArrayList;

class TracksHolder
{
    public static ArrayList<Audio> audiosVk;
    private static volatile boolean isScanned = false;
    public static String[] allArtist;
    public static String[] allAlbums;
    private static String[] allAcordingArtists;
    public static ArrayList<Audio> allAudios;




    public final static String[] projection = {MediaStore.Audio.Media.ARTIST,MediaStore.Audio.Media.TITLE,MediaStore.Audio.Media.ALBUM_ID,MediaStore.Audio.Media.DATA};
    public final static String[] projectionPlusId = {MediaStore.Audio.Media.ARTIST,MediaStore.Audio.Media.TITLE,MediaStore.Audio.Media.ALBUM_ID,MediaStore.Audio.Media.DATA,MediaStore.Audio.Media._ID};

    private TracksHolder(){}
    public static void scanCard(Context context)
    {
        Cursor cursor = context.getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                projection,null, null, null);

        if(cursor != null)
        {
            allAudios = Audio.parseAudiosCursor(cursor);
            cursor.close();
        }
        else
        {
             allAudios = new ArrayList<Audio>();
        }


        Cursor cursorAlbum = context.getContentResolver().query(MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI,
                new String[]{MediaStore.Audio.Albums.ALBUM,MediaStore.Audio.Albums.ARTIST },null, null,null );

        if(cursorAlbum != null)
        {
            allAlbums = new String[cursorAlbum.getCount()];
            allAcordingArtists = new String[cursorAlbum.getCount()];

            if(cursorAlbum.moveToFirst())
            {
                do
                {
                    allAlbums[cursorAlbum.getPosition()]= cursorAlbum.getString(cursorAlbum.getColumnIndex(MediaStore.Audio.Albums.ALBUM));
                    allAcordingArtists[cursorAlbum.getPosition()] = cursorAlbum.getString(cursorAlbum.getColumnIndex(MediaStore.Audio.Albums.ARTIST));

                } while (cursorAlbum.moveToNext());
            }

            cursorAlbum.close();
        }
        else
        {
            allAlbums = new String[0];
            allArtist = new String[0];
        }

        Cursor cursorArtist = context.getContentResolver().query(MediaStore.Audio.Artists.EXTERNAL_CONTENT_URI,
                new String[]{MediaStore.Audio.Artists.ARTIST},null, null,null );


        if(cursorArtist != null)
        {
            allArtist = new String[cursorArtist.getCount()];

            if(cursorArtist.moveToFirst())
            {
                do
                {
                    allArtist[cursorArtist.getPosition()]= cursorArtist.getString(cursorArtist.getColumnIndex(MediaStore.Audio.Artists.ARTIST));

                } while (cursorArtist.moveToNext());
            }
            cursorArtist.close();

        }
        else
        {
            allArtist = new String[0];
        }


        Serializator<User> serializatorUser = new Serializator<User>(context, Serializator.FileNames.User);
        VkFrActivity.setUsers(serializatorUser.getSerialization());

        Serializator<Group> serializatorGroup = new Serializator<Group>(context, Serializator.FileNames.Group);
        VkGrActivity.setGroups(serializatorGroup.getSerialization());

        Serializator<AudioAlbum> serializatorAlbum = new Serializator<AudioAlbum>(context, Serializator.FileNames.Album);
        VkAlbumsActivity.albums = serializatorAlbum.getSerialization();


        isScanned = true;
    }


    public static String getArtistFromAlbum(int positionAlbum)
    {
        return allAcordingArtists[positionAlbum];
    }

    public static boolean isScanned()
    {
        return isScanned;
    }
}
