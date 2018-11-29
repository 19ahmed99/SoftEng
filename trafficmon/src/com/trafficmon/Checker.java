package com.trafficmon;

import java.util.List;

public class Checker {

    public boolean previouslyRegistered(Vehicle vehicle, List<ZoneBoundaryCrossing> eventLog) {
        for (ZoneBoundaryCrossing crossing : eventLog) {
            if (crossing.getVehicle().equals(vehicle)) {
                return true;
            }
        }
        return false;
    }

    protected boolean checkOrderingOf(List<ZoneBoundaryCrossing> crossings) {
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