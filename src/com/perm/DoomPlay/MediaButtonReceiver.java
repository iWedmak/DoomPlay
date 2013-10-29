package com.perm.DoomPlay;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.view.KeyEvent;

public class MediaButtonReceiver extends BroadcastReceiver
{

    @Override
    public void onReceive(Context context, Intent intent)
    {
        if (Intent.ACTION_MEDIA_BUTTON.equals(intent.getAction())) {

            KeyEvent event = (KeyEvent) intent
                    .getParcelableExtra(Intent.EXTRA_KEY_EVENT);

            if (event == null )
                return;

                switch (event.getKeyCode())
                {
                    case KeyEvent.KEYCODE_MEDIA_STOP:
                    case KeyEvent.KEYCODE_HEADSETHOOK:
                    case KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE:
                        context.startService(new Intent(PlayingService.actionPlay));
                        break;
                    case KeyEvent.KEYCODE_MEDIA_NEXT:
                        context.startService(new Intent(PlayingService.actionNext));
                        break;
                    case KeyEvent.KEYCODE_MEDIA_PREVIOUS:
                        context.startService(new Intent(PlayingService.actionPrevious));
                        break;
                }

            this.abortBroadcast();

        }
        else if (android.media.AudioManager.ACTION_AUDIO_BECOMING_NOISY.equals(intent.getAction()))
        {
            context.startService(new Intent(PlayingService.actionClose));
        }
    }
}
