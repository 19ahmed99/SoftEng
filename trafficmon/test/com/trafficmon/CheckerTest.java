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

public class CheckerTest {

    @Rule
    public JUnitRuleMockery context = new JUnitRuleMockery();
    private Checker checker = new Checker();

    //Test1
    //check previously_registered works fine
    //create dummy event log and a vehicle that is in the log and assert that the result is true

    @Test
    public void checkPreviouslyRegisteredOnRegisteredCar() {
        List<ZoneBoundaryCrossing> dummy_eventLog = new ArrayList<>();
        Vehicle vehicle = Vehicle.withRegistration("A123 XYZ");
        dummy_eventLog.add(new EntryEvent(vehicle));
        dummy_eventLog.add(new ExitEvent(vehicle));
        assertTrue(checker.previouslyRegistered(vehicle,dummy_eventLog));

    }

    //Test2
    //check previously_registered works fine
    //create dummy event log and a vehicle that is NOt in the log and assert that the result is false

    @Test
    public void checkPreviouslyRegisteredOnUnregisteredCar() {
        List<ZoneBoundaryCrossing> dummy_eventLog = new ArrayList<>();
        Vehicle vehicle = Vehicle.withRegistration("A123 XYZ");
        dummy_eventLog.add(new EntryEvent(vehicle));
        dummy_eventLog.add(new ExitEvent(vehicle));
        assertFalse(checker.previouslyRegistered(Vehicle.withRegistration("A123 ABC"),dummy_eventLog));
    }


    // Checks the event log for these problems
    //   - Timestamps not ordered
    //   - Two entries in a row
    //   - Two exits in a row

    @Test
    public void testingCheckOrderingOfWithFaultyTimestamps() {
        /*
         * Test Description
         * Create an entry and exit with messed up timestamps
         * Check that the checkOrdering() method returns false
         * sort out the timestamps then check that it is working now
         */
        final List<ZoneBoundaryCrossing> crossings = new ArrayList<>();
        crossings.add((new EntryEvent(Vehicle.withRegistration("A123 XYZ")))); //adding an entry
        crossings.add((new ExitEvent(Vehicle.withRegistration("A123 XYZ")))); //adding an exit
        crossings.get(1).setTimeStamp(crossings.get(0).timestamp() - 1); //making the second event have a smaller timestamp than the first
        assertFalse(checker.checkOrderingOf(crossings));
        crossings.get(1).setTimeStamp(crossings.get(0).timestamp() + 1);
        assertTrue(checker.checkOrderingOf(crossings));
    }

    @Test
    public void checkOrderingOfCrossing_DoubleEntryError() {
        /*
         * Test Description
         * Create two entries in a row
         * Check that the checkOrdering() method returns false
         */
        final List<ZoneBoundaryCrossing> crossings = new ArrayList<>();
        crossings.add((new EntryEvent(Vehicle.withRegistration("A123 XYZ")))); // Adding an entry
        crossings.add((new EntryEvent(Vehicle.withRegistration("A123 XYZ")))); // Adding an entry
        assertFalse(checker.checkOrderingOf(crossings));
    }

    @Test
    public void checkOrderingOfCrossing_DoubleExitError() {
        /*
         * Test Description
         * Create two exits in a row
         * Check that the checkOrdering() method returns false
         */
        final List<ZoneBoundaryCrossing> crossings = new ArrayList<>();
        crossings.add((new ExitEvent(Vehicle.withRegistration("A123 XYZ")))); // Adding an exit
        crossings.add((new ExitEvent(Vehicle.withRegistration("A123 XYZ")))); // Adding an exit
        assertFalse(checker.checkOrderingOf(crossings));
    }

}
