package com.perm.vkontakte.api;

import android.util.Log;
import com.perm.DoomPlay.Audio;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.zip.GZIPInputStream;

public class Api {
    static final String TAG="Kate.Api";
    
    public static final String BASE_URL="https://api.vk.com/method/";
    
    public Api(String access_token, String api_id){
        this.access_token=access_token;
        this.api_id=api_id;
    }
    
    final String access_token;
    final String api_id;
    
    //TODO: it's not faster, even slower on slow devices. Maybe we should add an option to disable it. It's only good for paid internet connection.
    static final boolean enable_compression=true;
    
    /*** utils methods***/
    private void checkError(JSONObject root, String url) throws JSONException,KException {
        if(!root.isNull("error")){
            JSONObject error=root.getJSONObject("error");
            int code=error.getInt("error_code");
            String message=error.getString("error_msg");
            KException e = new KException(code, message, url); 
            if (code==14) {
                e.captcha_img = error.optString("captcha_img");
                e.captcha_sid = error.optString("captcha_sid");
            }
            throw e;
        }
        if(!root.isNull("execute_errors")){
            JSONArray errors=root.getJSONArray("execute_errors");
            if(errors.length()==0)
                return;
            //only first error is processed if there are multiple
            JSONObject error=errors.getJSONObject(0);
            int code=error.getInt("error_code");
            String message=error.getString("error_msg");
            KException e = new KException(code, message, url); 
            if (code==14) {
                e.captcha_img = error.optString("captcha_img");
                e.captcha_sid = error.optString("captcha_sid");
            }
            throw e;
        }
    }
    

    private final static int MAX_TRIES=3;
    private JSONObject sendRequest(Params params) throws IOException, JSONException, KException {
        String url = getSignedUrl(params);
        String body="";


        String response="";
        for(int i=1;i<=MAX_TRIES;++i){
            try{

                response = sendRequestInternal(url, body);
                break;
            }catch(javax.net.ssl.SSLException ex){
                processNetworkException(i, ex);
            }catch(java.net.SocketException ex){
                processNetworkException(i, ex);
            }
        }
        Log.i(TAG, "response="+response);
        JSONObject root=new JSONObject(response);
        checkError(root, url);
        return root;
    }

    private void processNetworkException(int i, IOException ex) throws IOException {
        ex.printStackTrace();
        if(i==MAX_TRIES)
            throw ex;
    }

    private String sendRequestInternal(String url, String body) throws IOException, WrongResponseCodeException {
        HttpURLConnection connection=null;
        try{
            connection = (HttpURLConnection)new URL(url).openConnection();
            connection.setConnectTimeout(30000);
            connection.setReadTimeout(30000);
            connection.setUseCaches(false);
            connection.setDoInput(true);
            connection.setRequestMethod("GET");
            if(enable_compression)
                connection.setRequestProperty("Accept-Encoding", "gzip");

            int code=connection.getResponseCode();
            Log.i(TAG, "code="+code);
            //It may happen due to keep-alive problem http://stackoverflow.com/questions/1440957/httpurlconnection-getresponsecode-returns-1-on-second-invocation
            if (code==-1)
                throw new WrongResponseCodeException("Network error");
            //может стоит проверить на код 200
            //on error can also read error stream from connection.
            InputStream is = new BufferedInputStream(connection.getInputStream(), 8192);
            String enc=connection.getHeaderField("Content-Encoding");
            if(enc!=null && enc.equalsIgnoreCase("gzip"))
                is = new GZIPInputStream(is);
            String response=Utils.convertStreamToString(is);
            return response;
        }
        finally{
            if(connection!=null)
                connection.disconnect();
        }
    }
    
    private String getSignedUrl(Params params) {
        params.put("access_token", access_token);
        
        String args = "";

        args=params.getParamsString();

        /*
        for(int i = 0 ; i < 10 ; i ++)
            Log.d("TAGGGGG", BASE_URL+params.method_name+"?"+args );
        */

        return BASE_URL+params.method_name+"?"+args;
    }
    
    public static String unescape(String text){
        if(text==null)
            return null;
        return text.replace("&amp;", "&").replace("&quot;", "\"").replace("<br>", "\n").replace("&gt;", ">").replace("&lt;", "<")
        .replace("&#39;", "'").replace("<br/>", "\n").replace("&ndash;","-").replace("&#33;", "!").trim();
        //возможно тут могут быть любые коды после &#, например были: 092 - backslash \
    }
    
    public static String unescapeWithSmiles(String text){
        return unescape(text)
                //May be useful to someone
                //.replace("\uD83D\uDE0A", ":-)")
                //.replace("\uD83D\uDE03", ":D")
                //.replace("\uD83D\uDE09", ";-)")
                //.replace("\uD83D\uDE06", "xD")
                //.replace("\uD83D\uDE1C", ";P")
                //.replace("\uD83D\uDE0B", ":p")
                //.replace("\uD83D\uDE0D", "8)")
                //.replace("\uD83D\uDE0E", "B)")
                //
                //.replace("\ud83d\ude12", ":(")  //F0 9F 98 92
                //.replace("\ud83d\ude0f", ":]")  //F0 9F 98 8F
                //.replace("\ud83d\ude14", "3(")  //F0 9F 98 94
                //.replace("\ud83d\ude22", ":'(")  //F0 9F 98 A2
                //.replace("\ud83d\ude2d", ":_(")  //F0 9F 98 AD
                //.replace("\ud83d\ude29", ":((")  //F0 9F 98 A9
                //.replace("\ud83d\ude28", ":o")  //F0 9F 98 A8
                //.replace("\ud83d\ude10", ":|")  //F0 9F 98 90
                //                           
                //.replace("\ud83d\ude0c", "3)")  //F0 9F 98 8C
                //.replace("\ud83d\ude20", ">(")  //F0 9F 98 A0
                //.replace("\ud83d\ude21", ">((")  //F0 9F 98 A1
                //.replace("\ud83d\ude07", "O:)")  //F0 9F 98 87
                //.replace("\ud83d\ude30", ";o")  //F0 9F 98 B0
                //.replace("\ud83d\ude32", "8o")  //F0 9F 98 B2
                //.replace("\ud83d\ude33", "8|")  //F0 9F 98 B3
                //.replace("\ud83d\ude37", ":X")  //F0 9F 98 B7
                //                           
                //.replace("\ud83d\ude1a", ":*")  //F0 9F 98 9A
                //.replace("\ud83d\ude08", "}:)")  //F0 9F 98 88
                //.replace("\u2764", "<3")  //E2 9D A4   
                //.replace("\ud83d\udc4d", ":like:")  //F0 9F 91 8D
                //.replace("\ud83d\udc4e", ":dislike:")  //F0 9F 91 8E
                //.replace("\u261d", ":up:")  //E2 98 9D   
                //.replace("\u270c", ":v:")  //E2 9C 8C   
                //.replace("\ud83d\udc4c", ":ok:")  //F0 9F 91 8C
                ;
    }

    /*** API methods ***/
    //http://vk.com/dev/database.getCities


    <T> String arrayToString(Collection<T> items) {
        if(items==null)
            return null;
        String str_cids = "";
        for (Object item:items){
            if(str_cids.length()!=0)
                str_cids+=',';
            str_cids+=item;
        }
        return str_cids;
    }


    /*** methods for friends ***/
    //http://vk.com/dev/friends.get
    public ArrayList<User> getFriends(Long user_id) throws IOException, JSONException, KException
    {
        Params params = new Params("friends.get");
        params.put("fields","first_name,last_name,photo_medium");
        params.put("uid",user_id);
        params.put("order","hints");
        
        //addCaptchaParams(captcha_key, captcha_sid, params);
        JSONObject root = sendRequest(params);
        ArrayList<User> users=new ArrayList<User>();
        JSONArray array=root.optJSONArray("response");
        //if there are no friends "response" will not be array
        if(array==null)
            return users;
        int category_count=array.length();
        for(int i=0; i<category_count; ++i){
            JSONObject o = (JSONObject)array.get(i);
            User u = User.parse(o);
            users.add(u);
        }
        return users;
    }
    
    //http://vk.com/dev/photos.get
    public ArrayList<Photo> getPhotos(Long uid, Long aid, Integer offset, Integer count) throws IOException, JSONException, KException{
        Params params = new Params("photos.get");
        if(uid>0)
            params.put("uid", uid);
        else
            params.put("gid", -uid);
        params.put("aid", aid);
        params.put("extended", "1");
        params.put("offset",offset);
        params.put("limit",count);
        params.put("v","4.1");
        JSONObject root = sendRequest(params);
        JSONArray array = root.optJSONArray("response");
        if (array == null)
            return new ArrayList<Photo>(); 
        ArrayList<Photo> photos = parsePhotos(array);
        return photos;
    }
    
    //http://vk.com/dev/photos.getUserPhotos
    public ArrayList<Photo> getUserPhotos(Long uid, Integer offset, Integer count) throws IOException, JSONException, KException{
        Params params = new Params("photos.getUserPhotos");
        params.put("uid", uid);
        params.put("sort","0");
        params.put("count",count);
        params.put("offset",offset);
        params.put("extended",1);
        JSONObject root = sendRequest(params);
        JSONArray array = root.optJSONArray("response");
        if (array == null)
            return new ArrayList<Photo>(); 
        ArrayList<Photo> photos = parsePhotos(array);
        return photos;
    }
    private ArrayList<Photo> parsePhotos(JSONArray array) throws JSONException {
        ArrayList<Photo> photos=new ArrayList<Photo>();
        int category_count=array.length();
        for(int i=0; i<category_count; ++i){
            //in getUserPhotos first element is integer
            if(array.get(i) instanceof JSONObject == false)
                continue;
            JSONObject o = (JSONObject)array.get(i);
            Photo p = Photo.parse(o);
            photos.add(p);
        }
        return photos;
    }

    //http://vk.com/dev/photos.getAll
    public ArrayList<Photo> getAllPhotos(Long owner_id, Integer offset, Integer count, boolean extended) throws IOException, JSONException, KException{
        Params params = new Params("photos.getAll");
        params.put("owner_id", owner_id);
        params.put("offset", offset);
        params.put("count",count);
        params.put("extended",extended?1:0);
        JSONObject root = sendRequest(params);
        JSONArray array = root.optJSONArray("response");
        if (array == null)
            return new ArrayList<Photo>(); 
        ArrayList<Photo> photos = parsePhotos(array);
        return photos;
    }

    
    private void addCaptchaParams(String captcha_key, String captcha_sid, Params params) {
        params.put("captcha_sid",captcha_sid);
        params.put("captcha_key",captcha_key);
    }


    /*** for audio ***/
    //http://vk.com/dev/audio.get
    public ArrayList<Audio> getAudio(Long uid, Long gid, Long album_id,int count) throws IOException, JSONException, KException{
        Params params = new Params("audio.get");
        params.put("uid", uid);
        params.put("gid", gid);
        params.put("album_id", album_id);
        params.put("count", count);
        //addCaptchaParams(captcha_key, captcha_sid, params);
        JSONObject root = sendRequest(params);
        JSONArray array = root.optJSONArray("response");
        return parseAudioList(array, 0);
    }


    //http://vk.com/dev/audio.getById
    public ArrayList<Audio> getAudioById(String audios, String captcha_key, String captcha_sid) throws IOException, JSONException, KException{
        Params params = new Params("audio.getById");
        params.put("audios", audios);
        addCaptchaParams(captcha_key, captcha_sid, params);
        JSONObject root = sendRequest(params);
        JSONArray array = root.optJSONArray("response");
        return parseAudioList(array, 0);
    }
    
    //http://vk.com/dev/audio.getLyrics
    public String getLyrics(Long id) throws IOException, JSONException, KException{
        Params params = new Params("audio.getLyrics");
        params.put("lyrics_id", id);
        JSONObject root = sendRequest(params);
        JSONObject response = root.optJSONObject("response");
        return response.optString("text");
    }
    
    /*** for crate album ***/

    //http://vk.com/dev/audio.getUploadServer
    public String getAudioUploadServer() throws IOException, JSONException, KException {
        Params params = new Params("audio.getUploadServer");
        JSONObject root = sendRequest(params);
        JSONObject response = root.getJSONObject("response");
        return response.getString("upload_url");
    }

    //http://vk.com/dev/audio.save
    public Audio saveAudio(String server, String audio, String hash, String artist, String title) throws IOException, JSONException, KException {
        Params params = new Params("audio.save");
        params.put("server",server);
        params.put("audio",audio);
        params.put("hash",hash);
        params.put("artist",artist);
        params.put("title",title);
        JSONObject root = sendRequest(params);
        JSONObject response=root.getJSONObject("response");
        return Audio.parseAudio(response);
    }

    
    //http://vk.com/dev/audio.search
    public ArrayList<Audio> searchAudio(String query, int count) throws IOException, JSONException, KException{
        Params params = new Params("audio.search");
        params.put("q", query);
        params.put("sort", "2");
        params.put("lyrics", "0");
        params.put("count", count);
        params.put("offset", "0");
        params.put("auto_complete", "1");
        //addCaptchaParams(captcha_key, captcha_sid, params);
        JSONObject root = sendRequest(params);
        JSONArray array = root.optJSONArray("response");
        return parseAudioList(array, 1);
    }

    private ArrayList<Audio> parseAudioList(JSONArray array, int type_array) //type_array must be 0 or 1
            throws JSONException {
        ArrayList<Audio> audios = new ArrayList<Audio>();
        if (array != null) {
            for(int i = type_array; i<array.length(); ++i) { //get(0) is integer, it is audio count
                JSONObject o = (JSONObject)array.get(i);
                audios.add(Audio.parseAudio(o));
            }
        }
        return audios;
    }
    
    //http://vk.com/dev/audio.delete
    public String deleteAudio(Long aid, Long oid) throws IOException, JSONException, KException{
        Params params = new Params("audio.delete");
        params.put("aid", aid);
        params.put("oid", oid);
        JSONObject root = sendRequest(params);
        Object response_code = root.opt("response");
        if (response_code != null)
            return String.valueOf(response_code);
        return null;
    }

    //http://vk.com/dev/audio.add
    public String addAudio(Long aid, Long oid) throws IOException, JSONException, KException
    {
        Params params = new Params("audio.add");
        params.put("aid", aid);
        params.put("oid", oid);
        //addCaptchaParams(captcha_key, captcha_sid, params);
        JSONObject root = sendRequest(params);
        Object response_code = root.opt("response");
        if (response_code != null)
            return String.valueOf(response_code);
        return null;
    }
    

    
    //http://vk.com/dev/likes.add
    public Long addLike(Long owner_id, Long item_id, String type, String access_key, String captcha_key, String captcha_sid) throws IOException, JSONException, KException{
        Params params = new Params("likes.add");
        params.put("owner_id", owner_id);
        params.put("item_id", item_id);
        params.put("type", type);
        params.put("access_key", access_key);
        addCaptchaParams(captcha_key, captcha_sid, params);
        JSONObject root = sendRequest(params);
        JSONObject response = root.optJSONObject("response");
        long likes=response.optLong("likes", -1);
        return likes;
    }
    
    //http://vk.com/dev/likes.delete
    public Long deleteLike(Long owner_id, String type, Long item_id, String captcha_key, String captcha_sid) throws IOException, JSONException, KException{
        Params params = new Params("likes.delete");
        params.put("owner_id", owner_id);
        params.put("type", type);
        params.put("item_id", item_id);
        addCaptchaParams(captcha_key, captcha_sid, params);
        JSONObject root = sendRequest(params);
        JSONObject response = root.optJSONObject("response");
        return response.optLong("likes", -1);
    }

    //http://vk.com/dev/execute
    public void execute(String code) throws IOException, JSONException, KException {
        Params params = new Params("execute");
        params.put("code", code);
        sendRequest(params);
    }

    
    //http://vk.com/dev/friends.getLists


    //http://vk.com/dev/groups.getById
    public ArrayList<Group> getGroups(Long uid, Integer count) throws IOException, JSONException, KException{

        if(count == null)
            count =  600;

        Params params = new Params("groups.get");
        params.put("uid", uid);
        params.put("extended",1);
        params.put("count", count);

        //params.put("fields", fields); //Possible values: place,wiki_page,city,country,description,start_date,finish_date,site,fixed_post
        JSONObject root = sendRequest(params);
        JSONArray array=root.optJSONArray("response");
        return Group.parseGroups(array);
    }


    //http://vk.com/dev/audio.getAlbums
    public ArrayList<AudioAlbum> getAudioAlbums(Long uid, Long gid, Integer count) throws IOException, JSONException, KException{
        Params params = new Params("audio.getAlbums");
        params.put("uid", uid);
        params.put("gid", gid);
        params.put("count", count);
        JSONObject root = sendRequest(params);
        JSONArray array = root.optJSONArray("response");
        ArrayList<AudioAlbum> albums = AudioAlbum.parseAlbums(array);
        return albums;
    }
    
    //http://vk.com/dev/audio.getRecommendations
    public ArrayList<Audio> getAudioRecommendations(int count,long userId) throws IOException, JSONException, KException{
        Params params = new Params("audio.getRecommendations");
        params.put("user_id",userId);
        params.put("count",count);
        JSONObject root = sendRequest(params);
        JSONArray array = root.optJSONArray("response");
        return parseAudioList(array, 0);
    }
    
    //http://vk.com/dev/audio.getPopular
    public ArrayList<Audio> getAudioPopular(int genre_id,int count) throws IOException, JSONException, KException{
        Params params = new Params("audio.getPopular");
        params.put("genre_id", genre_id);
        params.put("count", count);
        JSONObject root = sendRequest(params);
        JSONArray array = root.optJSONArray("response");
        return parseAudioList(array, 0);
    }

    //gets status of broadcasting user current audio to his page
    public boolean audioGetBroadcast() throws IOException, JSONException, KException {
        Params params = new Params("audio.getBroadcast");
        JSONObject root = sendRequest(params);
        JSONObject response = root.optJSONObject("response");
        return response.optInt("enabled")==1;
    }

    //http://vk.com/dev/audio.setBroadcast
    public boolean audioSetBroadcast(boolean enabled) throws IOException, JSONException, KException {
        Params params = new Params("audio.setBroadcast");
        params.put("enabled",enabled?"1":"0");
        JSONObject root = sendRequest(params);
        JSONObject response = root.optJSONObject("response");
        return response.optInt("enabled")==1;
    }
    
    //http://vk.com/dev/audio.addAlbum
    public Long addAudioAlbum(String title) throws IOException, JSONException, KException {
        Params params = new Params("audio.addAlbum");
        params.put("title", title);
        JSONObject root = sendRequest(params);
        JSONObject obj = root.getJSONObject("response");
        return obj.optLong("album_id");
    }
    
    //http://vk.com/dev/audio.editAlbum
    public Integer editAudioAlbum(String title, long album_id) throws IOException, JSONException, KException {
        Params params = new Params("audio.editAlbum");
        params.put("title", title);
        params.put("album_id", album_id);
        JSONObject root = sendRequest(params);
        return root.optInt("response");
    }
    
    //http://vk.com/dev/audio.deleteAlbum
    public Integer deleteAudioAlbum(long album_id) throws IOException, JSONException, KException {
        Params params = new Params("audio.deleteAlbum");
        params.put("album_id", album_id);
        JSONObject root = sendRequest(params);
        return root.optInt("response");
    }
    
    //http://vk.com/dev/audio.moveToAlbum
    public Integer moveToAudioAlbum(long album_id,long audioAids) throws IOException, JSONException, KException {
        Params params = new Params("audio.moveToAlbum");
        params.put("album_id", String.valueOf(album_id));
        params.put("album_ids",String.valueOf(audioAids));
        JSONObject root = sendRequest(params);
        return root.optInt("response");
    }
}