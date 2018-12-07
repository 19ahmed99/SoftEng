package com.trafficmon;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public class Calculator implements CalculatorInterface {
    private CheckerInterface checker;
    private PenaltiesService operationsTeam;
    private AccountsService accountsService;

    Calculator(CheckerInterface checker) {
        // Constructor that takes the operations team
        this.checker = checker;
        this.operationsTeam = OperationsTeam.getInstance();
        this.accountsService = RegisteredCustomerAccountsService.getInstance();
    }

    Calculator(CheckerInterface checker, PenaltiesService operationsTeam) {
        this(checker); //constructor chaining
        this.operationsTeam = operationsTeam;
    }

    public void calculateCharges(Map<Vehicle, List<ZoneBoundaryCrossing>> crossingsByVehicle) {
        for (Map.Entry<Vehicle, List<ZoneBoundaryCrossing>> vehicleCrossings : crossingsByVehicle.entrySet()) {
            Vehicle vehicle = vehicleCrossings.getKey();
            List<ZoneBoundaryCrossing> crossings = vehicleCrossings.getValue();
            boolean ordering_correct = checker.checkOrderingOf(crossings);
            if (ordering_correct) {
                BigDecimal charge = getCharge(crossings); // Get the charge for this vehicle
                charge_account(vehicle, charge);
            } else {
                operationsTeam.triggerInvestigationInto(vehicle);

            }
        }
    }

    public void charge_account(Vehicle vehicle, BigDecimal charge)  {
        try {
            accountsService.accountFor(vehicle).deduct(charge);
        } catch (InsufficientCreditException | AccountNotRegisteredException ice) { // If the person has not enough credit or isn't registered
            operationsTeam.issuePenaltyNotice(vehicle, charge);
        }
    }

    private BigDecimal getCharge(List<ZoneBoundaryCrossing> crossings) {
        // Method to get the charge for a vehicle

        int charge;
        int total_charge = 0;
        ZoneBoundaryCrossing lastEvent = crossings.get(0); // Get the first event (always an Entry)
        int timeIn = 0; // Counter for the time inside the zone

        int two_pm = 14*60*60; //14 hours in seconds
        int four_hours = 4*60*60;

        charge = lastEvent.timestamp() < two_pm ? 6 : 4;

        // Go through the events, adding the time spent is zone to timeIn
        int size_of_crossings = crossings.size();
        List<ZoneBoundaryCrossing> crossings_sublist = crossings.subList(1, size_of_crossings);
        for (ZoneBoundaryCrossing crossing : crossings_sublist) {
            if ((crossing instanceof  EntryEvent) && (crossing.timestamp() - lastEvent.timestamp() > four_hours)) {
                total_charge += charge;
                charge = crossing.timestamp() < two_pm ? 6 : 4;
            }
            if (crossing instanceof ExitEvent) {
                  timeIn += crossing.timestamp()-lastEvent.timestamp(); // Adding the time between the entry and exit to the timeIn
            }
            lastEvent = crossing;
            if (timeIn > four_hours) {
                charge = 0;
                total_charge = 12;
                break;
            }
        }

        total_charge += charge;
        return new BigDecimal(total_charge);
    }

    // ----- Test Methods -----

    public BigDecimal getCalculatedCharge(List<ZoneBoundaryCrossing> crossings){
        // A test method to calculate the charge for some entries/exits for one car
        return getCharge(crossings);
    }
}