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

import android.content.ContentUris;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.provider.MediaStore;

import java.io.FileNotFoundException;
import java.io.IOException;

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

    public static final Uri artworkUri = Uri.parse("content://media/external/audio/albumart");
    public Bitmap getBitmap(Context context)
    {
        if(isFound)
        {

            Uri uri = ContentUris.withAppendedId(artworkUri, TracksHolder.songAlbumId[currentPosition]);
            try
            {
                return MediaStore.Images.Media.getBitmap(context.getContentResolver(),uri);
            }
            catch (FileNotFoundException e)
            {
                return null;
            }
            catch (IOException e)
            {
                return null;
            }
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
    public static void insertBitmapInMediaStore(Context context,Bitmap bitmap,long id)
    {
        MediaStore.Images.Media.insertImage(context.getContentResolver(),bitmap,String.valueOf(id),null);
    }
}
