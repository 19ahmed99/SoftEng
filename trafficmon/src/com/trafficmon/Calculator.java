package com.trafficmon;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

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

    public void charge_account(Vehicle vehicle, BigDecimal charge) {
        try {
            accountsService.accountFor(vehicle).deduct(charge);
        } catch (InsufficientCreditException | AccountNotRegisteredException ice) { // If the person has not enough credit or isn't registered
            operationsTeam.issuePenaltyNotice(vehicle, charge);
        }
    }

    private BigDecimal getCharge(List<ZoneBoundaryCrossing> crossings) {
        // Method to get the charge for a vehicle

        BigDecimal charge; // Value of the charge
        ZoneBoundaryCrossing lastEvent = crossings.get(0); // Get the first event (always an Entry)
        int timeIn = 0; // Counter for the time inside the zone

        charge = lastEvent.timestamp() < 50400 ? new BigDecimal(6) : new BigDecimal(4);

        // Go through the events, adding the time spent is zone to timeIn
        int size_of_crossings = crossings.size();
        List<ZoneBoundaryCrossing> crossings_sublist = crossings.subList(1, size_of_crossings);
        for (ZoneBoundaryCrossing crossing : crossings_sublist) {
            if (crossing instanceof ExitEvent) {
                  timeIn += crossing.timestamp()-lastEvent.timestamp(); // Adding the time between the entry and exit to the timeIn
            }
            lastEvent = crossing;
        }
       return (timeIn > 14400)? new BigDecimal(12) : charge;
    }

    // ----- Test Methods -----

    public BigDecimal getCalculatedCharge(List<ZoneBoundaryCrossing> crossings){
        // A test method to calculate the charge for some entries/exits for one car
        return getCharge(crossings);
    }
}