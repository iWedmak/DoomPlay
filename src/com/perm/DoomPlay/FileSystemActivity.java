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
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.actionbarsherlock.view.ActionMode;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;

public class FileSystemActivity extends AbstractReceiver
{
    FileSystemAdapter adapter;
    ListView listView;
    File currentDirectory ;
    TextView textCurrentDir;
    File[] entriesFiles;
    public final static String keyMusic = "3kpoid";
    final static String keyCurrentDir = "wajia";


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
            fill(new File(savedInstanceState.getString(keyCurrentDir, "/mnt")));
        else
            fill(new File("/mnt"));
    }





    @Override
    protected void onSaveInstanceState(Bundle outState)
    {
        super.onSaveInstanceState(outState);
        outState.putString(keyCurrentDir, currentDirectory.getAbsolutePath());
    }

    private void toFullPlayback(int postion)
    {
        String[] filesToPlay = getRealPathes(currentDirectory);
        int realPosition = postion - entriesFiles.length + filesToPlay.length;
        startActivity(getToFullIntent(this,filesToPlay,realPosition));
    }
    public static Intent getToFullIntent(Context context,String[] toPlay,int position)
    {
        Intent intent = new Intent(context,FullPlaybackActivity.class);
        intent.setAction(FullPlaybackActivity.actionPlayFull);
        intent.putExtra(FileSystemActivity.keyMusic,toPlay);
        intent.putExtra(FullPlaybackActivity.keyIndex,position);
        return intent;
    }

    FileFilter fileFilter = new FileFilter()
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

    public static String[] getRealPathes(File rootFile)
    {
        ArrayList<String> temp = new ArrayList<String>();
            for(File file : rootFile.listFiles())
            {
                if(Utils.trackChecker(file.getName()))
                    temp.add(file.getAbsolutePath());
            }
        String[] result = new String[temp.size()];
        result = temp.toArray(result);
        return result;
    }

    ActionMode.Callback callback = new ActionMode.Callback()
    {
        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu)
        {
            getSupportMenuInflater().inflate(R.menu.action_filesystem,menu);
            return true;
        }
        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu){return false;}
        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item)
        {
            int position =(Integer) mode.getTag();

            String[] filesToPlay;


            if(entriesFiles[position].isDirectory())
            {
                filesToPlay = getRealPathes(entriesFiles[position]);
            }
            else
                filesToPlay = new String[]{entriesFiles[position].getAbsolutePath()};

            if(filesToPlay.length == 0 )
            {
                Toast.makeText(getBaseContext(),"There's no valid files in this directory",Toast.LENGTH_SHORT).show();
                return false ;
            }
            switch(item.getItemId())
            {
                case R.id.itemPlayAll:
                {
                    startActivity(getToFullIntent(getBaseContext(),filesToPlay,0));
                    break;
                }
                case R.id.itemToPlaylist:
                {
                    showPlaybackDialog(filesToPlay,getSupportFragmentManager());
                    break;
                }
                case R.id.itemDeleteFile:
                {
                    entriesFiles[position].delete();
                    fill(currentDirectory);
                    break;
                }
            }
            mode.finish();
            return true;
        }

        @Override
        public void onDestroyActionMode(ActionMode mode)
        {

        }
    };
    public static void showPlaybackDialog(String[] tracks, android.support.v4.app.FragmentManager fragmentManager)
    {
        AddTrackFromPlaybackDialog dialog = new AddTrackFromPlaybackDialog();
        Bundle bundle = new Bundle();
        bundle.putStringArray(AddTrackFromPlaybackDialog.keyBundleDialog,tracks);
        dialog.setArguments(bundle);
        dialog.show(fragmentManager,"tag");
    }


    private void fill(File file)
    {
        entriesFiles = file.listFiles(fileFilter);
        if(entriesFiles == null )
        {
            Toast.makeText(this, "There's no files in this directory", Toast.LENGTH_SHORT).show();
            return ;
        }
        currentDirectory = file;
        textCurrentDir.setText(file.getAbsolutePath());

        Arrays.sort(entriesFiles, fileComparator);
        adapter = new FileSystemAdapter();
        listView.setAdapter(adapter);


    }
    AdapterView.OnItemClickListener onItemClickListener = new AdapterView.OnItemClickListener()
    {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id)
        {
            if(entriesFiles[position].isDirectory())
                fill(entriesFiles[position]);
            else
                toFullPlayback(position);
        }
    };
    AdapterView.OnItemLongClickListener onItemLongClickListener = new AdapterView.OnItemLongClickListener()
    {
        @Override
        public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id)
        {
            startActionMode(callback).setTag(position);
            return true;
        }
    };


    @Override
    public void onBackPressed()
    {
        if(currentDirectory.equals(new File("/mnt")))
        {
            super.onBackPressed();
        }
        else
        {
            fill(currentDirectory.getParentFile());
        }
    }
    Comparator<File> fileComparator = new Comparator<File>()
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
        LayoutInflater inflater;

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
