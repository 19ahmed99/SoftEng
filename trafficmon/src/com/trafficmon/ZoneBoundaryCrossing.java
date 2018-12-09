package com.trafficmon;

import org.joda.time.DateTimeFieldType;
import org.joda.time.Instant;

public abstract class ZoneBoundaryCrossing {

    private final VehicleInterface vehicle;
    private int time;

    ZoneBoundaryCrossing(VehicleInterface vehicle) {
        // Constructor that takes a Vehicle and gets the actual time
        this.vehicle = vehicle;
        // Gets the time of the day (in terms of seconds)
        Instant instant = new Instant();
        this.time = instant.get(DateTimeFieldType.secondOfDay());
    }

    public VehicleInterface getVehicle() {
        // Method to get the object's vehicle
        return vehicle;
    }

    public int timestamp() {
        // Method to get the object's time
        return time;
    }

    // ----- Test Methods -----

    public void setTimeStamp(int time){
        // A method to set the timestamp
        this.time = time;
    }
}
