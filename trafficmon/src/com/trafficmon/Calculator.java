package com.trafficmon;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Calculator {
    private Checker checker = new Checker();
    private PenaltiesService operationsTeam;

    Calculator(PenaltiesService operationsTeam) {
        // Constructor that takes the operations team
        this.operationsTeam = operationsTeam;
    }

    void calculateCharges(Map<Vehicle, List<ZoneBoundaryCrossing>> crossingsByVehicle) {
        // Main method to calculate charges for each vehicle

        for (Map.Entry<Vehicle, List<ZoneBoundaryCrossing>> vehicleCrossings : crossingsByVehicle.entrySet()) {
            // Loop through the hash map

            // Sets "vehicle" to the key and "crossings" to the array list
            Vehicle vehicle = vehicleCrossings.getKey();
            List<ZoneBoundaryCrossing> crossings = vehicleCrossings.getValue();

            if (!checker.checkOrderingOf(crossings)) { // If ordering is messed up
                operationsTeam.triggerInvestigationInto(vehicle);
            } else {
                BigDecimal charge = getCharge(crossings); // Get the charge for this vehicle
                try {
                    RegisteredCustomerAccountsService.getInstance().accountFor(vehicle).deduct(charge);
                } catch (InsufficientCreditException | AccountNotRegisteredException ice) { // If the person has not enough credit or isn't registered
                    operationsTeam.issuePenaltyNotice(vehicle, charge);
                }
            }
        }
    }

    private BigDecimal getCharge(List<ZoneBoundaryCrossing> crossings) {
        // Method to get the charge for a vehicle

        BigDecimal charge;
        ZoneBoundaryCrossing lastEvent = crossings.get(0);
        int timeIn = 0;

        if (lastEvent.timestamp() < 50400) { // If the first entry is before 2pm
            charge = new BigDecimal(6);
        } else {
            charge = new BigDecimal(4);
        }
        // Go through the events, adding the time spent is zone to timeIn
        for (ZoneBoundaryCrossing crossing : crossings.subList(1, crossings.size())) {
            if (crossing instanceof ExitEvent) {
                  timeIn += crossing.timestamp()-lastEvent.timestamp();
            }
            if (timeIn > 14400) { // If timeIn is more than 4h
                charge = new BigDecimal(12);
                break;
            }
            lastEvent = crossing;
        }
        return charge;
    }

    // ----- Test Methods -----

    BigDecimal getCalculatedCharge(ZoneBoundaryCrossing entry, ZoneBoundaryCrossing exit){
        // A test method to calculate the charge for an entry/exit

        ArrayList<ZoneBoundaryCrossing> crossings = new ArrayList<>();
        crossings.add(entry);
        crossings.add(exit);

        return getCharge(crossings);
    }
}