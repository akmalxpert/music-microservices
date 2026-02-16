package com.akmaljon.songservice.exception;

public class SongNotFoundException extends RuntimeException {

    public SongNotFoundException(Long id) {
        super("Song metadata for ID=" + id + " not found");
    }
}

