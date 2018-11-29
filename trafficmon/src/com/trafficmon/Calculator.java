package com.trafficmon;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Calculator {
    private static final BigDecimal CHARGE_RATE_POUNDS_PER_MINUTE = new BigDecimal(0.05);
    private Checker checker;
    private PenaltiesService operationsTeam;


    Calculator(Checker checker, PenaltiesService operationsTeam) {
        this.checker = checker;
        this.operationsTeam = operationsTeam;
    }

    public void calculateCharges(Map<Vehicle, List<ZoneBoundaryCrossing>> crossingsByVehicle) {

        for (Map.Entry<Vehicle, List<ZoneBoundaryCrossing>> vehicleCrossings : crossingsByVehicle.entrySet()) {
            Vehicle vehicle = vehicleCrossings.getKey();
            List<ZoneBoundaryCrossing> crossings = vehicleCrossings.getValue();
            //loop through the hashmap and set "vehicle" to the key and "crossings" to the arraylist

            if (!checker.checkOrderingOf(crossings)) {
                operationsTeam.triggerInvestigationInto(vehicle); //if ordering is messed up, then investigate
            } else {
                BigDecimal charge = calculateChargeForTimeInZone(crossings); //calculate the charge
                try {
                    RegisteredCustomerAccountsService.getInstance().accountFor(vehicle).deduct(charge);
                } catch (InsufficientCreditException | AccountNotRegisteredException ice) {
                    operationsTeam.issuePenaltyNotice(vehicle, charge);
                }
            }
        }
    }

    protected BigDecimal calculateChargeForTimeInZone(List<ZoneBoundaryCrossing> crossings) {

        BigDecimal charge = new BigDecimal(0);

        ZoneBoundaryCrossing lastEvent = crossings.get(0);

        for (ZoneBoundaryCrossing crossing : crossings.subList(1, crossings.size())) {

            if (crossing instanceof ExitEvent) {
                charge = charge.add(new BigDecimal(minutesBetween(lastEvent.timestamp(), crossing.timestamp())).multiply(CHARGE_RATE_POUNDS_PER_MINUTE));
            }
            lastEvent = crossing;
        }
        return charge;
    }

    private int minutesBetween(long startTimeMs, long endTimeMs) {
        return (int) Math.ceil((endTimeMs - startTimeMs) / (1000.0 * 60.0));
    }

    public BigDecimal getCalculatedCharge(ZoneBoundaryCrossing entry, ZoneBoundaryCrossing exit){
        ArrayList<ZoneBoundaryCrossing> crossings = new ArrayList<>();
        crossings.add(entry);
        crossings.add(exit);
        return calculateChargeForTimeInZone(crossings);
    } //used in tests

}