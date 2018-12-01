package com.trafficmon;

import java.util.List;

public class Checker implements Checking{

    public boolean previouslyRegistered(Vehicle vehicle, List<ZoneBoundaryCrossing> eventLog) {
        // Checks if the vehicle is already in the event log

        for (ZoneBoundaryCrossing crossing : eventLog) {
            if (crossing.getVehicle().equals(vehicle)) {
                return true;
            }
        }
        return false;
    }

    public boolean checkOrderingOf(List<ZoneBoundaryCrossing> crossings) {
        // Checks the event log for these problems
        //   - Timestamps not ordered
        //   - Two entries in a row
        //   - Two exits in a row

        ZoneBoundaryCrossing lastEvent = crossings.get(0);
        List<ZoneBoundaryCrossing> crossings_after_first = crossings.subList(1, crossings.size());
        for (ZoneBoundaryCrossing crossing : crossings_after_first) {
            boolean timeStamp_Error = crossing.timestamp() < lastEvent.timestamp();
            boolean doubleSameEvent = (crossing.getClass() == lastEvent.getClass());
            if (timeStamp_Error || doubleSameEvent) {
                return false;
            }
            lastEvent = crossing;
        }
        return true;
    }
}