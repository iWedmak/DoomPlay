package com.perm.DoomPlay;

import android.content.Context;
import android.widget.Toast;

import java.io.*;
import java.util.ArrayList;

public class PlaylistParser
{
    public static boolean isFileCue(String fileName)
    {
        return fileName.endsWith(".cue") || fileName.endsWith(".CUE");
    }
    private static final String[] audioExts = { ".flac", ".FLAC", ".ape", ".APE", ".wv", ".WV", ".mpc", ".MPC", "m4a", "M4A",
            ".wav", ".WAV", ".mp3", ".MP3", ".wma", ".WMA", ".ogg", ".OGG", ".3gpp", ".3GPP", ".aac", ".AAC" };
    private static final String[] plistExts = { ".playlist", ".m3u", ".M3U", ".pls", ".PLS" };

    public static boolean isFilePlaylist(String s) {
        for(int i = 0; i < plistExts.length; i++) {
            if(s.endsWith(plistExts[i])) return true;
        }
        return false;
    }

    public static boolean hasAudioExt(String s) {
        for(int i = 0; i < audioExts.length; i++) {
            if(s.endsWith(audioExts[i])) return true;
        }
        return false;
    }

    public static ArrayList<Audio> displayCue(File fpath,Context context)
    {
        try
        {
            return parseCue(fpath);
        }
        catch (ParseException e)
        {
            switch(e.getErrorCode())
            {
                case ParseException.ERROR_CUE_BAD:
                    Toast.makeText(context, R.string.bad_cue, Toast.LENGTH_SHORT).show();
                    break;
                case ParseException.ERROR_CUE_NO_SRC:
                    Toast.makeText(context,R.string.no_cue_src,Toast.LENGTH_SHORT).show();
                    break;
                case ParseException.ERROR_CUE_SRC_EXT:
                    Toast.makeText(context,R.string.cue_src_ext,Toast.LENGTH_SHORT).show();
                    break;
            }
            return new ArrayList<Audio>();
        }
    }
    public static ArrayList<Audio> displayPlaylist(File fpath,Context context)
    {
        try {
            return parsePlaylist(fpath);
        } catch (ParseException e) {
            Toast.makeText(context,R.string.bad_playlist,Toast.LENGTH_SHORT).show();
            return new ArrayList<Audio>();
        }
    }

    public static ArrayList<Audio> parsePlaylist(File fpath) throws ParseException
    {
        ArrayList<Audio> audios = new ArrayList<Audio>();

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
            String path = null;
            if(fpath.getParent() != null) {
                path = fpath.getParent();
                if(!path.endsWith("/")) path += "/";
            }
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if(line.startsWith("File") && line.indexOf('=') > 4) {	// maybe it's a PLS file
                    int k, idx = line.indexOf('=');
                    for(k = 4; k < idx; k++) if(line.charAt(k) < '0' || line.charAt(k) > '9') break;
                    if(k != idx || idx == line.length()) continue;
                    line = line.substring(idx+1);
                }
                File f = new File(line);
                if(f.exists() && !f.isDirectory() && hasAudioExt(line)) {
                    audios.add(new Audio("unknown",f.getName(),line,0));
                    continue;
                }
                if(path != null) {
                    f = new File(path+line);	// maybe it was a relative path
                    if(f.exists() && !f.isDirectory() && hasAudioExt(path+line))
                        audios.add(new Audio("unknown",f.getName(),path+line,0));
                }
            }
            if(audios.size() == 0)
            {
                throw new ParseException(ParseException.ERROR_PLAYLIST_BAD);
            }
            return audios;
        } catch (Exception e) {
            throw new ParseException(ParseException.ERROR_PLAYLIST_BAD);
        }
    }

    public static ArrayList<Audio> parseCue(File fpath) throws ParseException
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


            while ((line = reader.readLine()) != null) {
                String s = line.trim();
                if(s.startsWith("FILE ")) {
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
                        throw new ParseException(ParseException.ERROR_CUE_NO_SRC);
                    } else if(!hasAudioExt(ff.getName())) {
                        throw new ParseException(ParseException.ERROR_CUE_SRC_EXT);
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
                throw new ParseException(ParseException.ERROR_CUE_BAD);
            }
            return audios;

        } catch (Exception e)
        {
            e.printStackTrace();
        }
        throw new ParseException(ParseException.ERROR_CUE_BAD);
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

    static class ParseException extends Exception
    {
        public int getErrorCode()
        {
            return errorCode;
        }

        private int errorCode;

        public static final int ERROR_CUE_NO_SRC = 1;
        public static final int ERROR_CUE_BAD = 2;
        public static final int ERROR_CUE_SRC_EXT = 3;
        public static final int ERROR_PLAYLIST_BAD = 4;


        public ParseException(int errorCode)
        {
            super("Error code"+errorCode);
            this.errorCode = errorCode;
        }
    }
}
