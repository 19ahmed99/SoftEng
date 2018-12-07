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

    public void calculateCharges(Map<VehicleInterface, List<ZoneBoundaryCrossing>> crossingsByVehicle) {
        for (Map.Entry<VehicleInterface, List<ZoneBoundaryCrossing>> vehicleCrossings : crossingsByVehicle.entrySet()) {
            VehicleInterface vehicle = vehicleCrossings.getKey();
            List<ZoneBoundaryCrossing> crossings = vehicleCrossings.getValue();
            boolean ordering_correct = checker.checkOrderingOf(crossings);
            if (ordering_correct) {
                BigDecimal charge = getCharge(crossings); // Get the charge for this vehicle
                charge_account(vehicle, charge);
            } else {
                operationsTeam.triggerInvestigationInto((Vehicle) vehicle); // Object reference type casting

            }
        }
    }

    public void charge_account(VehicleInterface vehicle, BigDecimal charge)  {
        try {
            accountsService.accountFor((Vehicle) vehicle).deduct(charge);
        } catch (InsufficientCreditException | AccountNotRegisteredException ice) { // If the person has not enough credit or isn't registered
            operationsTeam.issuePenaltyNotice((Vehicle) vehicle, charge);
        }
    }

    private BigDecimal getCharge(List<ZoneBoundaryCrossing> crossings) {
        /* PLEASE READ
        - From reading the brief, we were slightly confused as it mentioned that:
        "If you stay inside the zone for longer than 4 hours on a given day then you will be charged £12."
        however beforehand in the same paragraph, it said:
        "If you leave and come back within 4 hours, you should not be charged twice." which led us to believe
        that if you leave and come back after 4 hours, you should be charged twice. A scenario where this may be confusing is
        if i enter for 2 minutes each time however the gap between the last exit and new entry  is longer than 4 hours then
        the user could be charged up to £32 which would be foolish as if you stay inside for the whole day, the maximum charge would be £12.
        And so our implementation assumes that if the total time in is more than four hours, then the charge is £12.
        If an aforementioned scenario occurs, then you could be charged up to £32 (Entering for a few seconds every four hours)
         */

        int charge;
        int total_charge = 0;
        ZoneBoundaryCrossing lastEvent = crossings.get(0); // Get the first event (always an Entry)
        int timeIn = 0; // Counter for the time inside the zone

        int two_pm = 14*60*60; //14 hours in seconds
        int four_hours = 4*60*60;
        int interval;

        charge = lastEvent.timestamp() < two_pm ? 6 : 4;

        // Go through the events, adding the time spent is zone to timeIn
        int size_of_crossings = crossings.size();
        List<ZoneBoundaryCrossing> crossings_sublist = crossings.subList(1, size_of_crossings);
        for (ZoneBoundaryCrossing crossing : crossings_sublist) {
            interval = crossing.timestamp() - lastEvent.timestamp();
            if ((crossing instanceof  EntryEvent) && (interval > four_hours)) {
                total_charge += charge;
                charge = crossing.timestamp() < two_pm ? 6 : 4;
            }
            if (crossing instanceof ExitEvent) {
                  timeIn += interval; // Adding the time between the entry and exit to the timeIn
            }
            lastEvent = crossing;
            if (timeIn > four_hours) {
                return new BigDecimal(12);
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