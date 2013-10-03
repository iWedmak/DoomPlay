package com.perm.vkontakte.api;

@SuppressWarnings("serial")
public class KException extends Exception{
    KException(int code, String message, String url){
        super(message);
        error_code=code;
        this.url=url;
    }
    public final int error_code;
    public final String url;
    
    //for captcha
    public String captcha_img;
    public String captcha_sid;
}
