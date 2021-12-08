package com.ureye.utils.common;

import android.location.Location;

import androidx.annotation.NonNull;

import java.util.Calendar;
import java.util.Objects;

public class LocationsModel {
    public double latitude, longitude;
    public long timeStamp;

    public LocationsModel(Location location) {
        this.latitude = location.getLatitude();
        this.longitude = location.getLongitude();
        this.timeStamp = Calendar.getInstance().getTimeInMillis();
    }

    @NonNull
    @Override
    public String toString() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(timeStamp);
        return "Latitude=" + latitude +
                ", Longitude=" + longitude +
                "\n at " + calendar.getTime().toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof LocationsModel)) return false;
        LocationsModel that = (LocationsModel) o;
        return Double.compare(that.latitude, latitude) == 0 && Double.compare(that.longitude, longitude) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(latitude, longitude, timeStamp);
    }

}
