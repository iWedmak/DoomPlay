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
import android.graphics.Bitmap;
import android.util.LruCache;

public class ArtCacheUtils
{
    private static LruCache<Long,Bitmap> cache = new LruCache<Long, Bitmap>(2*1024*1024);

    public static void add(long id)
    {
        Bitmap bitmap = AlbumArtGetter.getBitmapById(id,MyApplication.getInstance());

        if(bitmap != null)
            cache.put(id,bitmap);
    }
    public static Bitmap get(long id)
    {
         return cache.get(id);
    }
}

