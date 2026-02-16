package com.akmaljon.songservice.exception;

public class SongAlreadyExistsException extends RuntimeException {

    public SongAlreadyExistsException(Long id) {
        super("Song with ID=" + id + " already exists");
    }
}

