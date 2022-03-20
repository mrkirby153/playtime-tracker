package com.mrkirby153.playtimetracker.repository;

public class PlaySession {

    private final String id;
    private final long start;
    private final long end;

    public PlaySession(String id, long start, long end) {
        this.id = id;
        this.start = start;
        this.end = end;
    }

    public String getId() {
        return id;
    }

    public long getStart() {
        return start;
    }

    public long getEnd() {
        return end;
    }

    public boolean isActive() {
        return this.end == -1;
    }

    public long duration() {
        long end = isActive() ? System.currentTimeMillis() : this.end;
        return end - this.start;
    }
}
