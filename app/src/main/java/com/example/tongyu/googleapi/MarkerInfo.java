package com.example.tongyu.googleapi;

/**
 * Created by tongyu on 11/1/17.
 */

import java.util.ArrayList;
import java.util.List;

/**
 * Created by tongyu on 10/6/17.
 */

//Implement Serializable class so that the object can be saved to file
public class MarkerInfo implements java.io.Serializable {
    private double lat, lon;
    private String path;

    MarkerInfo(double lat, double lon, String path) {
        this.lat = lat;
        this.lon = lon;
        this.path = path;
    }


    public double getLat()
    {
        return lat;
    }
    public double getLon()
    {
        return lon;
    }
    public String getPath()
    {
        return path;
    }

}
