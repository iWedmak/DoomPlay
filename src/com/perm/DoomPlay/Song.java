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
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.net.Uri;

public class Song
{
    private String currentTrack;
    private int currentPosition;
    private boolean isFound = false;
    MediaMetadataRetriever metadata = null;

    public Song(String currentTrack)
    {
        this.currentTrack = currentTrack;
        if(!TracksHolder.isScanned)
        {
            initializeMetadata();
            return;
        }
        int count = 0;
        for(String track : TracksHolder.songAllPath)
        {

            if(track.equals(currentTrack))
            {
                currentPosition = count;
                isFound = true;
                break;
            }
            count++;
        }
        if(!isFound)
        {
            initializeMetadata();
        }

    }
    void initializeMetadata()
    {
        metadata = new MediaMetadataRetriever();
        metadata.setDataSource(currentTrack);
    }
    public static String getArtistFromAlbum(int positionAlbum)
    {
        return TracksHolder.allAcordingArtists[positionAlbum];
    }
    public String getArtist()
    {
        if(isFound)
            return TracksHolder.songArtist[currentPosition];
        else
            return metadata.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);
    }
    public String getTitle()
    {
        if(isFound)
            return TracksHolder.songTitle[currentPosition];
        else
            return metadata.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);
    }
    public int getAlbumId()
    {
        if(isFound)
            return TracksHolder.songAlbumId[currentPosition];
        else
            return 0;
    }

    public static final Uri artworkUri = Uri.parse("content://media/external/audio/albumart");
    public Bitmap getAlbumArt(Context context)
    {
        int id = getAlbumId();
        if(id != 0)
        {
            return AlbumArtGetter.getBitmapById(id, context);
        }
        else
        {
            byte[] bitmap = metadata.getEmbeddedPicture();

            BitmapFactory.Options options = new BitmapFactory.Options();
            options.outWidth = 400;
            options.outHeight = 400;

            if(bitmap != null)
                return BitmapFactory.decodeByteArray(bitmap,0,bitmap.length,options);
            else
                return null;
        }
    }
}
