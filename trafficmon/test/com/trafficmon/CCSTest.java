package com.trafficmon;

import org.junit.Rule;
import org.junit.Test;
import org.jmock.Expectations;
import org.jmock.integration.junit4.JUnitRuleMockery;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
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
         * A car goes in then out
         * We assert that the Event Log has been updated with 2 events to reflect this
         */
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
         * A car goes in then out
         * We assert that the Event Log events are indeed for the correct vehicles
         * and whether the first and second are entry and exit events respectively.
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
        /*
         * Test Description
         * A car goes in then out
         * We expect a call to the checker, to check if the vehicle is already registered
         */
        context.checking(new Expectations() {{
            exactly(1).of(checker).previouslyRegistered(Vehicle.withRegistration("A123 XYZ"),system.getEventLog());
                will(returnValue(true));
        }});

        system.vehicleEnteringZone(Vehicle.withRegistration("A123 XYZ"));
        system.vehicleLeavingZone(Vehicle.withRegistration("A123 XYZ"));
    }

    @Test
    public void checkPreviouslyRegisteredIsDelegatedToCheckerUsingAnUnregisteredCar() {
        /*
         * Test Description
         * A car goes in then another car goes out
         * We expect a call to the checker, to check if that unregistered car is registered
         */
        context.checking(new Expectations() {{
            exactly(1).of(checker).previouslyRegistered(Vehicle.withRegistration("A123 XYA"),system.getEventLog());
                will(returnValue(false));
        }});

        system.vehicleEnteringZone(Vehicle.withRegistration("A123 XYZ"));
        system.vehicleLeavingZone(Vehicle.withRegistration("A123 XYA"));
    }

    @Test
    public void checkCalculateChargeIsDelegatedToCalculator() {
        /*
         * Test Description
         * A car goes in then out, then the calculateCharges() is called
         * We expect a call to the calculator with the HashMap generated
         */
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
