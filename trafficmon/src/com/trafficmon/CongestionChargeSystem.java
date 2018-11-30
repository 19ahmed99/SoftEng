package com.trafficmon;

import java.util.*;

public class CongestionChargeSystem implements main_system {

    private final List<ZoneBoundaryCrossing> eventLog = new ArrayList<>();
    private final Calculator calculator;
    private final Checker checker;
    private PenaltiesService operationsTeam;

    CongestionChargeSystem() {
        // Main constructor
        this.operationsTeam = OperationsTeam.getInstance();
        this.checker = new Checker();
        this.calculator = new Calculator(operationsTeam);
    }
    CongestionChargeSystem(PenaltiesService operationsTeam) {
        // Constructor that takes an operations team (for testing)
        this.operationsTeam = operationsTeam;
        this.checker = new Checker();
        this.calculator = new Calculator(operationsTeam);
    }

    public void vehicleEnteringZone(Vehicle vehicle) {
        // Vehicle entry

        eventLog.add(new EntryEvent(vehicle));
    }

    public void vehicleLeavingZone(Vehicle vehicle) {
        // // Vehicle exit

        if (checker.previouslyRegistered(vehicle, eventLog)){
            eventLog.add(new ExitEvent(vehicle));
        }
    }

    public void calculateCharges() {
        // Method to calculate charges (calls the Calculator)

        calculator.calculateCharges(generateHashMap());
    }

    private Map<Vehicle, List<ZoneBoundaryCrossing>> generateHashMap() {
        // Method to create a hash map with events per vehicle

        Map<Vehicle, List<ZoneBoundaryCrossing>> crossingsByVehicle = new HashMap<>();
        // Hash map: (key,value) = (Vehicle object, array list of the events (entry/exit)

        // Go through the event log
        //    - If the vehicle is already in, just add the event to the array list
        //    - If not, then create an entry with the vehicle, then add the event to the array list
        for (ZoneBoundaryCrossing crossing : eventLog) {
            if (!crossingsByVehicle.containsKey(crossing.getVehicle())) {
                crossingsByVehicle.put(crossing.getVehicle(), new ArrayList<>());
            }
            crossingsByVehicle.get(crossing.getVehicle()).add(crossing);
        }

        return crossingsByVehicle;
    }

    // ----- Test Method -----

    public List<ZoneBoundaryCrossing> getEventLog() {
        // Method to get the event log

        return eventLog;
    }
}
