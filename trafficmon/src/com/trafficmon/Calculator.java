package com.trafficmon;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Calculator {
    private final CongestionChargeSystem congestionChargeSystem;

    Calculator(CongestionChargeSystem congestionChargeSystem) {
        this.congestionChargeSystem = congestionChargeSystem;
    }

    public void calculateCharges(Map<Vehicle, List<ZoneBoundaryCrossing>> crossingsByVehicle,BigDecimal CHARGE_RATE_POUNDS_PER_MINUTE) {

        for (Map.Entry<Vehicle, List<ZoneBoundaryCrossing>> vehicleCrossings : crossingsByVehicle.entrySet()) {
            Vehicle vehicle = vehicleCrossings.getKey();
            List<ZoneBoundaryCrossing> crossings = vehicleCrossings.getValue();
            //loop through the hashmap and set "vehicle" to the key and "crossings" to the arraylist

            if (!congestionChargeSystem.checkOrderingOf(crossings)) {
                congestionChargeSystem.getOperationsTeam().triggerInvestigationInto(vehicle); //if ordering is messed up, then investigate
            } else {
                BigDecimal charge = calculateChargeForTimeInZone(crossings, CHARGE_RATE_POUNDS_PER_MINUTE); //calculate the charge
                try {
                    RegisteredCustomerAccountsService.getInstance().accountFor(vehicle).deduct(charge);
                } catch (InsufficientCreditException | AccountNotRegisteredException ice) {
                    congestionChargeSystem.getOperationsTeam().issuePenaltyNotice(vehicle, charge);
                }
            }
        }
    }

    protected BigDecimal calculateChargeForTimeInZone(List<ZoneBoundaryCrossing> crossings, BigDecimal CHARGE_RATE_POUNDS_PER_MINUTE) {

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
}