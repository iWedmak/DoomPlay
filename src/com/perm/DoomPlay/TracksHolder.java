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
import com.perm.vkontakte.api.Audio;

import java.util.ArrayList;

public final class TracksHolder
{
    public static volatile boolean isScanned = false;
    public static String[] songAllPath;
    public static String[] songArtist;
    public static String[] songTitle;
    public static int[] songAlbumId;
    public static String[] allArtist;
    public static String[] allAlbums;
    public static String[] allAcordingArtists;
    public static ArrayList<Audio> tempAudiosMine ;

    private TracksHolder(){}
    public static void scanCard(Context context)
    {
        Cursor cursor = context.getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                MainScreenActivity.STAR,MediaStore.Audio.Media.IS_MUSIC + " != 0", null, null);


        songAlbumId = new int[cursor.getCount()];
        songArtist = new String[cursor.getCount()];
        songTitle = new String[cursor.getCount()];
        songAllPath = new String[cursor.getCount()];

        if(cursor.moveToFirst())
        {
            do
            {
                songAllPath[cursor.getPosition()]= cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA));
                songAlbumId[cursor.getPosition()]= cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID));
                songArtist[cursor.getPosition()]= cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST));
                songTitle[cursor.getPosition()]= cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE));

            }
            while (cursor.moveToNext());
        }

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

        isScanned = true;
    }

}
