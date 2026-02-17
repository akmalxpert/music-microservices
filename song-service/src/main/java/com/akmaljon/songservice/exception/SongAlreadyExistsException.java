package com.akmaljon.songservice.exception;

public class SongAlreadyExistsException extends RuntimeException {

    public SongAlreadyExistsException(Long id) {
        super("Metadata for resource ID=" + id + " already exists");
    }
}

