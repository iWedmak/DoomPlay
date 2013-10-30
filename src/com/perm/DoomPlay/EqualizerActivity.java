package com.perm.DoomPlay;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.SeekBar;
import android.widget.TextView;

public class EqualizerActivity extends AbstractReceiver implements SeekBar.OnSeekBarChangeListener
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

    TextView text0;
    TextView text1;
    TextView text2;
    TextView text3;
    TextView text4;
    TextView text5;
    TextView text6;
    TextView text7;
    TextView text8;
    TextView text9;

    public static float convertProgressToGain(int progress)
    {
        float x = (( progress * 3f )/ 10f) - 15f;
        return x;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.equalizer);

        seek0 = (SeekBar)findViewById(R.id.seekEqual0);
        seek1 = (SeekBar)findViewById(R.id.seekEqual1);
        seek2 = (SeekBar)findViewById(R.id.seekEqual2);
        seek3 = (SeekBar)findViewById(R.id.seekEqual3);
        seek4 = (SeekBar)findViewById(R.id.seekEqual4);
        seek5 = (SeekBar)findViewById(R.id.seekEqual5);
        seek6 = (SeekBar)findViewById(R.id.seekEqual6);
        seek7 = (SeekBar)findViewById(R.id.seekEqual7);
        seek8 = (SeekBar)findViewById(R.id.seekEqual8);
        seek9 = (SeekBar)findViewById(R.id.seekEqual9);

        text0 = (TextView)findViewById(R.id.textDb0);
        text1 = (TextView)findViewById(R.id.textDb1);
        text2 = (TextView)findViewById(R.id.textDb2);
        text3 = (TextView)findViewById(R.id.textDb3);
        text4 = (TextView)findViewById(R.id.textDb4);
        text5 = (TextView)findViewById(R.id.textDb5);
        text6 = (TextView)findViewById(R.id.textDb6);
        text7 = (TextView)findViewById(R.id.textDb7);
        text8 = (TextView)findViewById(R.id.textDb8);
        text9 = (TextView)findViewById(R.id.textDb9);

        seek0.setOnSeekBarChangeListener(this);
        seek1.setOnSeekBarChangeListener(this);
        seek2.setOnSeekBarChangeListener(this);
        seek3.setOnSeekBarChangeListener(this);
        seek4.setOnSeekBarChangeListener(this);
        seek5.setOnSeekBarChangeListener(this);
        seek6.setOnSeekBarChangeListener(this);
        seek7.setOnSeekBarChangeListener(this);
        seek8.setOnSeekBarChangeListener(this);
        seek9.setOnSeekBarChangeListener(this);
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        int[] bounds = getSavedBounds();
        for(int i = 0;i < bounds.length; i++)
        {
            try
            {
                SeekBar seekBar =(SeekBar)this.getClass().getDeclaredField("seek" + String.valueOf(i)).get(this);
                seekBar.setProgress(bounds[i]);

                TextView textView = (TextView)this.getClass().getDeclaredField("text"+String.valueOf(i)).get(this);
                textView.setText(String.format("%.0f db", convertProgressToGain(bounds[i])));
                Log.i("TAG AUDIO", "seekbar" + seekBar.getId());

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
             bound[i] = preferences.getInt("equal"+String.valueOf(i),50);
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

            TextView textView ;
            try {
                textView = (TextView)this.getClass().getDeclaredField("text"+String.valueOf(n)).get(this);
                textView.setText(String.format("%.0f db", convertProgressToGain(progress)));
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (NoSuchFieldException e) {
                e.printStackTrace();
            }


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
