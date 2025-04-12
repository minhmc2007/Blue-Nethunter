package com.offsec.nethunter;

public enum AudioPlayState {
    STOPPED(false),
    STARTING(true),
    BUFFERING(true),
    STARTED(true),
    STOPPING(false);

    private final boolean isActive;

    AudioPlayState(boolean active) {
        isActive = active;
    }

    public boolean isActive() {
        return isActive;
    }
}
