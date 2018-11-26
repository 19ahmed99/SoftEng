package com.trafficmon;

import org.junit.Rule;
import org.junit.Test;

import java.math.*;
import java.util.ArrayList;
import java.util.List;
import org.jmock.Expectations;
import org.jmock.integration.junit4.JUnitRuleMockery;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class CongestionChargeSystemTest {

    @Rule
    public JUnitRuleMockery context = new JUnitRuleMockery();

    OrderingInterpreter ordInterpret = context.mock(OrderingInterpreter.class);

    OperationsTeamSystem operationsTeamSystem = context.mock(OperationsTeamSystem.class);

    CongestionChargeSystem system = new CongestionChargeSystem(ordInterpret, operationsTeamSystem);


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

    @Test
    public void checkOrderingOfCrossing_TimeStampError() {
        /*
        * Test Description
        * Create an entry and exit with messed up timestamps
        */
        context.checking(new Expectations() {{
            exactly(1).of(ordInterpret).timestamp_error();
            exactly(1).of(ordInterpret).perfect_ordering();
        }});

        final List<ZoneBoundaryCrossing> crossings = new ArrayList<ZoneBoundaryCrossing>();
        crossings.add((new EntryEvent(Vehicle.withRegistration("A123 XYZ")))); //adding an entry
        crossings.add((new ExitEvent(Vehicle.withRegistration("A123 XYZ"))));//adding an exit
        crossings.get(1).setTimeStamp(crossings.get(0).timestamp() - 1); //making the second event have a smaller timestamp than the first
        system.checkOrderingOfCrossings(crossings);
        crossings.get(1).setTimeStamp(crossings.get(0).timestamp() + 1);
        system.checkOrderingOfCrossings(crossings);

    }

    @Test
    public void checkOrderingOfCrossing_DoubleEntryError() {
        /*
        * Test Description
        * Create an entry and exit with messed up timestamps
        */
        context.checking(new Expectations() {{
            exactly(1).of(ordInterpret).doubleEntry_error();
        }});

        final List<ZoneBoundaryCrossing> crossings = new ArrayList<ZoneBoundaryCrossing>();
        crossings.add((new EntryEvent(Vehicle.withRegistration("A123 XYZ")))); //adding an entry
        crossings.add((new EntryEvent(Vehicle.withRegistration("A123 ABC"))));//adding an exit
        system.checkOrderingOfCrossings(crossings);

    }

    @Test
    public void checkOrderingOfCrossing_DoubleExitError() {
        /*
        * Test Description
        * Create an entry and exit with messed up timestamps
        */
        context.checking(new Expectations() {{
            exactly(1).of(ordInterpret).doubleExit_error();
        }});

        final List<ZoneBoundaryCrossing> crossings = new ArrayList<ZoneBoundaryCrossing>();
        crossings.add((new ExitEvent(Vehicle.withRegistration("A123 XYZ")))); //adding an entry
        crossings.add((new ExitEvent(Vehicle.withRegistration("A123 ABC"))));//adding an exit
        system.checkOrderingOfCrossings(crossings);

    }

    @Test
    public void checkInvestigationIsTriggeredIntoVehicleForFaultyTimestamps(){
        /*
        * create a vehicle with entry and exit
        * edit the entry and exit events to have messed up timestamp
        * call the calculateCharges() method
        * you expect to see a call to the mock object of the operationsteamsystem
        * */

        context.checking(new Expectations() {{
            exactly(1).of(ordInterpret).timestamp_error();
            exactly(1).of(operationsTeamSystem).triggerInvestigationIntoVehicle();
        }});

        system.vehicleEnteringZone(Vehicle.withRegistration("A123 XYZ"));
        system.vehicleLeavingZone(Vehicle.withRegistration("A123 XYZ"));
        system.getEventLogEntries(0).setTimeStamp(2000000); //it enters at time 1000
        system.getEventLogEntries(1).setTimeStamp(1000000); //it enters at time 1000
        system.calculateCharges();

    }

    @Test
    public void checkPenaltyNoticeIssuedForInsufficientFunds(){

        context.checking(new Expectations() {{
            exactly(1).of(ordInterpret).perfect_ordering();
            exactly(1).of(operationsTeamSystem).issuePenaltyNotice();
        }});

        system.vehicleEnteringZone(Vehicle.withRegistration("A123 XYZ"));
        system.vehicleLeavingZone(Vehicle.withRegistration("A123 XYZ"));
        system.getEventLogEntries(0).setTimeStamp(1000000); //it enters at time 1000
        system.getEventLogEntries(1).setTimeStamp(1000000000); //it enters at time 1000
        system.calculateCharges();

    }

    @Test
    public void checkPreviouslyRegistered(){
        //Create an exit event for the car
        //calaculate charges and see what comes up
        assertFalse(system.checkIfRegistered(Vehicle.withRegistration("A123 XYZ")));
        system.vehicleEnteringZone(Vehicle.withRegistration("A123 ABC"));
        assertTrue(system.checkIfRegistered(Vehicle.withRegistration("A123 ABC")));

    }



}