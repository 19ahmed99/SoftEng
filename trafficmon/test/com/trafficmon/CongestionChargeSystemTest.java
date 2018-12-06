//package com.trafficmon;
//
//import org.junit.Rule;
//import org.junit.Test;
//
//import java.math.*;
//import java.util.ArrayList;
//import java.util.List;
//import org.jmock.Expectations;
//import org.jmock.integration.junit4.JUnitRuleMockery;
//import static org.hamcrest.MatcherAssert.assertThat;
//import static org.hamcrest.core.Is.is;
//import static org.junit.Assert.assertFalse;
//import static org.junit.Assert.assertTrue;
//
//public class CongestionChargeSystemTest {
//
//    @Rule
//    public JUnitRuleMockery context = new JUnitRuleMockery();
//
//    // Create a mock of a Operations Team
//    private PenaltiesService penaltiesService = context.mock(PenaltiesService.class);
//    private CongestionChargeSystem system = new CongestionChargeSystem(penaltiesService);
//    private Checker checker = new Checker();
//    private Calculator calculator = new Calculator(penaltiesService);
//
//
//    @Test
//    public void carGoesInAndOutCheckEventLogSize() {
//        /*
//         * Test Description
//         * A car goes in then out, we check if the event log has been updated with 2 entries to
//         * reflect this
//         *
//         * We created a new public method called getSizeOfEventLog to determine this value
//         * */
//        system.vehicleEnteringZone(Vehicle.withRegistration("A123 XYZ"));
//        system.vehicleLeavingZone(Vehicle.withRegistration("A123 XYZ"));
//        assertThat(system.getEventLog().size(), is(2));
//    }
//
//    @Test
//    public void carGoesInAndOutCheckEventLogEntries() {
//        /*
//         * Test Description
//         * A car goes in then out, we check the event log entries if they are indeed the correct
//         * vehicles that are logged and whether the first and second are entry and exit events respectively.
//         *
//         */
//        system.vehicleEnteringZone(Vehicle.withRegistration("A123 XYZ"));
//        system.vehicleLeavingZone(Vehicle.withRegistration("A123 XYZ"));
//        assertTrue(system.getEventLog().get(0).getVehicle().equals(Vehicle.withRegistration("A123 XYZ")));
//        assertTrue(system.getEventLog().get(1).getVehicle().equals(Vehicle.withRegistration("A123 XYZ")));
//        assertTrue(system.getEventLog().get(0) instanceof EntryEvent);
//        assertTrue(system.getEventLog().get(1) instanceof ExitEvent);
//    }
//
//    @Test
//    public void carGoesInAndOutCheckCharge() {
//        /*
//         * Test Description
//         * Car goes in, we set the timestamp
//         * the car leaves, we set the timestamp
//         * we calculate the charge for these two events and assert that it is indeed 4 - a value for an entry after 2pm
//         * Here, we check charges for : - Car that entered after 2pm (less than 4h in-zone)
//         */
//        system.vehicleEnteringZone(Vehicle.withRegistration("A123 XYZ"));
//        system.getEventLog().get(0).setTimeStamp(54000);
//        system.vehicleLeavingZone(Vehicle.withRegistration("A123 XYZ"));
//        system.getEventLog().get(1).setTimeStamp(61200);
//
//        List<ZoneBoundaryCrossing> crossings = new ArrayList<>();
//        crossings.add(system.getEventLog().get(0));
//        crossings.add(system.getEventLog().get(1));
//
//        BigDecimal our_value = calculator.getCalculatedCharge(crossings);
//        MathContext mc = new MathContext(2);
//        assertThat(our_value.round(mc), is((new BigDecimal(4)).round(mc)));
//    }
//
//    @Test
//    public void twoCarsGoInAndOutCheckCharges() {
//        /*
//         * Test Description
//         * Two cars go in/out, we set all the timestamps and check that the correct charges are applied
//         * Here, we check charges for : - Car that entered before 2pm (less than 4h in-zone)
//         *                              - Car that entered before 2pm (more than 4h in-zone)
//         */
//        system.vehicleEnteringZone(Vehicle.withRegistration("A123 XYZ"));
//        system.getEventLog().get(0).setTimeStamp(36000);
//        system.vehicleEnteringZone(Vehicle.withRegistration("A123 ABC"));
//        system.getEventLog().get(1).setTimeStamp(36000);
//        system.vehicleLeavingZone(Vehicle.withRegistration("A123 XYZ"));
//        system.getEventLog().get(2).setTimeStamp(43200);
//        system.vehicleLeavingZone(Vehicle.withRegistration("A123 ABC"));
//        system.getEventLog().get(3).setTimeStamp(64800);
//
//        List<ZoneBoundaryCrossing> crossings_car1 = new ArrayList<>();
//        crossings_car1.add(system.getEventLog().get(0));
//        crossings_car1.add(system.getEventLog().get(2));
//        List<ZoneBoundaryCrossing> crossings_car2 = new ArrayList<>();
//        crossings_car2.add(system.getEventLog().get(1));
//        crossings_car2.add(system.getEventLog().get(3));
//
//        BigDecimal first_car = calculator.getCalculatedCharge(crossings_car1);
//        BigDecimal second_car = calculator.getCalculatedCharge(crossings_car2);
//        MathContext mc = new MathContext(2);
//        assertThat(first_car.round(mc), is((new BigDecimal(6)).round(mc)));
//        assertThat(second_car.round(mc), is((new BigDecimal(12)).round(mc)));
//    }
//
//    @Test
//    public void SomeCarsGoInAndOutCheckCharges() {
//        /*
//         * Test Description
//         * Some cars go in/out, we set all the timestamps and check that the correct charges are applied
//         * Here, we check charges for : - Car that entered after 2pm (more than 4h in-zone)
//         *                              - Car that entered at 2pm (less than 4h in-zone)
//         *                              - Car that entered at 2pm (more than 4h in-zone)
//         */
//        // All cars In
//        system.vehicleEnteringZone(Vehicle.withRegistration("A123 XYZ"));
//        system.getEventLog().get(0).setTimeStamp(64800);
//        system.vehicleEnteringZone(Vehicle.withRegistration("A123 ABC"));
//        system.getEventLog().get(1).setTimeStamp(50400);
//        system.vehicleEnteringZone(Vehicle.withRegistration("D243 5PR"));
//        system.getEventLog().get(2).setTimeStamp(50400);
//        // All cars Out
//        system.vehicleLeavingZone(Vehicle.withRegistration("A123 XYZ"));
//        system.getEventLog().get(3).setTimeStamp(82800);
//        system.vehicleLeavingZone(Vehicle.withRegistration("A123 ABC"));
//        system.getEventLog().get(4).setTimeStamp(61200);
//        system.vehicleLeavingZone(Vehicle.withRegistration("A123 ABC"));
//        system.getEventLog().get(5).setTimeStamp(72000);
//
//        List<ZoneBoundaryCrossing> crossings_car1 = new ArrayList<>();
//        crossings_car1.add(system.getEventLog().get(0));
//        crossings_car1.add(system.getEventLog().get(3));
//        List<ZoneBoundaryCrossing> crossings_car2 = new ArrayList<>();
//        crossings_car2.add(system.getEventLog().get(1));
//        crossings_car2.add(system.getEventLog().get(4));
//        List<ZoneBoundaryCrossing> crossings_car3 = new ArrayList<>();
//        crossings_car3.add(system.getEventLog().get(2));
//        crossings_car3.add(system.getEventLog().get(5));
//
//        BigDecimal first_car = calculator.getCalculatedCharge(crossings_car1);
//        BigDecimal second_car = calculator.getCalculatedCharge(crossings_car2);
//        BigDecimal third_car = calculator.getCalculatedCharge(crossings_car3);
//        MathContext mc = new MathContext(2);
//        assertThat(first_car.round(mc), is((new BigDecimal(12)).round(mc)));
//        assertThat(second_car.round(mc), is((new BigDecimal(4)).round(mc)));
//        assertThat(third_car.round(mc), is((new BigDecimal(12)).round(mc)));
//    }
//
//    @Test
//    public void checkOrderingOfCrossing_TimeStampError() {
//        /*
//         * Test Description
//         * Create an entry and exit with messed up timestamps
//         * Check that the checkOrdering() method returns false
//         */
//        final List<ZoneBoundaryCrossing> crossings = new ArrayList<>();
//        crossings.add((new EntryEvent(Vehicle.withRegistration("A123 XYZ")))); //adding an entry
//        crossings.add((new ExitEvent(Vehicle.withRegistration("A123 XYZ")))); //adding an exit
//        crossings.get(1).setTimeStamp(crossings.get(0).timestamp() - 1); //making the second event have a smaller timestamp than the first
//        assertFalse(checker.checkOrderingOf(crossings));
//        crossings.get(1).setTimeStamp(crossings.get(0).timestamp() + 1);
//        assertTrue(checker.checkOrderingOf(crossings));
//    }
//
//    @Test
//    public void checkOrderingOfCrossing_DoubleEntryError() {
//        /*
//         * Test Description
//         * Create two entries in a row
//         * Check that the checkOrdering() method returns false
//         */
//        final List<ZoneBoundaryCrossing> crossings = new ArrayList<>();
//        crossings.add((new EntryEvent(Vehicle.withRegistration("A123 XYZ")))); // Adding an entry
//        crossings.add((new EntryEvent(Vehicle.withRegistration("A123 XYZ")))); // Adding an entry
//        assertFalse(checker.checkOrderingOf(crossings));
//    }
//
//    @Test
//    public void checkOrderingOfCrossing_DoubleExitError() {
//        /*
//         * Test Description
//         * Create two exits in a row
//         * Check that the checkOrdering() method returns false
//         */
//        final List<ZoneBoundaryCrossing> crossings = new ArrayList<>();
//        crossings.add((new ExitEvent(Vehicle.withRegistration("A123 XYZ")))); // Adding an exit
//        crossings.add((new ExitEvent(Vehicle.withRegistration("A123 XYZ")))); // Adding an exit
//        assertFalse(checker.checkOrderingOf(crossings));
//    }
//
//    @Test
//    public void checkInvestigationIsTriggeredIntoVehicleForFaultyTimestamps() {
//        /*
//         * Test Description
//         * Create an Entry and an Exit for a Vehicle with faulty timestamps
//         * Check that an investigation is triggered on it
//         */
//        context.checking(new Expectations() {{
//            exactly(1).of(penaltiesService).triggerInvestigationInto(Vehicle.withRegistration("A123 XYZ"));
//        }});
//        system.vehicleEnteringZone(Vehicle.withRegistration("A123 XYZ"));
//        system.vehicleLeavingZone(Vehicle.withRegistration("A123 XYZ"));
//        system.getEventLog().get(0).setTimeStamp(2000000);
//        system.getEventLog().get(1).setTimeStamp(1000000);
//        system.calculateCharges();
//    }
//
//    @Test
//    public void checkPenaltyNoticeIssuedForNotRegistered(){
//        /*
//         * Test Description
//         * Create an Entry and an Exit for a not registered Vehicle
//         * Check that a penalty notice is sent to it
//         */
//
//        // Get the calculated charge for these entry/exit (Necessary because the return is BigDecimal -- Impossible to manipulate)
//        system.vehicleEnteringZone(Vehicle.withRegistration("A234 YYY"));
//        system.vehicleLeavingZone(Vehicle.withRegistration("A234 YYY"));
//        system.getEventLog().get(0).setTimeStamp(1000); //it enters at time 1000
//        system.getEventLog().get(1).setTimeStamp(2000); //it enters at time 2000
//        List<ZoneBoundaryCrossing> crossings = new ArrayList<>();
//        crossings.add(system.getEventLog().get(0));
//        crossings.add(system.getEventLog().get(1));
//        BigDecimal expected_value = calculator.getCalculatedCharge(crossings);
//
//        context.checking(new Expectations() {{
//            exactly(1).of(penaltiesService).issuePenaltyNotice(Vehicle.withRegistration("A234 YYY"), expected_value);
//        }});
//        system.vehicleEnteringZone(Vehicle.withRegistration("A234 YYY"));
//        system.vehicleLeavingZone(Vehicle.withRegistration("A234 YYY"));
//        system.getEventLog().get(0).setTimeStamp(1000); //it enters at time 1000
//        system.getEventLog().get(1).setTimeStamp(2000); //it enters at time 2000
//        system.calculateCharges();
//    }
//
//    @Test
//    public void checkPreviouslyRegistered(){
//        /*
//         * Test Description
//         * Create an Entry for a Vehicle
//         * Check that it is not registered before but registered after
//         */
//        assertFalse(checker.previouslyRegistered(Vehicle.withRegistration("A123 ABC"), system.getEventLog()));
//        system.vehicleEnteringZone(Vehicle.withRegistration("A123 ABC"));
//        assertTrue(checker.previouslyRegistered(Vehicle.withRegistration("A123 ABC"), system.getEventLog()));
//    }
//
//    @Test
//    public void unregisteredCarExitTest(){
//        /*
//         * Test Description
//         * Create an Exit for a Vehicle
//         * Check that it is not registered after
//         */
//        system.vehicleLeavingZone(Vehicle.withRegistration("A123 XYZ"));
//        assertThat(system.getEventLog().size(), is(0));
//        assertFalse(checker.previouslyRegistered(Vehicle.withRegistration("A123 XYZ"), system.getEventLog()));
//    }
//}
