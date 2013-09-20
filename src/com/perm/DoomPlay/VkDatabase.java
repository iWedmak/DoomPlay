package com.perm.DoomPlay;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class VkDatabase extends SQLiteOpenHelper
{
    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "VkDatabase";


    private static final String TABLE_AUDIOS = "tableAudios";
    private static final String TABLE_ALBUMS = "tableAlbums";
    private static final String TABLE_FRIENDS = "tableAlbums";
    private static final String TABLE_GROUPS = "tableAlbums";

    private static final String KEY_ID = "id";


    private VkDatabase(Context context)
    {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);

    }
    @Override
    public void onCreate(SQLiteDatabase db)
    {


    }
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
    {


    }



}
