package com.trafficmon;

import java.util.List;

public class Checker {
    private final CongestionChargeSystem congestionChargeSystem;

    public Checker(CongestionChargeSystem congestionChargeSystem) {
        this.congestionChargeSystem = congestionChargeSystem;
    }

    boolean previouslyRegistered(Vehicle vehicle) {
        for (ZoneBoundaryCrossing crossing : congestionChargeSystem.getEventLog()) {
            if (crossing.getVehicle().equals(vehicle)) {
                return true;
            }
        }
        return false;
    }

    public boolean checkIfRegistered(Vehicle vehicle) {
        return previouslyRegistered(vehicle);
    }//my own method}

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