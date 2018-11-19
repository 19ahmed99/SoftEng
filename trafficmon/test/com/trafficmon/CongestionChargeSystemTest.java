package com.trafficmon;

import org.junit.Test;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class CongestionChargeSystemTest {

    CongestionChargeSystem system = new CongestionChargeSystem();


    @Test
    public void carGoesInAndOut() {
        system.vehicleEnteringZone(Vehicle.withRegistration("A123 XYZ"));
        system.vehicleLeavingZone(Vehicle.withRegistration("A123 XYZ"));
        assertThat(system.getSizeofEventLog(), is(2));

    }

}