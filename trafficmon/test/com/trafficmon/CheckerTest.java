package com.trafficmon;

import org.junit.Test;
import java.util.ArrayList;
import java.util.List;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class CheckerTest {

    private Checker checker = new Checker();

    @Test
    public void checkPreviouslyRegisteredOnRegisteredCar() {
        /*
         * Test Description
         * We create an EventLog with two events for a car
         * We assert that this car is registered
         */
        List<ZoneBoundaryCrossing> dummy_eventLog = new ArrayList<>();
        VehicleInterface vehicle = Vehicle.withRegistration("A123 XYZ");
        dummy_eventLog.add(new EntryEvent(vehicle));
        dummy_eventLog.add(new ExitEvent(vehicle));

        assertTrue(checker.previouslyRegistered(vehicle,dummy_eventLog));
    }

    @Test
    public void checkPreviouslyRegisteredOnUnregisteredCar() {
        /*
         * Test Description
         * We create an EventLog with two events for a car
         * We assert that another car is not registered
         */
        List<ZoneBoundaryCrossing> dummy_eventLog = new ArrayList<>();
        VehicleInterface vehicle = Vehicle.withRegistration("A123 XYZ");
        dummy_eventLog.add(new EntryEvent(vehicle));
        dummy_eventLog.add(new ExitEvent(vehicle));

        assertFalse(checker.previouslyRegistered(Vehicle.withRegistration("A123 ABC"),dummy_eventLog));
    }

    @Test
    public void testingCheckOrderingWithFaultyTimestamps() {
        /*
         * Test Description
         * Create an entry and exit with messed up timestamps
         * Check that the checkOrdering() method returns False
         */
        final List<ZoneBoundaryCrossing> crossings = new ArrayList<>();
        crossings.add((new EntryEvent(Vehicle.withRegistration("A123 XYZ")))); // Adding an entry
        crossings.add((new ExitEvent(Vehicle.withRegistration("A123 XYZ")))); // Adding an exit
        crossings.get(1).setTimeStamp(crossings.get(0).timestamp() - 1); // We make the second event have a smaller timestamp than the first

        assertFalse(checker.checkOrderingOf(crossings));
    }

    @Test
    public void testingCheckOrderingWithCorrectTimestamps() {
        /*
         * Test Description
         * Create an entry and an exit with correct timestamps
         * Check that the checkOrdering() method returns True
         */
        final List<ZoneBoundaryCrossing> crossings = new ArrayList<>();
        crossings.add((new EntryEvent(Vehicle.withRegistration("A123 XYZ")))); // Adding an entry
        crossings.add((new ExitEvent(Vehicle.withRegistration("A123 XYZ")))); // Adding an exit
        crossings.get(1).setTimeStamp(crossings.get(0).timestamp() + 1); // We make the second event have a larger timestamp than the first

        assertTrue(checker.checkOrderingOf(crossings));
    }

    @Test
    public void checkOrderingOfCrossing_DoubleEntryError() {
        /*
         * Test Description
         * Create two entries in a row
         * Check that the checkOrdering() method returns False
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
         * Check that the checkOrdering() method returns False
         */
        final List<ZoneBoundaryCrossing> crossings = new ArrayList<>();
        crossings.add((new ExitEvent(Vehicle.withRegistration("A123 XYZ")))); // Adding an exit
        crossings.add((new ExitEvent(Vehicle.withRegistration("A123 XYZ")))); // Adding an exit

        assertFalse(checker.checkOrderingOf(crossings));
    }
}
