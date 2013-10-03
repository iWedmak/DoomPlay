package com.perm.vkontakte.api;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class AudioAlbum
{
    public final long album_id;
    public final String title;

    public static AudioAlbum parse(JSONObject o) throws JSONException
    {
        return new AudioAlbum(o.getLong("album_id"),Api.unescape(o.optString("title")));
    }


    public AudioAlbum(long album_id,String title)
    {
        this.title = title;
        this.album_id = album_id;
    }

    
    public static ArrayList<AudioAlbum> parseAlbums(JSONArray array) throws JSONException
    {
        ArrayList<AudioAlbum> albums = new ArrayList<AudioAlbum>();
        if (array == null) 
            return albums;
        int category_count = array.length();
        for(int i = 1; i < category_count; ++i) {
            JSONObject o = (JSONObject)array.get(i);
            AudioAlbum m = AudioAlbum.parse(o);
            albums.add(m);
        }
        return albums;
    }
}