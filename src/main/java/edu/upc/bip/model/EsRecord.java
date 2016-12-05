package edu.upc.bip.model;

import java.io.Serializable;

/**
 * Created by osboxes on 24/11/16.
 */
public class EsRecord implements Serializable{
    private String type;
    private int rdd;
    private long ts ;
    private double latitude;
    private double longitude;
    private int count;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getRdd() {
        return rdd;
    }

    public void setRdd(int rdd) {
        this.rdd = rdd;
    }

    public long getTs() {
        return ts;
    }

    public void setTs(long ts) {
        this.ts = ts;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }
}
