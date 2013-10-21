package com.un4seen.bass;

import com.perm.DoomPlay.DownloadingService;
import com.perm.DoomPlay.SettingActivity;

import java.io.*;
import java.nio.ByteBuffer;

public class BassPlayer
{
    public interface OnCompletionListener
    {
        void onCompletion();
    }
    public interface OnErrorListener
    {
        void onError();
    }
    public boolean isPlaying;

    private int chan;
    private int totalTime;
    private final int[] fx = new int[10];
    private OnCompletionListener completionListener;
    private OnErrorListener errorListener;


   public void setOnCompletetion(OnCompletionListener listener)
   {
        this.completionListener = listener;
   }

    public void setOnErrorListener(OnErrorListener listener)
    {
        this.errorListener = listener;
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
        chan = 0;
    }
    public void prepareFile(String url) throws IOException
    {
        String extension = (url.substring(url.lastIndexOf(".")+1)).toLowerCase();

        if(extension.equals("aac") || extension.equals("mp4"))
            chan = BASS_AAC.BASS_AAC_StreamCreateFile(url, 0L, 0L, BASS.BASS_SAMPLE_LOOP);
        else if(extension.equals("alac"))
            chan = BASS_ALAC.BASS_ALAC_StreamCreateFile(url, 0L, 0L, BASS.BASS_SAMPLE_LOOP);
        else if(extension.equals("ape"))
            chan = BASS_APE.BASS_APE_StreamCreateFile(url, 0L, 0L, BASS.BASS_SAMPLE_LOOP);
        else if(extension.equals("mpc"))
            chan = BASS_MPC.BASS_MPC_StreamCreateFile(url, 0L, 0L, BASS.BASS_SAMPLE_LOOP);
        else if(extension.equals("flac"))
            chan = BASSFLAC.BASS_FLAC_StreamCreateFile(url, 0L, 0L, BASS.BASS_SAMPLE_LOOP);
        else if(extension.equals("midi"))
            chan = BASSMIDI.BASS_MIDI_StreamCreateFile(url, 0L, 0L, BASS.BASS_SAMPLE_LOOP, 1);
        else if(extension.equals("opus"))
            chan = BASSOPUS.BASS_OPUS_StreamCreateFile(url, 0L, 0L, BASS.BASS_SAMPLE_LOOP);
        else if(extension.equals("wv"))
            chan = BASSWV.BASS_WV_StreamCreateFile(url, 0L, 0L, BASS.BASS_SAMPLE_LOOP);
        else
            chan = BASS.BASS_StreamCreateFile(url, 0L, 0L, BASS.BASS_SAMPLE_LOOP);


        if(chan == 0)
        {
            throw new IOException("prepare exception");
        }


        long bytes = BASS.BASS_ChannelGetLength(chan, BASS.BASS_POS_BYTE);
        totalTime = (int)BASS.BASS_ChannelBytes2Seconds(chan, bytes);
        BASS.BASS_CHANNELINFO info=new BASS.BASS_CHANNELINFO();
        BASS.BASS_ChannelGetInfo(chan, info);
        BASS.BASS_ChannelSetSync(chan, BASS.BASS_SYNC_END, 0, EndSync, 0);

    }

    private final Object lock = new Object();
    int req;

    public void prepareNet(String url) throws IOException
    {
        int r;
        synchronized(lock) {
            r=++req; // increment the request counter for this request
        }

        String extension = (url.substring(url.lastIndexOf(".")+1)).toLowerCase();

        int c;

        if(extension.equals("aac") || extension.equals("mp4"))
            c = BASS_AAC.BASS_AAC_StreamCreateURL(url, 0, BASS.BASS_STREAM_BLOCK|BASS.BASS_STREAM_STATUS, StatusProc, r);
        else if(extension.equals("alac"))
            c = BASS_ALAC.BASS_ALAC_StreamCreateURL(url, 0, BASS.BASS_STREAM_BLOCK|BASS.BASS_STREAM_STATUS, StatusProc, r);
        else if(extension.equals("mpc"))
            c = BASS_MPC.BASS_MPC_StreamCreateURL(url, 0, BASS.BASS_STREAM_BLOCK|BASS.BASS_STREAM_STATUS, StatusProc, r);
        else if(extension.equals("flac"))
            c = BASSFLAC.BASS_FLAC_StreamCreateURL(url, 0, BASS.BASS_STREAM_BLOCK|BASS.BASS_STREAM_STATUS, StatusProc, r);
        else if(extension.equals("midi"))
            c = BASSMIDI.BASS_MIDI_StreamCreateURL(url, 0, BASS.BASS_STREAM_BLOCK|BASS.BASS_STREAM_STATUS, StatusProc, r,1);
        else if(extension.equals("opus"))
            c = BASSOPUS.BASS_OPUS_StreamCreateURL(url, 0, BASS.BASS_STREAM_BLOCK|BASS.BASS_STREAM_STATUS, StatusProc, r);
        else if(extension.equals("wv"))
            c = BASSWV.BASS_WV_StreamCreateURL(url, 0, BASS.BASS_STREAM_BLOCK|BASS.BASS_STREAM_STATUS, StatusProc, r);
        else
            c = BASS.BASS_StreamCreateURL(url, 0, BASS.BASS_STREAM_BLOCK|BASS.BASS_STREAM_STATUS, StatusProc, r);


        synchronized(lock)
        {
            if (r!=req)
            { // there is a newer request, discard this stream
                if (c!=0)
                {
                    BASS.BASS_StreamFree(c);
                }
                throw new IOException("prepare exception");
            }
            chan=c; // this is now the current stream
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

        if(SettingActivity.getPreferences("savevkfile"))
            filePath = DownloadingService.defaultFolder + url.substring( url.lastIndexOf('/')+1, url.length());
    }


    OutputStream outputStream;
    String filePath;
    private final BASS.DOWNLOADPROC StatusProc=new BASS.DOWNLOADPROC()
    {
        @Override
        public void DOWNLOADPROC(ByteBuffer buffer, int length, Object user)
        {
            if(filePath != null)
            {
                if(outputStream == null)
                {
                    try {
                        outputStream = new FileOutputStream(filePath);
                    } catch (FileNotFoundException e) {
                        return;
                    }
                }
                else if(buffer != null)
                {
                    try {
                        outputStream.write(buffer.array(), 0, length);
                    } catch (IOException e) { }
                }
                else
                {
                    try {
                        outputStream.close();
                    } catch (IOException e) {}
                }
            }

        }
    };
    private void setUpEffects()
    {
        fx[0]=BASS.BASS_ChannelSetFX(chan, BASS.BASS_FX_DX8_PARAMEQ, 0);
        fx[1]=BASS.BASS_ChannelSetFX(chan, BASS.BASS_FX_DX8_PARAMEQ, 0);
        fx[2]=BASS.BASS_ChannelSetFX(chan, BASS.BASS_FX_DX8_PARAMEQ, 0);
        fx[3]=BASS.BASS_ChannelSetFX(chan, BASS.BASS_FX_DX8_REVERB, 0);

        BASS.BASS_DX8_PARAMEQ p = new BASS.BASS_DX8_PARAMEQ();

        p.fGain=0;
        p.fBandwidth=18;
        p.fCenter=125;
        BASS.BASS_FXSetParameters(fx[0], p);
        p.fCenter=1000;
        BASS.BASS_FXSetParameters(fx[1], p);
        p.fCenter=8000;
        BASS.BASS_FXSetParameters(fx[2], p);
    }


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

        if(outputStream != null)
        {
            new File(filePath).delete();
            try {
                outputStream.close();
            } catch (IOException e) {}

        }
        filePath = null;
    }
    public int getPercentage()
    {
        return (100*getCurrentPosition())/getTotalTime();
    }
}
