package com.trafficmon;

import java.math.BigDecimal;
import java.util.*;

public class CongestionChargeSystem {



    private static final BigDecimal CHARGE_RATE_POUNDS_PER_MINUTE = new BigDecimal(0.05);

    private final List<ZoneBoundaryCrossing> eventLog = new ArrayList<>();
    private final Calculator calculator = new Calculator(this);
    private final Checker checker = new Checker(this);

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
        if (checker.previouslyRegistered(vehicle)) {
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


    /*private boolean previouslyRegistered(Vehicle vehicle) {
        return checker.previouslyRegistered(vehicle);
    }*/

    public PenaltiesService getOperationsTeam() {
        return operationsTeam;
    }

    public List<ZoneBoundaryCrossing> getEventLog() {
        return eventLog;
    }

    public static BigDecimal getChargeRatePoundsPerMinute() {
        return CHARGE_RATE_POUNDS_PER_MINUTE;
    }


}
