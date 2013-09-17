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

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import com.actionbarsherlock.app.SherlockDialogFragment;

public class SleepDialog extends SherlockDialogFragment
{
    TextView textSeekTrack;
    TextView textSeekMin;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.dialog_sleep,container,false);
        Button btnEnable = (Button)view.findViewById(R.id.buttonDEnable);
        btnEnable .setOnClickListener(onClickEnableHandler);
        Button btnDisable = (Button)view.findViewById(R.id.buttonDDisable);
        btnDisable.setOnClickListener(onClickDisableHandler);
        Button btnCancel = (Button)view.findViewById(R.id.buttonDCancel) ;
        btnCancel.setOnClickListener(onClickCancelHandler);
        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);

        textSeekTrack = (TextView)view.findViewById(R.id.textSeekTrack);
        textSeekMin = (TextView)view.findViewById(R.id.textSeekMin);

        ((SeekBar)view.findViewById(R.id.seekTrack)).setOnSeekBarChangeListener(onSeekBarTrack);
        ((SeekBar)view.findViewById(R.id.seekMin)).setOnSeekBarChangeListener(onSeekBarMin);


        return view;
    }
    View.OnClickListener onClickEnableHandler = new View.OnClickListener()
    {
        @Override
        public void onClick(View v)
        {
            enableSpleep(Integer.parseInt(textSeekMin.getText().toString()), Integer.parseInt(textSeekTrack.getText().toString()));
            dismiss();
        }


    };
    View.OnClickListener onClickDisableHandler = new View.OnClickListener()
    {
        @Override
        public void onClick(View v)
        {
            disableTime();
            disableTracks();
            dismiss();
        }
    };

    private void disableTime()
    {
         if(timer != null)
         {
             timer.cancel();
             timer = null;
         }
    }
    private void disableTracks()
    {
        PlayingService.setSleepTrack(PlayingService.valueIncredible);
    }

    private static CountDownTimer timer;
    private void enableSpleep(int time, int tracks)
    {
        disableTime();
        if(time != 0)
        {
            timer = new CountDownTimer((long)time * 600,1000000)
            {
                @Override
                public void onTick(long millisUntilFinished){}

                @Override
                public void onFinish()
                {
                    getActivity().sendBroadcast(new Intent(AbstractReceiver.actionKill));
                }
            };
            timer.start();
        }

        if(tracks != 0)
            PlayingService.setSleepTrack(tracks);
        else
            disableTracks();
    }






    View.OnClickListener onClickCancelHandler = new View.OnClickListener()
    {
        @Override
        public void onClick(View v)
        {
             dismiss();
        }
    };


    SeekBar.OnSeekBarChangeListener onSeekBarTrack = new SeekBar.OnSeekBarChangeListener()
    {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser)
        {
            if(progress < 10)
                 textSeekTrack.setText("0");
            else if(progress >= 10 && progress < 20)
                textSeekTrack.setText("1");
            else if(progress >= 20 && progress < 30)
                textSeekTrack.setText("2");
            else if(progress >= 30 && progress < 40)
                textSeekTrack.setText("3");
            else if(progress >= 40 && progress < 50)
                textSeekTrack.setText("4");
            else if(progress >= 50 && progress < 60)
                textSeekTrack.setText("5");
            else if(progress >= 60 && progress < 70)
                textSeekTrack.setText("6");
            else if(progress >= 70 && progress < 80)
                textSeekTrack.setText("7");
            else if(progress >= 80 && progress < 90)
                textSeekTrack.setText("8");
            else if(progress >= 90 && progress < 100)
                textSeekTrack.setText("9");
            else if(progress == 100)
                textSeekTrack.setText("10");
        }
        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {}
        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {}
    };
    SeekBar.OnSeekBarChangeListener onSeekBarMin = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser)
        {
            if(progress < 8)
                 textSeekMin.setText("0");
            else if(progress >= 8 && progress < 16)
                 textSeekMin.setText("5");
            else if(progress >= 16 && progress < 25)
                 textSeekMin.setText("10");
            else if(progress >= 25 && progress < 33)
                 textSeekMin.setText("15");
            else if(progress >= 33 && progress < 42)
                textSeekMin.setText("20");
            else if(progress >= 42 && progress < 50)
                textSeekMin.setText("25");
            else if(progress >= 50 && progress < 58)
                textSeekMin.setText("30");
            else if(progress >= 58 && progress < 67)
                textSeekMin.setText("35");
            else if(progress >= 67 && progress < 75)
                textSeekMin.setText("40");
            else if(progress >= 75 && progress < 83)
                textSeekMin.setText("45");
            else if(progress >= 83 && progress < 92)
                textSeekMin.setText("50");
            else if(progress >= 92 && progress < 100)
                textSeekMin.setText("55");
            else if(progress == 100 )
                textSeekMin.setText("60");
        }
        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {}
        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {}
    };
}
