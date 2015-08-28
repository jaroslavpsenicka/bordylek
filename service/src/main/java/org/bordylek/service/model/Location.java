package org.bordylek.service.model;

import javax.validation.constraints.NotNull;

public class Location {

    private String id;

    @NotNull
    private String name;

    @NotNull
    private double lat;

    @NotNull
    private double lng;

    public Location() {
    }

    public Location(String name) {
        this.name = name;
    }

    public Location(String name, double lat, double lng) {
        this.name = name;
        this.lat = lat;
        this.lng = lng;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLng() {
        return lng;
    }

    public void setLng(double lng) {
        this.lng = lng;
    }
}
