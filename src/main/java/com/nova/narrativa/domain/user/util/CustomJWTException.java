package com.nova.narrativa.domain.user.util;

public class CustomJWTException extends RuntimeException{

    public  CustomJWTException(String msg){
        super(msg);
    }
}