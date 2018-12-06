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

public class CCSTest {

    @Rule
    public JUnitRuleMockery context = new JUnitRuleMockery();
    private CalculatorInterface calculator = context.mock(CalculatorInterface.class);
    private CheckerInterface checker = context.mock(CheckerInterface.class);
    private CongestionChargeSystem system = new CongestionChargeSystem(calculator,checker);

    @Test
        public void carGoesInAndOutCheckEventLogSize() {
        /*
         * Test Description
         * A car goes in then out, we check if the event log has been updated with 2 entries to
         * reflect this
         *
         * We created a new public method called getSizeOfEventLog to determine this value
         * */
        context.checking(new Expectations() {{
            ignoring(checker).previouslyRegistered(Vehicle.withRegistration("A123 XYZ"),system.getEventLog());
            will(returnValue(true));
        }});

        system.vehicleEnteringZone(Vehicle.withRegistration("A123 XYZ"));
        system.vehicleLeavingZone(Vehicle.withRegistration("A123 XYZ"));
        assertThat(system.getEventLog().size(), is(2));
    }

    @Test
    public void carGoesInAndOutCheckEventLogEntries() {
        /*
         * Test Description
         * A car goes in then out, we check the event log entries if they are indeed the correct
         * vehicles that are logged and whether the first and second are entry and exit events respectively.
         *
         */
        context.checking(new Expectations() {{
            ignoring(checker).previouslyRegistered(Vehicle.withRegistration("A123 XYZ"),system.getEventLog());
            will(returnValue(true));
        }});

        system.vehicleEnteringZone(Vehicle.withRegistration("A123 XYZ"));
        system.vehicleLeavingZone(Vehicle.withRegistration("A123 XYZ"));
        assertTrue(system.getEventLog().get(0).getVehicle().equals(Vehicle.withRegistration("A123 XYZ")));
        assertTrue(system.getEventLog().get(1).getVehicle().equals(Vehicle.withRegistration("A123 XYZ")));
        assertTrue(system.getEventLog().get(0) instanceof EntryEvent);
        assertTrue(system.getEventLog().get(1) instanceof ExitEvent);
    }

    @Test
    public void checkPreviouslyRegisteredIsDelegatedToChecker() {
        context.checking(new Expectations() {{
            exactly(1).of(checker).previouslyRegistered(Vehicle.withRegistration("A123 XYZ"),system.getEventLog());
            will(returnValue(true));
        }});

        system.vehicleEnteringZone(Vehicle.withRegistration("A123 XYZ"));
        system.vehicleLeavingZone(Vehicle.withRegistration("A123 XYZ"));

    }

    @Test
    public void checkPreviouslyRegisteredIsDelegatedToCheckerWithUnregisteredCar() {
        context.checking(new Expectations() {{
            exactly(1).of(checker).previouslyRegistered(Vehicle.withRegistration("A123 XYA"),system.getEventLog());
            will(returnValue(false));
        }});

        system.vehicleEnteringZone(Vehicle.withRegistration("A123 XYZ"));
        system.vehicleLeavingZone(Vehicle.withRegistration("A123 XYA"));

    }

    @Test
    public void checkCalculateChargeIsDelegatedToCalculator() {
        context.checking(new Expectations() {{
            ignoring(checker).previouslyRegistered(Vehicle.withRegistration("A123 XYZ"),system.getEventLog());
            will(returnValue(true));
            exactly(1).of(calculator).calculateCharges(system.getHashMap());
        }});

        system.vehicleEnteringZone(Vehicle.withRegistration("A123 XYZ"));
        system.vehicleLeavingZone(Vehicle.withRegistration("A123 XYZ"));
        system.calculateCharges();

    }


}
