package com.perm.DoomPlay;

import com.un4seen.bass.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

public class BassPlayer
{
    public interface OnCompletionListener
    {
        void onCompletion();
    }
    public boolean isPlaying;

    private static int chan;
    private int totalTime;
    private OnCompletionListener completionListener;
    private static int[] fx = new int[10];


   public void setOnCompletetion(OnCompletionListener listener)
   {
        this.completionListener = listener;
   }

   private final BASS.SYNCPROC EndSync=new BASS.SYNCPROC()
   {
        public void SYNCPROC(int handle, int channel, int data, Object user)
        {
            isPlaying = false;
            if(completionListener != null)
                completionListener.onCompletion();
        }
    };

    public BassPlayer()
    {
        BASS.BASS_Init(-1, 44100, 0);
        BASS.BASS_SetConfig(BASS.BASS_CONFIG_FLOATDSP,32);
        chan = 0;

        String path = MyApplication.getInstance().getApplicationInfo().nativeLibraryDir;
        String[] list = new File(path).list();
        for (String s: list)
        {
            BASS.BASS_PluginLoad(path+"/"+s, 0);
        }
    }
    public void prepareFile(String url) throws IOException
    {
        chan = BASS.BASS_StreamCreateFile(url, 0L, 0L, 0);

        if(chan == 0)
        {
            throw new IOException("prepare exception"+ BASS.BASS_ErrorGetCode());
        }

        long bytes = BASS.BASS_ChannelGetLength(chan, BASS.BASS_POS_BYTE);
        totalTime = (int)BASS.BASS_ChannelBytes2Seconds(chan, bytes);
        BASS.BASS_CHANNELINFO info=new BASS.BASS_CHANNELINFO();
        BASS.BASS_ChannelGetInfo(chan, info);
        BASS.BASS_ChannelSetSync(chan, BASS.BASS_SYNC_END, 0, EndSync, 0);

        setUpEffects();

    }
    private void setUpEffects()
    {
        fx[0] = BASS.BASS_ChannelSetFX(chan, BASS.BASS_FX_DX8_PARAMEQ, 0);
        fx[1] = BASS.BASS_ChannelSetFX(chan, BASS.BASS_FX_DX8_PARAMEQ, 0);
        fx[2] = BASS.BASS_ChannelSetFX(chan, BASS.BASS_FX_DX8_PARAMEQ, 0);
        fx[3] = BASS.BASS_ChannelSetFX(chan, BASS.BASS_FX_DX8_PARAMEQ, 0);
        fx[4] = BASS.BASS_ChannelSetFX(chan, BASS.BASS_FX_DX8_PARAMEQ, 0);
        fx[5] = BASS.BASS_ChannelSetFX(chan, BASS.BASS_FX_DX8_PARAMEQ, 0);
        fx[6] = BASS.BASS_ChannelSetFX(chan, BASS.BASS_FX_DX8_PARAMEQ, 0);
        fx[7] = BASS.BASS_ChannelSetFX(chan, BASS.BASS_FX_DX8_PARAMEQ, 0);
        fx[8] = BASS.BASS_ChannelSetFX(chan, BASS.BASS_FX_DX8_PARAMEQ, 0);
        fx[9] = BASS.BASS_ChannelSetFX(chan, BASS.BASS_FX_DX8_PARAMEQ, 0);

        BASS.BASS_DX8_PARAMEQ p = new BASS.BASS_DX8_PARAMEQ();

        p.fGain=0;
        p.fBandwidth = 0.5f;
        p.fCenter=32;
        BASS.BASS_FXSetParameters(fx[0], p);
        p.fCenter=64;
        BASS.BASS_FXSetParameters(fx[1], p);
        p.fCenter=125;
        BASS.BASS_FXSetParameters(fx[2], p);
        p.fBandwidth = 2f;
        p.fCenter=250;
        BASS.BASS_FXSetParameters(fx[3], p);
        p.fCenter=500;
        BASS.BASS_FXSetParameters(fx[4], p);
        p.fBandwidth = 4f;
        p.fCenter=1000;
        BASS.BASS_FXSetParameters(fx[5], p);
        p.fCenter=2000;
        BASS.BASS_FXSetParameters(fx[6], p);
        p.fCenter=4000;
        BASS.BASS_FXSetParameters(fx[7], p);
        p.fCenter=8000;
        p.fBandwidth = 8f;
        BASS.BASS_FXSetParameters(fx[8], p);
        p.fCenter=16000;
        BASS.BASS_FXSetParameters(fx[9], p);


        int[] bounds  = EqualizerActivity.getSavedBounds();
        for(int i = 0 ; i < bounds.length ; i++ )
            updateFX(bounds[i], i);



    }
    public static void updateFX(int progress, int n)
    {
        BASS.BASS_DX8_PARAMEQ p = new BASS.BASS_DX8_PARAMEQ();
        BASS.BASS_FXGetParameters(fx[n], p);
        p.fGain = EqualizerActivity.convertProgressToGain(progress);
        BASS.BASS_FXSetParameters(fx[n], p);

         /*
            ***Reverb***
             BASS.BASS_DX8_REVERB p=new BASS.BASS_DX8_REVERB();
            BASS.BASS_FXGetParameters(fx[n], p);
            p.fReverbMix=(float)(progress > 15 ? Math.log(progress/20.0)*20.0:-96.0);
            BASS.BASS_FXSetParameters(fx[n], p);

         */
    }

    private final Object lock = new Object();
    int req;

    public void prepareNet(String url) throws IOException
    {
        int r;
        synchronized(lock)
        {
            r=++req;
        }

        int c = BASS.BASS_StreamCreateURL(url, 0, BASS.BASS_STREAM_STATUS, StatusProc, r);

        synchronized(lock)
        {
            if (r!=req)
            {
                if (c!=0)
                {
                    BASS.BASS_StreamFree(c);
                }
                throw new IOException("prepare exception");
            }
            chan=c;
        }

        if(chan == 0)
        {
            throw new IOException("prepare exception");
        }

        long bytes = BASS.BASS_ChannelGetLength(chan, BASS.BASS_POS_BYTE);
        totalTime = (int)BASS.BASS_ChannelBytes2Seconds(chan, bytes);
        BASS.BASS_CHANNELINFO info=new BASS.BASS_CHANNELINFO();
        BASS.BASS_ChannelGetInfo(chan, info);
        BASS.BASS_ChannelSetSync(chan, BASS.BASS_SYNC_END, 0, EndSync, 0);

        setUpEffects();

        if(SettingActivity.getPreferences("savevkfile"))
            filePath = DownloadingService.getDownloadDir() + url.substring( url.lastIndexOf('/')+1, url.length());
    }


    private FileChannel fc;
    private String filePath;
    private final BASS.DOWNLOADPROC StatusProc=new BASS.DOWNLOADPROC()
    {
        @Override
        public void DOWNLOADPROC(ByteBuffer buffer, int length, Object user)
        {

            if(filePath != null && (Integer)user == req )
            {

                try
                {
                    if (buffer!=null)
                    {
                        if (fc==null)
                            fc=new FileOutputStream(new File(filePath)).getChannel();
                        fc.write(buffer);
                    }
                    else if (fc!=null)
                    {
                        fc.close();
                        fc = null;
                    }
                }
                catch (IOException e) {}
            }

        }
    };

    public void start()
    {
        BASS.BASS_ChannelPlay(chan, false);
        isPlaying = true;
    }
    public void pause()
    {
        BASS.BASS_ChannelPause(chan);
        isPlaying = false;
    }

    public int getCurrentPosition()
    {
        return (int)BASS.BASS_ChannelBytes2Seconds(chan, BASS.BASS_ChannelGetPosition(chan, BASS.BASS_POS_BYTE));
    }

    public int getTotalTime()
    {
        return totalTime;
    }

    public void seekTo(int to)
    {
        BASS.BASS_ChannelSetPosition(chan, BASS.BASS_ChannelSeconds2Bytes(chan, to), BASS.BASS_POS_BYTE);
    }

    public void releaseTotal()
    {
        BASS.BASS_Free();
        BASS.BASS_PluginFree(0);
    }
    public void release()
    {
        BASS.BASS_MusicFree(chan);
        BASS.BASS_StreamFree(chan);

        assert fc == null;
        filePath = null;
    }
    public int getPercentage()
    {
        if(getTotalTime() == 0)
            return 0;
        return (100*getCurrentPosition())/getTotalTime();
    }
}
