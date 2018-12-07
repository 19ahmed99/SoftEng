package com.trafficmon;

import java.util.*;

public class CongestionChargeSystem implements CongestionChargeSystemInterface {

    private final List<ZoneBoundaryCrossing> eventLog = new ArrayList<>();
    private final CalculatorInterface calculator;
    private final CheckerInterface checker;
    private final Map<VehicleInterface, List<ZoneBoundaryCrossing>> crossingsByVehicle = new HashMap<>(); //moved to the top

    CongestionChargeSystem() {
        this.checker = new Checker();
        this.calculator = new Calculator(checker);
    }

    CongestionChargeSystem(CalculatorInterface calculator, CheckerInterface checker) {
        // Constructor that takes an operations team (for testing)
        this.calculator = calculator;
        this.checker = checker;

    }

    public void vehicleEnteringZone(VehicleInterface vehicle) {
        // Vehicle entry
        EntryEvent new_entry = new EntryEvent(vehicle);
        eventLog.add(new_entry);
        if (!crossingsByVehicle.containsKey(vehicle)) {
            crossingsByVehicle.put(vehicle, new ArrayList<>());
        }
        crossingsByVehicle.get(vehicle).add(new_entry);
    }

    public void vehicleLeavingZone(VehicleInterface vehicle) {
        // Vehicle exit
        if (checker.previouslyRegistered(vehicle, eventLog)){
            ExitEvent new_exit = new ExitEvent(vehicle);
            eventLog.add(new_exit);
            crossingsByVehicle.get(vehicle).add(new_exit);
        }
    }

    public void calculateCharges() {
        // Method to calculate charges (calls the Calculator)
        calculator.calculateCharges(crossingsByVehicle);
    }


    // ----- Test Method -----

    public List<ZoneBoundaryCrossing> getEventLog() {
        return eventLog;
    }

    public Map<VehicleInterface, List<ZoneBoundaryCrossing>> getHashMap(){
        return crossingsByVehicle;
    }
}
