package com.perm.vkontakte.api;

import java.io.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class Utils {
    
    public static String extractPattern(String string, String pattern){
        Pattern p = Pattern.compile(pattern);
        Matcher m = p.matcher(string);
        if (!m.find())
            return null;
        return m.toMatchResult().group(1);
    }
    
    public static String convertStreamToString(InputStream is) throws IOException {
        InputStreamReader r = new InputStreamReader(is);
        StringWriter sw = new StringWriter();
        char[] buffer = new char[1024];
        try {
            for (int n; (n = r.read(buffer)) != -1;)
                sw.write(buffer, 0, n);
        }
        finally{
            try {
                is.close();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }
        return sw.toString();
    }
    
    public static void closeStream(Object oin) {
        if(oin!=null)
            try {
                if(oin instanceof InputStream)
                    ((InputStream)oin).close();
                if(oin instanceof OutputStream)
                    ((OutputStream)oin).close();
            } catch (IOException e) {
                e.printStackTrace();
            }
    }
    
    private static final String pattern_string_profile_id = "^(id)?(\\d{1,10})$";
    private static final Pattern pattern_profile_id = Pattern.compile(pattern_string_profile_id);
    public static String parseProfileId(String text) {
        Matcher m = pattern_profile_id.matcher(text);
        if (!m.find())
            return null;
        return m.group(2);
    }
}
