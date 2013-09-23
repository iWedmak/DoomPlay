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

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.perm.vkontakte.api.Audio;

import java.util.ArrayList;

import static android.provider.MediaStore.Audio.Media;

public class PlaylistDB extends SQLiteOpenHelper
{

    private static final int DATABASE_VERSION = 6;
    private static final String DATABASE_NAME = "playlistsData";
    private static final String TABLE_LISTPLAYLIST = "listplaylist";
    private static final String KEY_POSITION_TRACK = "postiontak";
    private static final String TABLE_DEFAULT = "defaultTable";
    private static final String KEY_NAME_PLAYLIST = "playlistName";





    private PlaylistDB(Context context)
    {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    private static volatile PlaylistDB instance;

    public static PlaylistDB getInstance(Context context)
    {
        PlaylistDB temp = instance;
        if(temp == null)
        {
            synchronized (PlaylistDB.class)
            {
                temp = instance;
                if(temp == null)
                    instance = temp = new PlaylistDB(context);
            }
        }
        return temp;
    }

    @Override
    public void onCreate(SQLiteDatabase db)
    {
        String createListPlaylistTable = "CREATE TABLE " + TABLE_LISTPLAYLIST + "(" + Media._ID +
                " INTEGER PRIMARY KEY AUTOINCREMENT," + KEY_NAME_PLAYLIST + " TEXT" +")";

        String createDefaultPlaylistTable = "CREATE TABLE IF NOT EXISTS " + TABLE_DEFAULT + "("
                + Media._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," + Media.DATA + " TEXT,"+
                Media.ARTIST + " TEXT,"+ Media.TITLE + " TEXT," + Media.ALBUM_ID +" LONG," + KEY_POSITION_TRACK + " INTEGER"+ ")";

        db.execSQL(createDefaultPlaylistTable);
        db.execSQL(createListPlaylistTable);

        ContentValues cv = new ContentValues();
        cv.put(KEY_NAME_PLAYLIST, TABLE_DEFAULT);
        db.insert(TABLE_LISTPLAYLIST,null,cv);

        String[] tables = getListPlaylistForDatabase(db);
        for(String table : tables )
        {
            if(!table.equals(TABLE_DEFAULT))
            {
                String createTable = "CREATE TABLE IF NOT EXISTS " + table + "("
                    + Media._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," + Media.DATA + " TEXT,"+
                    Media.ARTIST + " TEXT,"+ Media.TITLE + " TEXT," + Media.ALBUM_ID +" LONG," + KEY_POSITION_TRACK + " INTEGER"+ ")";
                db.execSQL(createTable);
            }
        }
    }
    public int getLastPosition(String playlist, SQLiteDatabase db)
    {
        Cursor c = db.query(playlist,new String[]{Media._ID},null, null, null, null, null);
        return c.getCount() ;
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
    {
        String[] tables = getListPlaylistForDatabase(db);
        for(String table : tables)
        {
            db.execSQL("DROP TABLE IF EXISTS " + table);
        }
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_LISTPLAYLIST);
        onCreate(db);
    }
    public void addTracks(ArrayList<Audio> audios, String playlist)
    {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv;
        int position = getLastPosition(playlist,db);

        for(Audio audio : audios)
        {
            cv = new ContentValues();
            cv.put(Media.DATA, audio.url);
            cv.put(Media.ARTIST, audio.artist);
            cv.put(Media.ALBUM_ID, audio.aid);
            cv.put(Media.TITLE, audio.title);
            cv.put(KEY_POSITION_TRACK,position);
            position++;
            db.insert(playlist, null, cv);
        }
        db.close();
    }
    public void addTrack(Audio audio,String playlist)
    {
        SQLiteDatabase db = getWritableDatabase();
        int position = getLastPosition(playlist,db);

        ContentValues cv = new ContentValues();
        cv.put(Media.DATA, audio.url);
        cv.put(Media.ARTIST, audio.artist);
        cv.put(Media.ALBUM_ID, audio.aid);
        cv.put(Media.TITLE, audio.title);
        cv.put(KEY_POSITION_TRACK,position);
        db.insert(playlist, null, cv);
        db.close();
    }

    public void deleteTrack(int positionTrack,String playlist)
    {
        SQLiteDatabase db = getWritableDatabase();
        db.delete(playlist, KEY_POSITION_TRACK + " = ?", new String[]{String.valueOf(positionTrack)});

        db.close();
    }
    public void setAcordingPositions(int positionTrack,String playlist)
    {
        SQLiteDatabase db = getWritableDatabase();
        int lastPos = getLastPosition(playlist,db);
        int pos = positionTrack;

        for(int i = pos ; i < lastPos  ; i++)
        {
            ContentValues cv = new ContentValues();
            cv.put(KEY_POSITION_TRACK,pos);
            db.update(playlist,cv,KEY_POSITION_TRACK + " = ?",new String[]{String.valueOf(pos+1)});
            pos ++ ;
        }
        db.close();
    }

    public ArrayList<Audio> getTracks(String playlist)
    {
        SQLiteDatabase db = getWritableDatabase();

        Cursor c = db.query(playlist,TracksHolder.projection,null, null, null, null, null);

        c.moveToFirst();
        ArrayList<Audio> result = Audio.parseAudio(c);

        c.close();
        db.close();
        return result;


    }
    public String[] getListPlaylistForDatabase(SQLiteDatabase db)
    {
        Cursor c = db.query(TABLE_LISTPLAYLIST,new String[]{KEY_NAME_PLAYLIST},null,null,null,null,null);

        String[] result = new String[c.getCount()];

        if (c.moveToFirst())
        {
            do
            {
                result[c.getPosition()] = (c.getString(c.getColumnIndex(KEY_NAME_PLAYLIST)));

            }while(c.moveToNext());
        }
        c.close();
        return  result;
    }
    public String[] getListPlaylist()
    {

        SQLiteDatabase db = getWritableDatabase();
        Cursor c = db.query(TABLE_LISTPLAYLIST,new String[]{KEY_NAME_PLAYLIST},null,null,null,null,null);

        String[] result = new String[c.getCount()];

        if (c.moveToFirst())
        {
            do
            {
                result[c.getPosition()] = (c.getString(c.getColumnIndex(KEY_NAME_PLAYLIST)));

            }while(c.moveToNext());
        }
        c.close();
        db.close();

        return  result;
    }
    public void deletePlaylist(String playlist)
    {
        SQLiteDatabase db = getWritableDatabase();
        db.delete(TABLE_LISTPLAYLIST,KEY_NAME_PLAYLIST + " = ?", new String[]{playlist});
        db.delete(playlist,null,null);
        db.execSQL("DROP TABLE IF EXISTS " + playlist);
        db.close();
    }
    public void changeColumns(String playlist , int first,int second)
    {
        SQLiteDatabase db = getWritableDatabase();

        Cursor cFirst = db.query(playlist,TracksHolder.projectionPlusId,KEY_POSITION_TRACK + " = ?",new String[]{String.valueOf(first)},null,null,null);
        cFirst.moveToFirst();
        String idFirst = cFirst.getString(cFirst.getColumnIndex(Media._ID));
        String trackFirst = cFirst.getString(cFirst.getColumnIndex(Media.DATA));
        cFirst.close();

        Cursor cSecond = db.query(playlist,TracksHolder.projectionPlusId,KEY_POSITION_TRACK + " = ?",new String[]{String.valueOf(second)},null,null,null);
        cSecond.moveToFirst();
        String idSecond = cSecond.getString(cSecond.getColumnIndex(Media._ID));
        String trackSecond = cSecond.getString(cSecond.getColumnIndex(Media.DATA));
        cSecond.close();



        ContentValues cv = new ContentValues();
        cv.put (KEY_POSITION_TRACK, first);
        cv.put(Media.DATA,trackSecond);

        ContentValues cv2 = new ContentValues();
        cv2.put (KEY_POSITION_TRACK, second);
        cv2.put(Media.DATA,trackFirst);

        db.update (playlist, cv,Media._ID + " = ?",  new String[]{idFirst});

        db.update (playlist, cv2,Media._ID + " = ?" , new String[]{idSecond});

        db.close();

    }
    public void addPlaylist(String playlist)
    {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(KEY_NAME_PLAYLIST,playlist);
        db.insert(TABLE_LISTPLAYLIST,null,cv);
        String createTable = "CREATE TABLE IF NOT EXISTS " + playlist + "("
                + Media._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," + Media.DATA + " TEXT,"+
                Media.ARTIST + " TEXT,"+ Media.TITLE + " TEXT," + Media.ALBUM_ID +" LONG," + KEY_POSITION_TRACK + " INTEGER"+ ")";
        db.execSQL(createTable);
        db.close();
    }
}