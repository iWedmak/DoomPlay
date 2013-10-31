package com.perm.DoomPlay;

import android.content.Context;
import android.widget.Toast;

import java.io.*;
import java.util.ArrayList;

public class CueFile
{
    public static boolean isFileCue(String fileName)
    {
        return fileName.endsWith(".cue") || fileName.endsWith(".CUE");
    }
    private static final String[] audioExts = { ".flac", ".FLAC", ".ape", ".APE", ".wv", ".WV", ".mpc", ".MPC", "m4a", "M4A",
            ".wav", ".WAV", ".mp3", ".MP3", ".wma", ".WMA", ".ogg", ".OGG", ".3gpp", ".3GPP", ".aac", ".AAC" };


    public static boolean hasAudioExt(String s) {
        for(int i = 0; i < audioExts.length; i++) {
            if(s.endsWith(audioExts[i])) return true;
        }
        return false;
    }

    private static String replaceSkips(String s)
    {
        char c = '"';
        return s.replaceAll("[c]","");
    }

    public static ArrayList<Audio> displayCue(File fpath,Context context)
    {
        try
        {
            return parseCue(fpath);
        }
        catch (CueFile.ParseCueException e)
        {
            switch(e.getErrorCode())
            {
                case CueFile.ParseCueException.ERROR_BAD_CUE:
                    Toast.makeText(context, R.string.bad_cue, Toast.LENGTH_SHORT).show();
                    break;
                case CueFile.ParseCueException.ERROR_NO_CUE_SRC:
                    Toast.makeText(context,R.string.no_cue_src,Toast.LENGTH_SHORT).show();
                    break;
                case CueFile.ParseCueException.ERROR_SRC_EXT:
                    Toast.makeText(context,R.string.cue_src_ext,Toast.LENGTH_SHORT).show();
                    break;
            }
            return new ArrayList<Audio>();
        }
    }

    public static ArrayList<Audio> parseCue(File fpath) throws ParseCueException
    {
        try {
            BufferedReader reader;
            if (checkUTF16(fpath)) {
                reader = new BufferedReader(new InputStreamReader(
                        new FileInputStream(fpath), "UTF-16"), 8192);
            } else {
                reader = new BufferedReader(new InputStreamReader(
                        new FileInputStream(fpath)), 8192);
            }
            String line = null;
            ArrayList<Audio> audios = new ArrayList<Audio>();

            String cur_file = null;
            String cur_track_title = null;
            String cur_track_artist = null;
            int cur_track = 0;

            String path = fpath.toString();
            int plen = path.lastIndexOf('/');
            path = (plen < 0) ? "/" : path.substring(0, plen+1);

            // Simple CUE parser

            while ((line = reader.readLine()) != null) {
                String s = line.trim();
                // 	log_msg("first trying: " + s);
                if(s.startsWith("FILE ") /* && s.endsWith(" WAVE") */) {
                    if(s.charAt(5) == '\"') {
                        int i = s.lastIndexOf('\"');
                        if(i < 7) continue;
                        cur_file = s.substring(6, i);
                    } else {
                        int i = s.lastIndexOf(' ');
                        if(i < 6) continue;
                        cur_file = s.substring(5, i);
                    }
                    File ff = new File(path + cur_file);
                    //		log_msg("trying: " + ff.toString());
                    if(!ff.exists()) {
                        // sometimes cues reference source with wrong extension
                        int kk = (path + cur_file).lastIndexOf('.');
                        if(kk > 0) {
                            File f = null;
                            String ss = (path + cur_file).substring(0, kk);
                            for(kk = 0; kk < audioExts.length; kk++) {
                                f = new File(ss + audioExts[kk]);
                                if(f.exists()) break;
                            }
                            if(kk < audioExts.length) {
                                cur_file = cur_file.substring(0,cur_file.lastIndexOf('.')) + audioExts[kk];
                                continue;
                            }
                        }
                        throw new ParseCueException(ParseCueException.ERROR_NO_CUE_SRC);
                    } else if(!hasAudioExt(ff.getName())) {
                        throw new ParseCueException(ParseCueException.ERROR_SRC_EXT);
                    }

                }
                else if(s.startsWith("PERFORMER ")) {
                    if(s.charAt(8) == '\"') {
                        int i = s.lastIndexOf('\"');
                        if(i < 12) continue;
                        cur_track_artist = s.substring(11, i);
                    }	 else {
                        int i = s.length();
                        if(i < 11) continue;
                        cur_track_artist = s.substring(10, i);
                    }
                }else if(s.startsWith("TRACK ") && s.endsWith(" AUDIO")) {
                    String tr = s.substring(6, 8);
                    cur_track = (Integer.valueOf(tr)).intValue();
                } else if(s.startsWith("TITLE ")) {
                    if(s.charAt(6) == '\"') {
                        int i = s.lastIndexOf('\"');
                        if(i < 8) continue;
                        cur_track_title = s.substring(7, i);
                    }	 else {
                        int i = s.length();
                        if(i < 7) continue;
                        cur_track_title = s.substring(6);
                    }
                } else if(s.startsWith("INDEX 01 ")) {
                    if(s.charAt(11) != ':' || s.charAt(14) != ':') {
                        continue;
                    }
                    if(cur_file == null)
                        continue;

                    audios.add(new Audio(cur_track_artist == null ? "unknown" : cur_track_artist,
                            cur_track_title == null ? String.valueOf(cur_track) : cur_track_title,path + cur_file,0));

                    cur_track_title = null;
                    cur_track_artist = null;
                }
            }
            if(audios.size() < 1) {
                throw new ParseCueException(ParseCueException.ERROR_BAD_CUE);
            }
            return audios;

        } catch (Exception e)
        {
            e.printStackTrace();
        }
        throw new ParseCueException(ParseCueException.ERROR_BAD_CUE);
    }
    private static boolean checkUTF16(File fpath) throws IOException
    {
        FileInputStream fr = new FileInputStream(fpath);
        byte[] bytes = new byte[2];
        if(fr.read(bytes, 0, 2) < 2)
        {
            throw new IOException("failed reading file in checkUTF16()");
        }
        fr.close();

        // First two bytes are equals to Byte Order Mark
        // for Little Endian (0xFFFE) or Big Endian (0xFEFF).
        return (bytes[0] == -1 && bytes[1] == -2)
                || (bytes[0] == -2 && bytes[1] == -1);
    }

    static class ParseCueException extends Exception
    {
        public int getErrorCode()
        {
            return errorCode;
        }

        private int errorCode;

        public static final int ERROR_NO_CUE_SRC = 1;
        public static final int ERROR_BAD_CUE = 2;
        public static final int ERROR_SRC_EXT = 3;


        public ParseCueException(int errorCode)
        {
            super("Error code"+errorCode);
            this.errorCode = errorCode;
        }
    }
}
