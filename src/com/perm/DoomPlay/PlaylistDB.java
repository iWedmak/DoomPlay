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

import java.util.ArrayList;

import static android.provider.MediaStore.Audio.Media;

public class PlaylistDB extends SQLiteOpenHelper
{

    private static final int DATABASE_VERSION = 10;
    private static final String DATABASE_NAME = "playlistsData";
    private static final String TABLE_LISTPLAYLIST = "listplaylist";
    private static final String KEY_POSITION_TRACK = "postiontak";
    private static final String TABLE_DEFAULT = "defaultTable";
    private static final String KEY_NAME_PLAYLIST = "playlistName";
    static final String TABLE_VK = "vk_table";
    private static final String KEY_OID = "owner_id";
    private static final String KEY_LID = "lyrics_id";
    static boolean isLoading  = false;

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

    private static void createTable(String table,SQLiteDatabase db)
    {
          db.execSQL("CREATE TABLE IF NOT EXISTS " + table + "("
                  + Media._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," + Media.DATA + " TEXT,"+
                  Media.ARTIST + " TEXT,"+ Media.TITLE + " TEXT," + Media.ALBUM_ID +" LONG," + KEY_POSITION_TRACK + " INTEGER"+ ")");
    }

    @Override
    public void onCreate(SQLiteDatabase db)
    {
        String createListPlaylistTable = "CREATE TABLE IF NOT EXISTS " + TABLE_LISTPLAYLIST + "(" + Media._ID +
                " INTEGER PRIMARY KEY AUTOINCREMENT," + KEY_NAME_PLAYLIST + " TEXT" +")";

        db.execSQL(createListPlaylistTable);
        createTable(TABLE_DEFAULT,db);

        db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_VK + "("
                + Media._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," + Media.DATA + " TEXT,"+
                Media.ARTIST + " TEXT,"+ Media.TITLE + " TEXT," + Media.ALBUM_ID +" LONG,"+
                KEY_OID + " LONG,"+ KEY_LID + " LONG,"+ KEY_POSITION_TRACK + " INTEGER"+ ")");



        ContentValues cv = new ContentValues();
        cv.put(KEY_NAME_PLAYLIST, TABLE_DEFAULT);
        db.insert(TABLE_LISTPLAYLIST,null,cv);

        String[] tables = getListPlaylistForDatabase(db);
        for(String table : tables )
        {
            if(!table.equals(TABLE_DEFAULT))
            {
                createTable(table,db);
            }
        }
    }
    private int getLastPosition(String playlist, SQLiteDatabase db)
    {
        Cursor c = db.query(playlist,new String[]{Media._ID},null, null, null, null, null);
        int count = c.getCount();
        c.close();
        return  count;
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
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_VK);
        onCreate(db);
    }

    ArrayList<Audio> getVkAudios()
    {
        SQLiteDatabase db = getWritableDatabase();

        Cursor c = db.query(TABLE_VK,new String[]{Media.ARTIST,Media.TITLE,
                Media.ALBUM_ID,Media.DATA,KEY_OID,KEY_LID} ,null, null, null, null, null);

        ArrayList<Audio> result  = new ArrayList<Audio>();

        if(c.moveToFirst())
        {
            do
            {
                result.add(Audio.createAudioCursorExtend(c));

            }while (c.moveToNext());
        }

        c.close();
        db.close();
        return result;
    }

    void addVkTracks(ArrayList<Audio> audios)
    {

        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv;
        int position = getLastPosition(TABLE_VK,db);

        for(Audio audio : audios)
        {
            cv = new ContentValues();
            cv.put(Media.DATA, audio.getUrl());
            cv.put(Media.ARTIST, audio.getArtist());
            cv.put(Media.ALBUM_ID, audio.getAid());
            cv.put(Media.TITLE, audio.getTitle());
            cv.put(KEY_OID, audio.getOwner_id());
            cv.put(KEY_LID, audio.getLyrics_id());
            cv.put(KEY_POSITION_TRACK,position);
            position++;
            db.insert(TABLE_VK, null, cv);
        }
        db.close();
    }
    void addVkTrack(Audio audio)
    {

        SQLiteDatabase db = getWritableDatabase();
        int position = getLastPosition(TABLE_VK,db);

        ContentValues cv = new ContentValues();
        cv.put(Media.DATA, audio.getUrl());
        cv.put(Media.ARTIST, audio.getArtist());
        cv.put(Media.ALBUM_ID, audio.getAid());
        cv.put(Media.TITLE, audio.getTitle());
        cv.put(KEY_OID, audio.getOwner_id());
        cv.put(KEY_LID, audio.getLyrics_id());
        cv.put(KEY_POSITION_TRACK,position);
        db.insert(TABLE_VK, null, cv);

        db.close();
    }

    void addTracks(ArrayList<Audio> audios, String playlist)
    {
        isLoading = true;
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv;
        int position = getLastPosition(playlist,db);

        for(Audio audio : audios)
        {
            cv = new ContentValues();
            cv.put(Media.DATA, audio.getUrl());
            cv.put(Media.ARTIST, audio.getArtist());
            cv.put(Media.ALBUM_ID, audio.getAid());
            cv.put(Media.TITLE, audio.getTitle());
            cv.put(KEY_POSITION_TRACK,position);
            position++;
            db.insert(playlist, null, cv);
        }
        db.close();
        isLoading = false;
    }
    void addTrack(Audio audio,String playlist)
    {
        SQLiteDatabase db = getWritableDatabase();
        int position = getLastPosition(playlist,db);

        ContentValues cv = new ContentValues();
        cv.put(Media.DATA, audio.getUrl());
        cv.put(Media.ARTIST, audio.getArtist());
        cv.put(Media.ALBUM_ID, audio.getAid());
        cv.put(Media.TITLE, audio.getTitle());
        cv.put(KEY_POSITION_TRACK,position);
        db.insert(playlist, null, cv);
        db.close();
    }

    void deleteTrack(int positionTrack,String playlist)
    {
        SQLiteDatabase db = getWritableDatabase();
        db.delete(playlist, KEY_POSITION_TRACK + " = ?", new String[]{String.valueOf(positionTrack)});

        db.close();
    }
    void setAcordingPositions(int positionTrack,String playlist)
    {
        isLoading = true;
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
        isLoading = false;
    }

    ArrayList<Audio> getTracks(String playlist)
    {
        isLoading = true;
        SQLiteDatabase db = getWritableDatabase();

        Cursor c = db.query(playlist,TracksHolder.projection,null, null, null, null, null);

        ArrayList<Audio> result ;

        if(c.moveToFirst())
            result = Audio.parseAudio(c);
        else
            result = new ArrayList<Audio>();

        c.close();
        db.close();
        isLoading = false;
        return result;


    }
    private String[] getListPlaylistForDatabase(SQLiteDatabase db)
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
    String[] getListPlaylist()
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
        db.execSQL("DROP TABLE IF EXISTS " + playlist);
        db.close();
    }
    void changeColumns(String playlist , int first,int second)
    {

        SQLiteDatabase db = getWritableDatabase();

        Cursor cFirst = db.query(playlist,TracksHolder.projectionPlusId,KEY_POSITION_TRACK + " = ?",new String[]{String.valueOf(first)},null,null,null);
        cFirst.moveToFirst();
        String idFirst = cFirst.getString(cFirst.getColumnIndex(Media._ID));
        String trackFirst = cFirst.getString(cFirst.getColumnIndex(Media.DATA));
        String artistFirst  = cFirst.getString(cFirst.getColumnIndex(Media.ARTIST));
        String titleFirst  = cFirst.getString(cFirst.getColumnIndex(Media.TITLE));
        long albumIdFirst  = cFirst.getLong(cFirst.getColumnIndex(Media.ALBUM_ID));
        cFirst.close();

        Cursor cSecond = db.query(playlist,TracksHolder.projectionPlusId,KEY_POSITION_TRACK + " = ?",new String[]{String.valueOf(second)},null,null,null);
        cSecond.moveToFirst();
        String idSecond = cSecond.getString(cSecond.getColumnIndex(Media._ID));
        String trackSecond = cSecond.getString(cSecond.getColumnIndex(Media.DATA));
        String artistSecond  = cSecond.getString(cSecond.getColumnIndex(Media.ARTIST));
        String titleSecond  = cSecond.getString(cSecond.getColumnIndex(Media.TITLE));
        long albumIdSecond  = cSecond.getLong(cSecond.getColumnIndex(Media.ALBUM_ID));
        cSecond.close();



        ContentValues cv = new ContentValues();
        cv.put (KEY_POSITION_TRACK, first);
        cv.put(Media.DATA,trackSecond);
        cv.put(Media.TITLE,titleSecond);
        cv.put(Media.ARTIST,artistSecond);
        cv.put(Media.ALBUM_ID,albumIdSecond);

        ContentValues cv2 = new ContentValues();
        cv2.put (KEY_POSITION_TRACK, second);
        cv2.put(Media.DATA,trackFirst);
        cv2.put(Media.TITLE,titleFirst);
        cv2.put(Media.ARTIST,artistFirst);
        cv2.put(Media.ALBUM_ID,albumIdFirst);

        db.update (playlist, cv,Media._ID + " = ?",  new String[]{idFirst});

        db.update (playlist, cv2,Media._ID + " = ?" , new String[]{idSecond});

        db.close();

    }
    void addPlaylist(String playlist)
    {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(KEY_NAME_PLAYLIST,playlist);
        db.insert(TABLE_LISTPLAYLIST,null,cv);
        createTable(playlist,db);
        db.close();
    }
}