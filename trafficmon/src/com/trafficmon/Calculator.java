package com.trafficmon;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public class Calculator implements CalculatorInterface {
    private CheckerInterface checker;
    private PenaltiesService operationsTeam;
    private AccountsService accountsService;

    Calculator(CheckerInterface checker) {
        this.checker = checker;
        this.operationsTeam = OperationsTeam.getInstance();
        this.accountsService = RegisteredCustomerAccountsService.getInstance();
    }

    Calculator(CheckerInterface checker, PenaltiesService operationsTeam) {
        // Constructor for testing
        this(checker); // Constructor chaining
        this.operationsTeam = operationsTeam;
    }

    public void calculateCharges(Map<VehicleInterface, List<ZoneBoundaryCrossing>> crossingsByVehicle) {
        // Calculates the charge and charges each driver that interacted with the Zone for a day

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
        // Charges the account of each driver

        try {
            accountsService.accountFor((Vehicle) vehicle).deduct(charge);
        } catch (InsufficientCreditException | AccountNotRegisteredException ice) { // If the person has not enough credit or isn't registered
            operationsTeam.issuePenaltyNotice((Vehicle) vehicle, charge);
        }
    }

    public BigDecimal getCharge(List<ZoneBoundaryCrossing> crossings) {
        // Calculates the charge for a single vehicle
        /* PLEASE READ
        - From reading the brief, we were slightly confused as it mentioned that:
        "If you stay inside the zone for longer than 4 hours on a given day then you will be charged £12."
        However, beforehand in the same paragraph, it said:
        "If you leave and come back within 4 hours, you should not be charged twice."
        Which led us to believe that if you leave and come back after 4 hours, you should be charged twice.
        A scenario where this may be confusing is, if I enter for 2 minutes each time but the gap between
        the last exit and new entry is longer than 4 hours, then the user could be charged up to £32 (which would
        be foolish as if you stay inside for the whole day, the maximum charge would be £12).
        And so our implementation assumes that if the total time in is more than four hours, then the charge is £12.
        If an aforementioned scenario occurs, then you could be charged up to £32
        (e.g. Entering for a few seconds every four hours)
         */

        ZoneBoundaryCrossing lastEvent = crossings.get(0); // Get the first event (always an entry)
        int charge;
        int total_charge = 0;

        int two_pm = 14*60*60; // 14 hours in seconds
        int four_hours = 4*60*60; // 4 hours in seconds
        int interval;
        int timeIn = 0; // Counter for the time spent inside the zone

        charge = lastEvent.timestamp() < two_pm ? 6 : 4; // Creates the current charge according to time of entry

        // Go through the following events
        int size_of_crossings = crossings.size();
        List<ZoneBoundaryCrossing> crossings_sublist = crossings.subList(1, size_of_crossings);
        for (ZoneBoundaryCrossing crossing : crossings_sublist) {
            interval = crossing.timestamp() - lastEvent.timestamp(); // Get the interval between current and last event
            // If it's an Entry after >4h then add current charge to total and reset it
            if ((crossing instanceof  EntryEvent) && (interval > four_hours)) {
                total_charge += charge;
                charge = crossing.timestamp() < two_pm ? 6 : 4;
            }
            // If it's an Exit then add time spent in zone
            if (crossing instanceof ExitEvent) {
                  timeIn += interval; // Adding the time between the entry and exit to the timeIn
            }
            lastEvent = crossing;
            // If time in zone is >4h then the charge is 12
            if (timeIn > four_hours) {
                return new BigDecimal(12);
            }
        }

        // Add last current charge to total and return it
        total_charge += charge;
        return new BigDecimal(total_charge);
    }
}