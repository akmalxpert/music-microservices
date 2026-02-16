package com.akmaljon.resourceservice.exception;

public class InvalidAudioDataException extends RuntimeException {

    public InvalidAudioDataException(String message) {
        super(message);
    }

    public InvalidAudioDataException(String message, Throwable cause) {
        super(message, cause);
    }
}

