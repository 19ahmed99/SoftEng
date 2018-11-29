package com.trafficmon;

import org.joda.time.DateTimeFieldType;
import org.joda.time.Instant;

public abstract class ZoneBoundaryCrossing {

    private final Vehicle vehicle;
    private int time;

    ZoneBoundaryCrossing(Vehicle vehicle) {
        // Constructor that takes a vehicle and gets the actual time
        this.vehicle = vehicle;
        // Gets the time in the day (in terms of seconds)
        Instant instant = new Instant();
        this.time = instant.get(DateTimeFieldType.secondOfDay());
    }

    public Vehicle getVehicle() {
        // Method to get the object's vehicle
        return vehicle;
    }

    public int timestamp() {
        // Method to get the object's time
        return time;
    }

    // ----- Test Methods -----

    public void setTimeStamp(int time){
        // A test method to set the timestamp
        this.time = time;
    }
}
