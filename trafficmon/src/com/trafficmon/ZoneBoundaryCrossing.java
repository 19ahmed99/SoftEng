package com.trafficmon;

import org.joda.time.DateTimeFieldType;
import org.joda.time.Instant;

public abstract class ZoneBoundaryCrossing {

    private final Vehicle vehicle;
    private int time;

    ZoneBoundaryCrossing(Vehicle vehicle) {
        this.vehicle = vehicle;
        Instant instant = new Instant();
        this.time = instant.get(DateTimeFieldType.secondOfDay());
    }

    public Vehicle getVehicle() {
        return vehicle;
    }

    public int timestamp() {
        return time;
    }

    // ----- Test Methods -----

    public void setTimeStamp(int time){
        this.time = time;
    }
}
