package com.trafficmon;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Calculator implements CalculatorInterface {
    private Checker checker = new Checker();
    private PenaltiesService operationsTeam;
    private AccountsService accountsService;

    Calculator(PenaltiesService operationsTeam) {
        // Constructor that takes the operations team
        this.operationsTeam = operationsTeam;
        this.accountsService = RegisteredCustomerAccountsService.getInstance();
    }

    public void calculateCharges(Map<Vehicle, List<ZoneBoundaryCrossing>> crossingsByVehicle) {
        // Main method to calculate charges for each vehicle

        final Set<Map.Entry<Vehicle, List<ZoneBoundaryCrossing>>> entries_in_hashMap = crossingsByVehicle.entrySet();
        for (Map.Entry<Vehicle, List<ZoneBoundaryCrossing>> vehicleCrossings : entries_in_hashMap) {
            // Loop through the hash map

            // Sets "vehicle" to the key and "crossings" to the value
            Vehicle vehicle = vehicleCrossings.getKey(); // This gets the current vehicle you are on
            List<ZoneBoundaryCrossing> crossings = vehicleCrossings.getValue();

            boolean ordering_correct = checker.checkOrderingOf(crossings);
            if (!ordering_correct) {
                operationsTeam.triggerInvestigationInto(vehicle);
            } else {
                BigDecimal charge = getCharge(crossings); // Get the charge for this vehicle
                try {
                    accountsService.accountFor(vehicle).deduct(charge);
                } catch (InsufficientCreditException | AccountNotRegisteredException ice) { // If the person has not enough credit or isn't registered
                    operationsTeam.issuePenaltyNotice(vehicle, charge);
                }
            }
        }
    }

    private BigDecimal getCharge(List<ZoneBoundaryCrossing> crossings) {
        // Method to get the charge for a vehicle

        BigDecimal charge; // Value of the charge
        ZoneBoundaryCrossing lastEvent = crossings.get(0); // Get the first event (always an Entry)
        int timeIn = 0; // Counter for the time inside the zone


        if (lastEvent.timestamp() < 50400) { // If the first entry is before 2pm
            charge = new BigDecimal(6);
        } else {
            charge = new BigDecimal(4);
        }

        // Go through the events, adding the time spent is zone to timeIn
        List<ZoneBoundaryCrossing> crossings_sublist = crossings.subList(1, crossings.size());
        for (ZoneBoundaryCrossing crossing : crossings_sublist) {
            if (crossing instanceof ExitEvent) {
                  timeIn += crossing.timestamp()-lastEvent.timestamp(); // Adding the time between the entry and exit to the timeIn
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

    public BigDecimal getCalculatedCharge(ZoneBoundaryCrossing entry, ZoneBoundaryCrossing exit){
        // A test method to calculate the charge for an entry/exit

        ArrayList<ZoneBoundaryCrossing> crossings = new ArrayList<>();
        crossings.add(entry);
        crossings.add(exit);

        return getCharge(crossings);
    }
}