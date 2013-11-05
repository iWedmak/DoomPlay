package com.perm.DoomPlay;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;
import android.widget.TextView;
import com.un4seen.bass.BASS;

public class EqualizerEffectsFragment extends Fragment implements SeekBar.OnSeekBarChangeListener
{

    SeekBar seek10;
    SeekBar seek11;
    SeekBar seek12;
    SeekBar seek13;

    TextView text10;
    TextView text11;
    TextView text12;
    TextView text13;

    private static final int LOW_FREQ = 10;
    private static final int HIGHT_FREQ = 11;
    private static final int REVERB = 12;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.equalizer_effects,container,false);

        seek10 = (SeekBar)view.findViewById(R.id.seekEqual10);
        seek11 = (SeekBar)view.findViewById(R.id.seekEqual11);
        seek12 = (SeekBar)view.findViewById(R.id.seekEqual12);
        seek13 = (SeekBar)view.findViewById(R.id.seekEqual13);


        text10 = (TextView)view.findViewById(R.id.textDb10);
        text11 = (TextView)view.findViewById(R.id.textDb11);
        text12 = (TextView)view.findViewById(R.id.textDb12);
        text13 = (TextView)view.findViewById(R.id.textDb13);


        seek10.setOnSeekBarChangeListener(this);
        seek11.setOnSeekBarChangeListener(this);
        seek12.setOnSeekBarChangeListener(this);
        seek13.setOnSeekBarChangeListener(this);


        int[] progresses = getSavedEffects();
        seek10.setProgress(progresses[0]);
        seek11.setProgress(progresses[1]);
        seek12.setProgress(progresses[2]);

        text10.setText(String.format("x%.1f", BassPlayer.convertProgressToFreq(progresses[0])));
        text11.setText(String.format("x%.1f", BassPlayer.convertProgressToFreq(progresses[1])));
        text12.setText(String.valueOf(progresses[2]));
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        seek13.setProgress((int)(BASS.BASS_GetVolume()*100));
        text13.setText(String.valueOf((int)(BASS.BASS_GetVolume()*100)));
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser)
    {
        if(fromUser)
        {
            switch (seekBar.getId())
            {
                case R.id.seekEqual10:
                    saveEffect(progress, LOW_FREQ);
                    text10.setText(String.format("x%.1f", BassPlayer.convertProgressToFreq(progress)));
                    BassPlayer.setLowFreq(progress);
                    break;
                case R.id.seekEqual11:
                    BassPlayer.setHightFreq(progress);
                    text11.setText(String.format("x%.1f", BassPlayer.convertProgressToFreq(progress)));
                    saveEffect(progress, HIGHT_FREQ);
                    break;
                case R.id.seekEqual12:
                    BassPlayer.setReverb(progress);
                    text12.setText(String.valueOf(progress));
                    saveEffect(progress, REVERB);
                    break;
                case R.id.seekEqual13:
                    BASS.BASS_SetVolume((float)progress/100f);
                    text13.setText(String.valueOf((int)(BASS.BASS_GetVolume()*100)));
                    break;
            }
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {}
    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {}

    private void saveEffect(int progress, int n)
    {
        SharedPreferences preferences =getActivity().getSharedPreferences("effects", Activity.MODE_PRIVATE);

        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt("equal" + String.valueOf(n), progress);
        editor.commit();
        editor.apply();
    }

    public static int[] getSavedEffects()
    {
        int[] bound = new int[3];
        SharedPreferences preferences = MyApplication.getInstance().getSharedPreferences("effects", Activity.MODE_PRIVATE);
        for(int i = 10 ; i < 13; i++)
        {
            bound[i-10] = preferences.getInt("equal"+String.valueOf(i),0);
        }
        return bound;
    }
}

