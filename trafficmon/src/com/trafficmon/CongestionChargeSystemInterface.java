package com.trafficmon;

import java.util.List;
import java.util.Map;

public interface CongestionChargeSystemInterface {
    void vehicleEnteringZone(Vehicle vehicle);
    void vehicleLeavingZone(Vehicle vehicle);
    void calculateCharges();
    List<ZoneBoundaryCrossing> getEventLog();
    Map<Vehicle, List<ZoneBoundaryCrossing>> getHashMap();

}
