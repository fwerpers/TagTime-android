package com.fwerpers.tagtime;

/**
 * Created by FWerpers on 02/09/17.
 */

public class Ping {
    private int id;
    private long time;
    private String notes;
    private int interval;

    public Ping(long time, String notes, int interval) {
        this.time = time;
        this.notes = notes;
        this.interval = interval;
    }

    public void setId(int id) {
        this.id = id;
    }
}
