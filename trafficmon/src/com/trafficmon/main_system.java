package com.trafficmon;

import java.util.List;

public interface main_system {
    void vehicleEnteringZone(Vehicle vehicle);
    void vehicleLeavingZone(Vehicle vehicle);
    void calculateCharges();
    List<ZoneBoundaryCrossing> getEventLog();
}
