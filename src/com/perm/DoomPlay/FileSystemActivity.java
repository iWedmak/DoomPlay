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
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.view.ActionMode;
import android.view.*;
import android.widget.*;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;

public class FileSystemActivity extends AbstractReceiver
{
    private FileSystemAdapter adapter;
    private ListView listView;
    private File currentDirectory ;
    private TextView textCurrentDir;
    private File[] entriesFiles;
    public final static String keyMusic = "3kpoid";
    private final static String keyCurrentDir = "currentDij";

    private final static File rootFile = new File("/storage");

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.file_system);

        entriesFiles = new File[0];
        textCurrentDir = (TextView)findViewById(R.id.textTitle);
        listView = (ListView)findViewById(R.id.listDirectories);
        adapter = new FileSystemAdapter();
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(onItemClickListener);
        listView.setOnItemLongClickListener(onItemLongClickListener);

        if(savedInstanceState != null)
        {
            String savedPath = savedInstanceState.getString(keyCurrentDir);
            if(savedPath != null)
                fill(new File(savedInstanceState.getString(keyCurrentDir)));
            else
                fill(rootFile);
        }
        else
            fill(rootFile);
    }


    public static String getRealPath(File file)
    {
        try
        {
            return file.getCanonicalPath();
        }
        catch (IOException e)
        {
            return file.getAbsolutePath();
        }
    }


    @Override
    protected void onSaveInstanceState(Bundle outState)
    {
        super.onSaveInstanceState(outState);
        outState.putString(keyCurrentDir, currentDirectory.getAbsolutePath());
    }

    public static Intent getToFullIntent(Context context,ArrayList<Audio> audios)
    {
        Intent intent = new Intent(context,FullPlaybackActivity.class);
        intent.setAction(FullPlaybackActivity.actionPlayFull);
        intent.putExtra(FileSystemActivity.keyMusic,audios);
        return intent;
    }

    private static final FileFilter fileFilter = new FileFilter()
    {
        @Override
        public boolean accept(File file)
        {
            if ((!Utils.trackChecker(file.getName()) && !file.isDirectory())
                    || (file.isDirectory() && file.list() == null) || (file.isDirectory()) && (file.list().length == 0 ))
                return false;
            else
                return true;
        }
    };
    private static ArrayList<Audio> getAudiosFromFolder(File file, Context context)
    {
        String selectionArgs = getRealPath(file);

        Cursor cursor = context.getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                TracksHolder.projection,MediaStore.Audio.Media.DATA + " LIKE ? ",new String[]{"%"+selectionArgs +"%"},null);

        ArrayList<Audio> audios = Audio.parseAudiosCursor(cursor);

        cursor.close();
        return audios;
    }

    private static Audio getAudioFromFile(File file, Context context)
    {
        String selectionArgs = getRealPath(file);

        Cursor cursor = context.getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                TracksHolder.projection,MediaStore.Audio.Media.DATA + " = ? ",new String[]{selectionArgs}, null);

        Audio audio;

        if(cursor.moveToFirst())
            audio = Audio.parseAudioCursor(cursor);
        else
        {
            audio = new Audio("unknown",file.getName(),selectionArgs,0);
        }
        cursor.close();
        return audio;
    }
    private final android.support.v7.view.ActionMode.Callback callback = new android.support.v7.view.ActionMode.Callback()
    {

        @Override
        public boolean onCreateActionMode(ActionMode actionMode, Menu menu)
        {
            getMenuInflater().inflate(R.menu.action_filesystem,menu);
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode actionMode, Menu menu) {return false;  }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item)
        {
            int position =(Integer) mode.getTag();


            switch(item.getItemId())
            {
                case R.id.itemPlayAll:
                {
                    ArrayList<Audio> audios = getAudiosFromFolder(entriesFiles[position],getBaseContext());

                    if(audios.size() == 0 )
                    {
                        Toast.makeText(getBaseContext(),getResources().getString(R.string.no_valid_files),Toast.LENGTH_SHORT).show();
                        mode.finish();
                        return false ;
                    }

                    startActivity(getToFullIntent(getBaseContext(),audios));
                    break;
                }
                case R.id.itemToPlaylist:
                {
                    ArrayList<Audio> audios = getAudiosFromFolder(entriesFiles[position],getBaseContext());

                    if(audios.size() == 0 )
                    {
                        Toast.makeText(getBaseContext(),getResources().getString(R.string.no_valid_files),Toast.LENGTH_SHORT).show();
                        mode.finish();
                        return false ;
                    }
                    showPlaybackDialog(audios);
                    break;
                }
                case R.id.itemDeleteFile:
                {
                    if(entriesFiles[position].delete())
                        fill(currentDirectory);
                    else
                        Toast.makeText(getBaseContext(),getString(R.string.cant_delete_file),Toast.LENGTH_SHORT).show();
                    break;
                }
            }
            mode.finish();
            return true;
        }

        @Override
        public void onDestroyActionMode(ActionMode actionMode) {
            mActionMode = null;
        }
    };


    private void fill(File file)
    {
        entriesFiles = file.listFiles(fileFilter);
        if(entriesFiles == null )
        {
            Toast.makeText(this, getResources().getString(R.string.no_valid_files), Toast.LENGTH_SHORT).show();
            return ;
        }
        currentDirectory = file;

        textCurrentDir.setText(getRealPath(file));

        Arrays.sort(entriesFiles, fileComparator);

        if(adapter == null)
            adapter = new FileSystemAdapter();
        else
            adapter.notifyDataSetChanged();

        listView.setAdapter(adapter);


    }

    private android.support.v7.view.ActionMode mActionMode;

    private final AdapterView.OnItemClickListener onItemClickListener = new AdapterView.OnItemClickListener()
    {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id)
        {


            if(entriesFiles[position].isDirectory())
            {
                fill(entriesFiles[position]);

                if(mActionMode != null)
                    mActionMode.finish();
            }
            else
            {
                ArrayList <Audio> audios = new ArrayList<Audio>();
                audios.add(getAudioFromFile(entriesFiles[position],getBaseContext()));
                startActivity(getToFullIntent(getBaseContext(),audios));
            }

        }
    };
    private final AdapterView.OnItemLongClickListener onItemLongClickListener = new AdapterView.OnItemLongClickListener()
    {
        @Override
        public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id)
        {
            mActionMode = startSupportActionMode(callback);
            mActionMode.setTag(position);
            return true;
        }
    };


    @Override
    public void onBackPressed()
    {
        if(currentDirectory.equals(rootFile))
        {
            super.onBackPressed();
        }
        else
        {
            fill(currentDirectory.getParentFile());
        }
    }
    private final Comparator<File> fileComparator = new Comparator<File>()
    {
        @Override
        public int compare(File fileFirst, File fileSecond)
        {

            boolean isDirFirst = fileFirst.isDirectory();
            boolean isDirSecond = fileSecond.isDirectory();

            if(isDirFirst && !isDirSecond)
                return -1;
            else if(!isDirFirst && isDirSecond)
                return 1;
            else
                return fileFirst.getName().compareToIgnoreCase(fileSecond.getName());
        }
    };
    class FileSystemAdapter extends BaseAdapter
    {
        final LayoutInflater inflater;

        public FileSystemAdapter()
        {
            inflater = (LayoutInflater)getSystemService(LAYOUT_INFLATER_SERVICE);
        }
        @Override
        public int getCount()
        {
            return entriesFiles.length;
        }
        @Override
        public Object getItem(int position)
        {
            return entriesFiles[position];
        }
        @Override
        public long getItemId(int position)
        {
            return position;
        }
        @Override
        public View getView(int position, View convertView, ViewGroup parent)
        {
            View v ;

            if(entriesFiles[position].isDirectory())
            {
                v = inflater.inflate(R.layout.item_file_system ,parent,false );
            }
            else
            {
                v = inflater.inflate(R.layout.item_file ,parent,false );
            }

            TextView textItem = (TextView)v.findViewById(R.id.textItem);
            textItem.setText(entriesFiles[position].getName());

            return v;
        }
    }
}
