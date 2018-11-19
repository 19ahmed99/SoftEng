package com.trafficmon;

import org.junit.Test;

import java.math.BigDecimal;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class CongestionChargeSystemTest {

    CongestionChargeSystem system = new CongestionChargeSystem();


    @Test
    public void carGoesInAndOutCheckEventLogSize() {
        /*
        * Test Description
        * A car goes in then out, we check if the eventLog has been updated with 2 entries to
        * reflect this
        *
        * We created a new public method called getSizeOfEventLog to determine this value
        * */
        system.vehicleEnteringZone(Vehicle.withRegistration("A123 XYZ"));
        system.vehicleLeavingZone(Vehicle.withRegistration("A123 XYZ"));
        assertThat(system.getSizeofEventLog(), is(2));

    }

    @Test
    public void carGoesInAndOutCheckEventLogEntries() {
        system.vehicleEnteringZone(Vehicle.withRegistration("A123 XYZ"));
        system.vehicleLeavingZone(Vehicle.withRegistration("A123 XYZ"));
        assertTrue(system.getEventLogEntries(0).getVehicle().equals(Vehicle.withRegistration("A123 XYZ")));
        assertTrue(system.getEventLogEntries(1).getVehicle().equals(Vehicle.withRegistration("A123 XYZ")));
        assertTrue(system.getEventLogEntries(0) instanceof EntryEvent);
        assertTrue(system.getEventLogEntries(1) instanceof ExitEvent);

    }



    @Test
    public void carGoesInAndOutCheckCharge() {
        system.vehicleEnteringZone(Vehicle.withRegistration("A123 XYZ"));
        system.getEventLogEntries(0).setTimeStamp(1000000); //it enters at time 1000
        system.vehicleLeavingZone(Vehicle.withRegistration("A123 XYZ"));
        system.getEventLogEntries(0).setTimeStamp(2000000);
        assertThat(system.getCalculatedCharge(system.getEventLogEntries(0),system.getEventLogEntries(1)), is(new BigDecimal(0.85)));



    }



}