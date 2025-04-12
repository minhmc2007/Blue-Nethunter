package com.offsec.nethunter.exception;

public class AudioStoppedException extends RuntimeException {
    public AudioStoppedException() {
    }

    public AudioStoppedException(String message) {
        super(message);
    }

    public AudioStoppedException(String message, Throwable cause) {
        super(message, cause);
    }

    public AudioStoppedException(Throwable cause) {
        super(cause);
    }
}
