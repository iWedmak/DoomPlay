package com.perm.DoomPlay;

public class CueFile
{
    public static boolean isFileCue(String fileName)
    {
        return fileName.endsWith(".cue") || fileName.endsWith(".CUE");
    }
}
