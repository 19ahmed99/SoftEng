package com.trafficmon;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public interface CalculatorInterface {
    void calculateCharges(Map<Vehicle, List<ZoneBoundaryCrossing>> crossingsByVehicle);
    void charge_account(Vehicle vehicle, BigDecimal charge);
    BigDecimal getCalculatedCharge(List<ZoneBoundaryCrossing> crossings);
}
