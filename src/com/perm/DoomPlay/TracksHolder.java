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
import com.perm.vkontakte.api.AudioAlbum;
import com.perm.vkontakte.api.Group;
import com.perm.vkontakte.api.User;

import java.util.ArrayList;

class TracksHolder
{
    private static volatile boolean isScanned = false;
    public static String[] allArtist;
    public static String[] allAlbums;
    private static String[] allAcordingArtists;
    public static ArrayList<Audio> audiosVk;
    public static ArrayList<Audio> allAudios;




    public final static String[] projection = {MediaStore.Audio.Media.ARTIST,MediaStore.Audio.Media.TITLE,MediaStore.Audio.Media.ALBUM_ID,MediaStore.Audio.Media.DATA};
    public final static String[] projectionPlusId = {MediaStore.Audio.Media.ARTIST,MediaStore.Audio.Media.TITLE,MediaStore.Audio.Media.ALBUM_ID,MediaStore.Audio.Media.DATA,MediaStore.Audio.Media._ID};

    private TracksHolder(){}
    public static void scanCard(Context context)
    {
        Cursor cursor = context.getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                projection,MediaStore.Audio.Media.IS_MUSIC + " != 0", null, null);

        allAudios = Audio.parseAudiosCursor(cursor);

        Cursor cursorAlbum = context.getContentResolver().query(MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI,
                new String[]{MediaStore.Audio.Albums.ALBUM,MediaStore.Audio.Albums.ARTIST },null, null,null );

        Cursor cursorArtist = context.getContentResolver().query(MediaStore.Audio.Artists.EXTERNAL_CONTENT_URI,
                new String[]{MediaStore.Audio.Artists.ARTIST},null, null,null );


        allAlbums = new String[cursorAlbum.getCount()];
        allArtist = new String[cursorArtist.getCount()];
        allAcordingArtists = new String[cursorAlbum.getCount()];

        if(cursorAlbum.moveToFirst())
        {
            do
            {
                allAlbums[cursorAlbum.getPosition()]= cursorAlbum.getString(cursorAlbum.getColumnIndex(MediaStore.Audio.Albums.ALBUM));
                allAcordingArtists[cursorAlbum.getPosition()] = cursorAlbum.getString(cursorAlbum.getColumnIndex(MediaStore.Audio.Albums.ARTIST));

            } while (cursorAlbum.moveToNext());
        }

        if(cursorArtist.moveToFirst())
        {
            do
            {
                allArtist[cursorArtist.getPosition()]= cursorArtist.getString(cursorArtist.getColumnIndex(MediaStore.Audio.Artists.ARTIST));

            } while (cursorArtist.moveToNext());
        }



        cursor.close();
        cursorAlbum.close();
        cursorArtist.close();


        Serializator<User> factory1 = new Serializator<User>(context, Serializator.FileNames.User);
        VkFrActivity.setUsers(factory1.getSerialization());

        Serializator<Group> factory2 = new Serializator<Group>(context, Serializator.FileNames.Group);
        VkGrActivity.setGroups(factory2.getSerialization());

        Serializator<Audio> factory3 = new Serializator<Audio>(context, Serializator.FileNames.Audio);
        audiosVk = factory3.getSerialization();

        Serializator<AudioAlbum> factory4 = new Serializator<AudioAlbum>(context, Serializator.FileNames.Album);
        VkAlbumsActivity.albums = factory4.getSerialization();


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
