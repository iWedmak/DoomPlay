package com.perm.vkontakte.api;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;

//Fields are optional. Should be null if not populated
public class User implements Serializable {
    private static final long serialVersionUID = 1L;
    public long uid;
    public String first_name;
    public String last_name;
    private String nickname;
    private String photo;//the same as photo_rec
    private String photo_medium;
    private String photo_medium_rec;
    private String status;
    
    
    public static User parse(JSONObject o) throws JSONException {
        User u = new User();
        u.uid = Long.parseLong(o.getString("uid"));
        if(!o.isNull("first_name"))
            u.first_name = Api.unescape(o.getString("first_name"));
        if(!o.isNull("last_name"))
            u.last_name = Api.unescape(o.getString("last_name"));
        if(!o.isNull("nickname"))
            u.nickname = Api.unescape(o.optString("nickname"));
        if(!o.isNull("photo"))
            u.photo = o.optString("photo");
        if(!o.isNull("photo_medium"))
            u.photo_medium = o.optString("photo_medium");
        if(!o.isNull("photo_medium_rec"))
            u.photo_medium_rec = o.optString("photo_medium_rec");
        if(!o.isNull("activity"))
            u.status = Api.unescape(o.optString("activity"));


        return u;
    }

    
    private static User parseFromGetByPhones(JSONObject o) throws JSONException {
        User u = new User();
        u.uid = o.getLong("uid");
        u.first_name = Api.unescape(o.optString("first_name"));
        u.last_name = Api.unescape(o.optString("last_name"));
        return u;
    }
    
    public static ArrayList<User> parseUsers(JSONArray array) throws JSONException {
        ArrayList<User> users=new ArrayList<User>();
        //it may be null if no users returned
        //no users may be returned if we request users that are already removed
        if(array==null)
            return users;
        int category_count=array.length();
        for(int i=0; i<category_count; ++i){
            if(array.get(i)==null || (!(array.get(i) instanceof JSONObject)))
                continue;
            JSONObject o = (JSONObject)array.get(i);
            User u = User.parse(o);
            users.add(u);
        }
        return users;
    }
    
    public static ArrayList<User> parseUsersForGetByPhones(JSONArray array) throws JSONException {
        ArrayList<User> users=new ArrayList<User>();
        //it may be null if no users returned
        //no users may be returned if we request users that are already removed
        if(array==null)
            return users;
        int category_count=array.length();
        for(int i=0; i<category_count; ++i){
            if(array.get(i)==null || (!(array.get(i) instanceof JSONObject)))
                continue;
            JSONObject o = (JSONObject)array.get(i);
            User u = User.parseFromGetByPhones(o);
            users.add(u);
        }
        return users;
    }
    
    //TODO why it duplicates parseAudiosCursor() method
    public static User parseFromFave(JSONObject jprofile) throws JSONException {
        User m = new User();
        m.uid = Long.parseLong(jprofile.getString("uid"));
        m.first_name = Api.unescape(jprofile.optString("first_name"));
        m.last_name = Api.unescape(jprofile.optString("last_name"));
        m.photo_medium_rec = jprofile.optString("photo_medium_rec");
        return m;
    }
}
