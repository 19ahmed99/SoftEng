package com.trafficmon;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public interface CalculatorInterface {
    void calculateCharges(Map<VehicleInterface, List<ZoneBoundaryCrossing>> crossingsByVehicle);
    void charge_account(VehicleInterface vehicle, BigDecimal charge);
    BigDecimal getCharge(List<ZoneBoundaryCrossing> crossings);
}
