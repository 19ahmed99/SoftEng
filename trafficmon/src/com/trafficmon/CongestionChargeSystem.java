package com.trafficmon;

import java.math.BigDecimal;
import java.util.*;

public class CongestionChargeSystem {

    private static final BigDecimal CHARGE_RATE_POUNDS_PER_MINUTE = new BigDecimal(0.05);

    private final List<ZoneBoundaryCrossing> eventLog = new ArrayList<>();

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

        Map<Vehicle, List<ZoneBoundaryCrossing>> crossingsByVehicle = new HashMap<>();
        //hashmap: (key,value) = (Vehicle object, arraylist of their entry and exits , starts off empty

        for (ZoneBoundaryCrossing crossing : eventLog) {
            if (!crossingsByVehicle.containsKey(crossing.getVehicle())) {
                crossingsByVehicle.put(crossing.getVehicle(), new ArrayList<>());
            }
            crossingsByVehicle.get(crossing.getVehicle()).add(crossing);
        }
        //loop through the eventlog and if the crossing was done by a car in the hashmap then just add it to its arraylist
        //if its not already in the hashmap, create the new entry and add the event to its arraylist

        for (Map.Entry<Vehicle, List<ZoneBoundaryCrossing>> vehicleCrossings : crossingsByVehicle.entrySet()) {
            Vehicle vehicle = vehicleCrossings.getKey();
            List<ZoneBoundaryCrossing> crossings = vehicleCrossings.getValue();
            //loop through the hashmap and set "vehicle" to the key and "crossings" to the arraylist

            if (!checkOrderingOf(crossings)) {
                operationsTeam.triggerInvestigationInto(vehicle); //if ordering is messed up, then investigate
            } else {
                BigDecimal charge = calculateChargeForTimeInZone(crossings); //calculate the charge
                try {
                    RegisteredCustomerAccountsService.getInstance().accountFor(vehicle).deduct(charge);
                } catch (InsufficientCreditException | AccountNotRegisteredException ice) {
                    operationsTeam.issuePenaltyNotice(vehicle, charge);}
            }
        }
    }

    private BigDecimal calculateChargeForTimeInZone(List<ZoneBoundaryCrossing> crossings) {

        BigDecimal charge = new BigDecimal(0);

        ZoneBoundaryCrossing lastEvent = crossings.get(0);

        for (ZoneBoundaryCrossing crossing : crossings.subList(1, crossings.size())) {

            if (crossing instanceof ExitEvent) {
                charge = charge.add(new BigDecimal(minutesBetween(lastEvent.timestamp(), crossing.timestamp())).multiply(CHARGE_RATE_POUNDS_PER_MINUTE));
            }
            lastEvent = crossing;
        }
        return charge;
    }

    private boolean previouslyRegistered(Vehicle vehicle) {
        for (ZoneBoundaryCrossing crossing : eventLog) {
            if (crossing.getVehicle().equals(vehicle)) {
                return true;
            }
        }
        return false;
    }

    private boolean checkOrderingOf(List<ZoneBoundaryCrossing> crossings) {

        ZoneBoundaryCrossing lastEvent = crossings.get(0);
        for (ZoneBoundaryCrossing crossing : crossings.subList(1, crossings.size())) {
            if ((crossing.timestamp() < lastEvent.timestamp()) || (crossing instanceof EntryEvent && lastEvent instanceof EntryEvent) || (crossing instanceof ExitEvent && lastEvent instanceof ExitEvent)) {
                return false;
            }
            lastEvent = crossing;
        }
        return true;
    }

    private int minutesBetween(long startTimeMs, long endTimeMs) {
        return (int) Math.ceil((endTimeMs - startTimeMs) / (1000.0 * 60.0));
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
        return calculateChargeForTimeInZone(crossings);
    } //my own method

    public boolean checkIfRegistered(Vehicle vehicle) {
        return previouslyRegistered(vehicle);
    }//my own method
}
