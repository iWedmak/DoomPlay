package com.perm.DoomPlay;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.widget.SeekBar;
import com.un4seen.bass.BassPlayer;

public class EqualizerActivity extends Activity implements SeekBar.OnSeekBarChangeListener
{
    SeekBar seek0;
    SeekBar seek1;
    SeekBar seek2;
    SeekBar seek3;
    SeekBar seek4;
    SeekBar seek5;
    SeekBar seek6;
    SeekBar seek7;
    SeekBar seek8;
    SeekBar seek9;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.equalizer);

        seek0 = (SeekBar)findViewById(R.id.seekEqual1);
        seek1 = (SeekBar)findViewById(R.id.seekEqual2);
        seek2 = (SeekBar)findViewById(R.id.seekEqual3);
        seek3 = (SeekBar)findViewById(R.id.seekEqual4);
        seek4 = (SeekBar)findViewById(R.id.seekEqual5);
        seek5 = (SeekBar)findViewById(R.id.seekEqual6);
        seek6 = (SeekBar)findViewById(R.id.seekEqual7);
        seek7 = (SeekBar)findViewById(R.id.seekEqual8);
        seek8 = (SeekBar)findViewById(R.id.seekEqual9);
        seek9 = (SeekBar)findViewById(R.id.seekEqual10);

        int[] bounds = getSavedBounds();
        for(int i = 0;i < 10; i++)
        {
            try
            {
               SeekBar seekBar =(SeekBar)this.getClass().getDeclaredField("seek" + String.valueOf(i)).get(this);
               seekBar.setOnSeekBarChangeListener(this);
               seekBar.setProgress(bounds[i]);
               Log.i("TAG AUDIO","seekbar"+ seekBar.getId());

            } catch (NoSuchFieldException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }


    }

    public static int[] getSavedBounds()
    {
        int[] bound = new int[10];
        SharedPreferences preferences = MyApplication.getInstance().getSharedPreferences("bounds",MODE_PRIVATE);
        for(int i = 0 ; i < 10; i++)
        {
            if(i < 8)
                bound[i] = preferences.getInt("equal"+String.valueOf(i),50);
            else
                bound[i] = preferences.getInt("equal"+String.valueOf(i),0);
        }
        return bound;
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser)
    {
        if(fromUser)
        {
            //for  java.lang.ClassCastException
            Object tag = seekBar.getTag();
            int n;
            if(tag instanceof Integer)
                n = (Integer)tag;
            else if(tag instanceof String)
                n = Integer.parseInt((String)tag);
            else
                throw new IllegalArgumentException("wtf exception");

            Log.i("TAG AUDIO","tag "+ n);

            saveBound(progress,n);
            BassPlayer.updateFX(progress, n);
        }
    }

    private void saveBound(int progress, int n)
    {
        SharedPreferences preferences = getSharedPreferences("bounds",MODE_PRIVATE);

        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt("equal" + String.valueOf(n), progress);
        editor.commit();
        editor.apply();
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {}
    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {}


}
