package com.nova.narrativa.common.exception;

public class NoMusicFileFoundException extends RuntimeException {

    public NoMusicFileFoundException(String genre) {
        super("해당 장르의 음악 파일이 없습니다: " + genre);
    }
}
