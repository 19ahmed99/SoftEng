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

    private PenaltiesService penaltiesService = context.mock(PenaltiesService.class);
    private CongestionChargeSystem system = new CongestionChargeSystem(penaltiesService);
    private Checker checker = new Checker();
    private Calculator calculator = new Calculator(penaltiesService);


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
        assertThat(system.getEventLog().size(), is(2));
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
        assertTrue(system.getEventLog().get(0).getVehicle().equals(Vehicle.withRegistration("A123 XYZ")));
        assertTrue(system.getEventLog().get(1).getVehicle().equals(Vehicle.withRegistration("A123 XYZ")));
        assertTrue(system.getEventLog().get(0) instanceof EntryEvent);
        assertTrue(system.getEventLog().get(1) instanceof ExitEvent);
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
        system.getEventLog().get(0).setTimeStamp(54000);
        system.vehicleLeavingZone(Vehicle.withRegistration("A123 XYZ"));
        system.getEventLog().get(1).setTimeStamp(61200);
        BigDecimal our_value = calculator.getCalculatedCharge(system.getEventLog().get(0), system.getEventLog().get(1));
        MathContext mc = new MathContext(2);
        assertThat(our_value.round(mc), is((new BigDecimal(4)).round(mc)));
    }

    @Test
    public void twoCarsGoInAndOutCheckRespectiveCharges() {
        system.vehicleEnteringZone(Vehicle.withRegistration("A123 XYZ"));
        system.getEventLog().get(0).setTimeStamp(36000); //it enters at time 1000
        system.vehicleEnteringZone(Vehicle.withRegistration("A123 ABC"));
        system.getEventLog().get(1).setTimeStamp(36000); //it enters at time 1000
        system.vehicleLeavingZone(Vehicle.withRegistration("A123 XYZ"));
        system.getEventLog().get(2).setTimeStamp(43200);
        system.vehicleLeavingZone(Vehicle.withRegistration("A123 ABC"));
        system.getEventLog().get(3).setTimeStamp(64800); //it enters at time 1000
        BigDecimal first_car = calculator.getCalculatedCharge(system.getEventLog().get(0), system.getEventLog().get(2));
        BigDecimal second_car = calculator.getCalculatedCharge(system.getEventLog().get(1), system.getEventLog().get(3));
        MathContext mc = new MathContext(2);
        assertThat(first_car.round(mc), is((new BigDecimal(6)).round(mc)));
        assertThat(second_car.round(mc), is((new BigDecimal(12)).round(mc)));
    }

    @Test
    public void checkOrderingOfCrossing_TimeStampError() {
        /*
         * Test Description
         * Create an entry and exit with messed up timestamps
         */
        final List<ZoneBoundaryCrossing> crossings = new ArrayList<>();
        crossings.add((new EntryEvent(Vehicle.withRegistration("A123 XYZ")))); //adding an entry
        crossings.add((new ExitEvent(Vehicle.withRegistration("A123 XYZ"))));//adding an exit
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
         */
        final List<ZoneBoundaryCrossing> crossings = new ArrayList<>();
        crossings.add((new EntryEvent(Vehicle.withRegistration("A123 XYZ")))); //adding an entry
        crossings.add((new EntryEvent(Vehicle.withRegistration("A123 ABC"))));//adding an entry
        assertFalse(checker.checkOrderingOf(crossings));
    }

    @Test
    public void checkOrderingOfCrossing_DoubleExitError() {
        /*
         * Test Description
         * Create two exits in a row
         */
        final List<ZoneBoundaryCrossing> crossings = new ArrayList<>();
        crossings.add((new ExitEvent(Vehicle.withRegistration("A123 XYZ")))); //adding an exit
        crossings.add((new ExitEvent(Vehicle.withRegistration("A123 ABC"))));//adding an exit
        assertFalse(checker.checkOrderingOf(crossings));
    }

    @Test
    public void checkInvestigationIsTriggeredIntoVehicleForFaultyTimestamps() {
        context.checking(new Expectations() {{
            exactly(1).of(penaltiesService).triggerInvestigationInto(Vehicle.withRegistration("A123 XYZ"));
        }});
        system.vehicleEnteringZone(Vehicle.withRegistration("A123 XYZ"));
        system.vehicleLeavingZone(Vehicle.withRegistration("A123 XYZ"));
        system.getEventLog().get(0).setTimeStamp(2000000); //it enters at time 1000
        system.getEventLog().get(1).setTimeStamp(1000000); //it enters at time 1000
        system.calculateCharges();
    }

    @Test
    public void checkPenaltyNoticeIssuedForNotRegistered(){

        system.vehicleEnteringZone(Vehicle.withRegistration("A234 YYY"));
        system.vehicleLeavingZone(Vehicle.withRegistration("A234 YYY"));
        system.getEventLog().get(0).setTimeStamp(1000); //it enters at time 1000
        system.getEventLog().get(1).setTimeStamp(2000); //it enters at time 2000
        BigDecimal expected_value = calculator.getCalculatedCharge(system.getEventLog().get(0), system.getEventLog().get(1));

        context.checking(new Expectations() {{
            exactly(1).of(penaltiesService).issuePenaltyNotice(Vehicle.withRegistration("A234 YYY"), expected_value);
        }});
        system.vehicleEnteringZone(Vehicle.withRegistration("A234 YYY"));
        system.vehicleLeavingZone(Vehicle.withRegistration("A234 YYY"));
        system.getEventLog().get(0).setTimeStamp(1000); //it enters at time 1000
        system.getEventLog().get(1).setTimeStamp(2000); //it enters at time 2000
        system.calculateCharges();
    }

    @Test
    public void checkPreviouslyRegistered(){
        //Create an entry event for the car
        //Check that it's registered after
        assertFalse(checker.previouslyRegistered(Vehicle.withRegistration("A123 ABC"), system.getEventLog()));
        system.vehicleEnteringZone(Vehicle.withRegistration("A123 ABC"));
        assertTrue(checker.previouslyRegistered(Vehicle.withRegistration("A123 ABC"), system.getEventLog()));
    }

    @Test
    public void unregisteredCarExitTest(){
        // Create an exit event for the car
        // Check that it's not registered
        system.vehicleLeavingZone(Vehicle.withRegistration("A123 XYZ"));
        assertThat(system.getEventLog().size(), is(0));
        assertFalse(checker.previouslyRegistered(Vehicle.withRegistration("A123 XYZ"), system.getEventLog()));
    }
}
