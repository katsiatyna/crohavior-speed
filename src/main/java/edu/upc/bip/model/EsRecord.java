package edu.upc.bip.model;

import java.io.Serializable;

/**
 * Created by osboxes on 24/11/16.
 */
public class EsRecord implements Serializable{
    private String type;
    private int rdd;
    private long ts ;
    private double a;
    private double o;
    private int c;

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

    public double getA() {
        return a;
    }

    public void setA(double a) {
        this.a = a;
    }

    public double getO() {
        return o;
    }

    public void setO(double o) {
        this.o = o;
    }

    public int getC() {
        return c;
    }

    public void setC(int c) {
        this.c = c;
    }
}
