package com.trafficmon;

import java.util.List;
import java.util.Map;

public interface CongestionChargeSystemInterface {
    void vehicleEnteringZone(VehicleInterface vehicle);
    void vehicleLeavingZone(VehicleInterface vehicle);
    void calculateCharges();
    List<ZoneBoundaryCrossing> getEventLog();
    Map<VehicleInterface, List<ZoneBoundaryCrossing>> getHashMap();
}
