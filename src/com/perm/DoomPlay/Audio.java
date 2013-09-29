package com.perm.DoomPlay;

import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;
import com.perm.vkontakte.api.Api;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;

public class Audio implements Serializable ,Parcelable
{
    private static final long serialVersionUID = 1L;
    private long aid;
    private String artist;
    private String title;
    private String url;
    private long lyrics_id;
    private long owner_id;

    public static Audio parseAudio(JSONObject o) throws NumberFormatException, JSONException
    {
        Audio audio = new Audio();
        audio.aid = Long.parseLong(o.getString("aid"));
        if(o.has("performer"))
            audio.artist = Api.unescape(o.optString("performer"));
        else if(o.has("artist"))
            audio.artist = Api.unescape(o.optString("artist"));


        audio.title = Api.unescape(o.optString("title"));
        audio.url = o.optString("url", null);
        audio.owner_id = Long.parseLong(o.getString("owner_id"));

        String tmp=o.optString("lyrics_id");
        if(tmp!=null && !tmp.equals(""))
            audio.lyrics_id = Long.parseLong(tmp);
        else
            audio.lyrics_id = 0;
        return audio;
    }
    private Audio(){}

    public Audio(String artist,String title,String url,long aid)
    {
        this.artist = artist;
        this.title = title;
        this.aid = aid;
        this.url = url;
    }
    public static Audio createAudioCursor(Cursor cursor)
    {
        Audio audio = new Audio();
        audio.url = cursor.getString(3);
        audio.title = cursor.getString(1);
        audio.artist = cursor.getString(0);
        audio.aid = cursor.getLong(2);

        return audio;
    }
    public static Audio createAudioCursorExtend(Cursor cursor)
    {
        Audio audio = new Audio();
        audio.url = cursor.getString(3);
        audio.title = cursor.getString(1);
        audio.artist = cursor.getString(0);
        audio.aid = cursor.getLong(2);
        audio.owner_id = cursor.getLong(4);
        audio.lyrics_id = cursor.getLong(5);

        return audio;
    }


    public static ArrayList<Audio> parseAudio(Cursor cursor)
    {

        ArrayList<Audio> audios = new ArrayList<Audio>();
        do
        {
            audios.add(createAudioCursor(cursor));

        }while (cursor.moveToNext());

        return audios;
    }

    @Override
    public int describeContents()
    {
        return 0;
    }
    public Audio(Parcel source)
    {
        artist = source.readString();
        title = source.readString();
        url = source.readString();
        aid = source.readLong();
        lyrics_id = source.readLong();
        owner_id = source.readLong();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags)
    {
        dest.writeString(getArtist());
        dest.writeString(getTitle());
        dest.writeString(getUrl());
        dest.writeLong(getAid());
        dest.writeLong(getLyrics_id());
        dest.writeLong(getOwner_id());
    }

    @Override
    public boolean equals(Object o)
    {
        return o.getClass() == Audio.class && getUrl().equals(((Audio)o).getUrl());
    }

    @Override
    public int hashCode()
    {
        return getUrl().hashCode();
    }

    public static final Parcelable.Creator<Audio> CREATOR = new Parcelable.Creator<Audio>()
    {
        @Override
        public Audio createFromParcel(Parcel source)
        {
            return new Audio(source);
        }

        @Override
        public Audio[] newArray(int size)
        {
            return new Audio[size];
        }
    };

    public long getAid()
    {
        return aid;
    }

    public String getArtist()
    {
        return artist;
    }

    public String getTitle()
    {
        return title;
    }

    public String getUrl()
    {
        return url;
    }

    public long getLyrics_id()
    {
        return lyrics_id;
    }

    public long getOwner_id()
    {
        return owner_id;
    }
}