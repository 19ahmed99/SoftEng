package com.trafficmon;

import java.util.List;

public interface CheckerInterface {
    boolean previouslyRegistered(VehicleInterface vehicle, List<ZoneBoundaryCrossing> eventLog);
    boolean checkOrderingOf(List<ZoneBoundaryCrossing> crossings);
}
