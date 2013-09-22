package com.perm.vkontakte.api;

import android.os.Parcel;
import android.os.Parcelable;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;

public class Audio implements Serializable ,Parcelable
{
    private static final long serialVersionUID = 1L;
    public long aid;
    public String artist;
    public String title;
    public String url;
    public long lyrics_id;
    public long owner_id;

    public static Audio parse(JSONObject o) throws NumberFormatException, JSONException{
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

    public Audio(String artist,String title,long aid)
    {
        this.artist = artist;
        this.title = title;
        this.aid = aid;
        url = "";
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
        dest.writeString(artist);
        dest.writeString(title);
        dest.writeString(url);
        dest.writeLong(aid);
        dest.writeLong(lyrics_id);
        dest.writeLong(owner_id);
    }


    public boolean equal(Audio o)
    {
        return o.title.equals(title) && o.artist.equals(artist) && o.url.equals(url) && o.aid == aid && o.lyrics_id == lyrics_id;
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
}