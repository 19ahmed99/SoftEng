package com.trafficmon;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Calculator {
    private Checker checker;
    private PenaltiesService operationsTeam;

    Calculator(Checker checker, PenaltiesService operationsTeam) {
        this.checker = checker;
        this.operationsTeam = operationsTeam;
    }


    void calculateCharges(Map<Vehicle, List<ZoneBoundaryCrossing>> crossingsByVehicle) {

        for (Map.Entry<Vehicle, List<ZoneBoundaryCrossing>> vehicleCrossings : crossingsByVehicle.entrySet()) {
            Vehicle vehicle = vehicleCrossings.getKey();
            List<ZoneBoundaryCrossing> crossings = vehicleCrossings.getValue();
            //loop through the hashmap and set "vehicle" to the key and "crossings" to the arraylist

            if (!checker.checkOrderingOf(crossings)) {
                operationsTeam.triggerInvestigationInto(vehicle); //if ordering is messed up, then investigate
            } else {
                BigDecimal charge = getCharge(crossings);
                try {
                    RegisteredCustomerAccountsService.getInstance().accountFor(vehicle).deduct(charge);
                } catch (InsufficientCreditException | AccountNotRegisteredException ice) {
                    operationsTeam.issuePenaltyNotice(vehicle, charge); }
            }
        }
    }

    private BigDecimal getCharge(List<ZoneBoundaryCrossing> crossings) {
        BigDecimal charge;
        ZoneBoundaryCrossing lastEvent = crossings.get(0);
        int timeIn = 0;

        if (lastEvent.timestamp() <= 50400) { //It is 2pm
            charge = new BigDecimal(6);
        } else {
            charge = new BigDecimal(4);
        }
        for (ZoneBoundaryCrossing crossing : crossings.subList(1, crossings.size())) {
            if (crossing instanceof ExitEvent) {
                  timeIn += crossing.timestamp()-lastEvent.timestamp();
            }
            if (timeIn > 14400) { //14400 is 4h
                charge = new BigDecimal(12);
                break;
            }
            lastEvent = crossing;
        }
        return charge;
    }

    BigDecimal getCalculatedCharge(ZoneBoundaryCrossing entry, ZoneBoundaryCrossing exit){
        ArrayList<ZoneBoundaryCrossing> crossings = new ArrayList<>();
        crossings.add(entry);
        crossings.add(exit);

        return getCharge(crossings);
    }
}