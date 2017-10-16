package com.napas.landmarkremark.model;

import android.os.Parcel;
import android.os.Parcelable;

public class Landmark implements Parcelable {

    private String name;
    private String Address;
    private Double latitude;
    private Double longitude;
    private String createdBy;
    private String note;

    public Landmark() {
    }

    public Landmark(String name, String address, Double latitude, Double longitude, String createdBy, String note) {
        this.name = name;
        Address = address;
        this.latitude = latitude;
        this.longitude = longitude;
        this.createdBy = createdBy;
        this.note = note;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return Address;
    }

    public void setAddress(String address) {
        Address = address;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.name);
        dest.writeString(this.Address);
        dest.writeValue(this.latitude);
        dest.writeValue(this.longitude);
        dest.writeString(this.createdBy);
        dest.writeString(this.note);
    }

    protected Landmark(Parcel in) {
        this.name = in.readString();
        this.Address = in.readString();
        this.latitude = (Double) in.readValue(Double.class.getClassLoader());
        this.longitude = (Double) in.readValue(Double.class.getClassLoader());
        this.createdBy = in.readString();
        this.note = in.readString();
    }

    public static final Parcelable.Creator<Landmark> CREATOR = new Parcelable.Creator<Landmark>() {
        @Override
        public Landmark createFromParcel(Parcel source) {
            return new Landmark(source);
        }

        @Override
        public Landmark[] newArray(int size) {
            return new Landmark[size];
        }
    };
}
