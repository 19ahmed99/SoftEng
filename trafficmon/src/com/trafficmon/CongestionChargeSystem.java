package com.trafficmon;

import java.math.BigDecimal;
import java.util.*;

public class CongestionChargeSystem {

    private static final BigDecimal CHARGE_RATE_POUNDS_PER_MINUTE = new BigDecimal(0.05);

    private final List<ZoneBoundaryCrossing> eventLog = new ArrayList<>();
    private final Calculator calculator = new Calculator(this);

    private PenaltiesService operationsTeam;

    CongestionChargeSystem() {
        this.operationsTeam = OperationsTeam.getInstance();
    }
    CongestionChargeSystem(PenaltiesService operationsTeam) {
        this.operationsTeam = operationsTeam;
    }

    public void vehicleEnteringZone(Vehicle vehicle) { //Vehicle Entry
        eventLog.add(new EntryEvent(vehicle));
    }

    public void vehicleLeavingZone(Vehicle vehicle) { // Vehicle Exit
        if (previouslyRegistered(vehicle)) {
            eventLog.add(new ExitEvent(vehicle));
        }
    }

    public void calculateCharges() {
        calculator.calculateCharges(generateHashMap(), CHARGE_RATE_POUNDS_PER_MINUTE);
    }

    private Map<Vehicle, List<ZoneBoundaryCrossing>> generateHashMap() { //we made this method
        Map<Vehicle, List<ZoneBoundaryCrossing>> crossingsByVehicle = new HashMap<Vehicle, List<ZoneBoundaryCrossing>>();
        //hashmap: (key,value) = (Vehicle object, arraylist of their entry and exits , starts off empty

        for (ZoneBoundaryCrossing crossing : eventLog) {
            if (!crossingsByVehicle.containsKey(crossing.getVehicle())) {
                crossingsByVehicle.put(crossing.getVehicle(), new ArrayList<ZoneBoundaryCrossing>());
            }
            crossingsByVehicle.get(crossing.getVehicle()).add(crossing);
        }
        //loop through the eventlog and if the crossing was done by a car in the hashmap then just add it to its arraylist
        //if its not already in the hashmap, create the new entry and add the event to its arraylist
        return crossingsByVehicle;
    }

    private boolean previouslyRegistered(Vehicle vehicle) {
        for (ZoneBoundaryCrossing crossing : eventLog) {
            if (crossing.getVehicle().equals(vehicle)) {
                return true;
            }
        }
        return false;
    }

    //notice, i made this protected so it can be accessed from the calculator
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

    // ----- Test Methods -----

    public boolean checkOrderingOfCrossings(List<ZoneBoundaryCrossing> crossings){
        return checkOrderingOf(crossings);
    }

    public int getSizeofEventLog() {
        return eventLog.size();
    } //my own method

    public ZoneBoundaryCrossing getEventLogEntries(int i) {
        return eventLog.get(i);
    } //my own method

    public BigDecimal getCalculatedCharge(ZoneBoundaryCrossing entry, ZoneBoundaryCrossing exit) {
        ArrayList<ZoneBoundaryCrossing> crossings = new ArrayList<>();
        crossings.add(entry);
        crossings.add(exit);
        return calculator.calculateChargeForTimeInZone(crossings, CHARGE_RATE_POUNDS_PER_MINUTE);
    } //my own method

    public boolean checkIfRegistered(Vehicle vehicle) {
        return previouslyRegistered(vehicle);
    }//my own method

    public PenaltiesService getOperationsTeam() {
        return operationsTeam;
    }
}
