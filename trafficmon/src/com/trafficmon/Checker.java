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
        for (ZoneBoundaryCrossing crossing : crossings.subList(1, crossings.size())) {
            if ((crossing.timestamp() < lastEvent.timestamp()) || (crossing instanceof EntryEvent && lastEvent instanceof EntryEvent) || (crossing instanceof ExitEvent && lastEvent instanceof ExitEvent)) {
                return false;
            }
            lastEvent = crossing;
        }
        return true;
    }
}