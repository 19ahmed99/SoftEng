package com.trafficmon;

import org.junit.Test;

import java.math.*;
import java.util.ArrayList;
import java.util.List;

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
        /*
        * Test Description
        * A car goes in then out, we check the eventlog entries if they are indeed the correct
        * vehicles that are logged and whether the first and second are entry and exit events respecively.
        *
        */
        system.vehicleEnteringZone(Vehicle.withRegistration("A123 XYZ"));
        system.vehicleLeavingZone(Vehicle.withRegistration("A123 XYZ"));
        assertTrue(system.getEventLogEntries(0).getVehicle().equals(Vehicle.withRegistration("A123 XYZ")));
        assertTrue(system.getEventLogEntries(1).getVehicle().equals(Vehicle.withRegistration("A123 XYZ")));
        assertTrue(system.getEventLogEntries(0) instanceof EntryEvent);
        assertTrue(system.getEventLogEntries(1) instanceof ExitEvent);

    }


    @Test
    public void carGoesInAndOutCheckCharge() {
        /*
        * Test Description
        *car goes in, we set the timestamp
        * the car leaves, we set the timestamp
        * we calculate the charge for these two events and assert that it is indeed 0.85 - a value which we previously calculated
        */
        system.vehicleEnteringZone(Vehicle.withRegistration("A123 XYZ"));
        system.getEventLogEntries(0).setTimeStamp(1000000); //it enters at time 1000
        system.vehicleLeavingZone(Vehicle.withRegistration("A123 XYZ"));
        system.getEventLogEntries(1).setTimeStamp(2000000);
        BigDecimal our_value = system.getCalculatedCharge(system.getEventLogEntries(0), system.getEventLogEntries(1));
        MathContext mc = new MathContext(2);
        assertThat(our_value.round(mc), is((new BigDecimal(0.85)).round(mc)));
    }

    @Test
    public void twoCarsGoInAndOutCheckRespectiveCharges() {
        system.vehicleEnteringZone(Vehicle.withRegistration("A123 XYZ"));
        system.getEventLogEntries(0).setTimeStamp(1000000); //it enters at time 1000
        system.vehicleEnteringZone(Vehicle.withRegistration("A123 ABC"));
        system.getEventLogEntries(1).setTimeStamp(1500000); //it enters at time 1000
        system.vehicleLeavingZone(Vehicle.withRegistration("A123 XYZ"));
        system.getEventLogEntries(2).setTimeStamp(2000000);
        system.vehicleLeavingZone(Vehicle.withRegistration("A123 ABC"));
        system.getEventLogEntries(3).setTimeStamp(3000000); //it enters at time 1000
        BigDecimal first_car = system.getCalculatedCharge(system.getEventLogEntries(0), system.getEventLogEntries(2));
        BigDecimal second_car = system.getCalculatedCharge(system.getEventLogEntries(1), system.getEventLogEntries(3));
        MathContext mc = new MathContext(2);
        assertThat(first_car.round(mc), is((new BigDecimal(0.85)).round(mc)));
        assertThat(second_car.round(mc), is((new BigDecimal(1.25)).round(mc)));
    }




}