package com.trafficmon;

import java.math.BigDecimal;
import java.util.*;

public class CongestionChargeSystem {


    private final List<ZoneBoundaryCrossing> eventLog = new ArrayList<>();
    private final Calculator calculator;
    private final Checker checker;
    private PenaltiesService operationsTeam;


    CongestionChargeSystem() {
        this.operationsTeam = OperationsTeam.getInstance();
        this.checker = new Checker();
        this.calculator = new Calculator(checker,operationsTeam);
    }
    CongestionChargeSystem(PenaltiesService operationsTeam) {
        this.operationsTeam = operationsTeam;
        this.checker = new Checker();
        this.calculator = new Calculator(checker,operationsTeam);
    }

    public void vehicleEnteringZone(Vehicle vehicle) { //Vehicle Entry
        eventLog.add(new EntryEvent(vehicle));
    }

    public void vehicleLeavingZone(Vehicle vehicle) { // Vehicle Exit
        if (checker.previouslyRegistered(vehicle, eventLog)){
            eventLog.add(new ExitEvent(vehicle));
        }
    }

    public void calculateCharges() {
        calculator.calculateCharges(generateHashMap());
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

    public List<ZoneBoundaryCrossing> getEventLog() {
        return eventLog;
    }



}
