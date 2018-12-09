package com.trafficmon;

import org.junit.Rule;
import org.junit.Test;
import java.math.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.jmock.Expectations;
import org.jmock.integration.junit4.JUnitRuleMockery;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

public class CalculatorTest {

    @Rule
    public JUnitRuleMockery context = new JUnitRuleMockery();
    private CheckerInterface checker = context.mock(CheckerInterface.class);
    private PenaltiesService operationsTeam = context.mock(PenaltiesService.class);
    private Calculator calculator = new Calculator(checker,operationsTeam);

    @Test
    public void checkCalculateChargesWithCrossingsThatAreNotOrdered() {
        /*
         * Test Description
         * Pass a HashMap with not ordered events to calculateCharges()
         * We expect a call to the checker and a call to Operations Team
         */
        List<ZoneBoundaryCrossing> crossingsForVehicle = new ArrayList<>();
        VehicleInterface vehicle = Vehicle.withRegistration("A123 XYZ");
        crossingsForVehicle.add(new ExitEvent(vehicle)); // Putting the events in the wrong order
        crossingsForVehicle.add(new EntryEvent(vehicle));
        Map<VehicleInterface, List<ZoneBoundaryCrossing>> crossingsByVehicles = new HashMap<>();
        crossingsByVehicles.put(vehicle, crossingsForVehicle);

        context.checking(new Expectations() {{
            exactly(1).of(checker).checkOrderingOf(crossingsForVehicle);will(returnValue(false));
            exactly(1).of(operationsTeam).triggerInvestigationInto((Vehicle) vehicle);
        }});

        calculator.calculateCharges(crossingsByVehicles);
    }

    @Test
    public void checkCalculateChargesWithCrossingsOrdered() {
        /*
         * Test Description
         * Pass a HashMap with ordered events to calculateCharges()
         * We expect a call to the checker
         */

        List<ZoneBoundaryCrossing> crossingsForVehicle = new ArrayList<>();
        VehicleInterface vehicle = Vehicle.withRegistration("A123 XYZ");
        crossingsForVehicle.add(new EntryEvent(vehicle));
        crossingsForVehicle.add(new ExitEvent(vehicle));
        Map<VehicleInterface, List<ZoneBoundaryCrossing>> crossingsByVehicles = new HashMap<>();
        crossingsByVehicles.put(vehicle, crossingsForVehicle);

        context.checking(new Expectations() {{
            exactly(1).of(checker).checkOrderingOf(crossingsForVehicle);will(returnValue(true));
        }});

        calculator.calculateCharges(crossingsByVehicles);
    }

    @Test
    public void checkChargeAccountForInsufficientFunds() {
        /*
         * Test Description
         * Pass a vehicle and a very big charge to chargeAccount()
         * We expect a call to Operations Team for insufficient funds
         */
        VehicleInterface vehicle = Vehicle.withRegistration("A123 XYZ");
        BigDecimal ridiculous_charge = new BigDecimal(10000); // Creating a very expensive charge

        context.checking(new Expectations() {{
            exactly(1).of(operationsTeam).issuePenaltyNotice((Vehicle) vehicle,ridiculous_charge);
        }});

        calculator.charge_account(vehicle, ridiculous_charge);
    }

    @Test
    public void checkChargeAccountForNotRegistered() {
        /*
         * Test Description
         * Pass a not registered vehicle and a charge to chargeAccount()
         * We expect a call to Operations Team for not registered vehicle
         */
        VehicleInterface vehicle = Vehicle.withRegistration("A123 XYZZ");
        BigDecimal charge = new BigDecimal(10);

        context.checking(new Expectations() {{
            exactly(1).of(operationsTeam).issuePenaltyNotice((Vehicle) vehicle,charge);
        }});

        calculator.charge_account(vehicle, charge);
    }

    @Test
    public void checkChargeAccountForNoExceptions() {
        /*
         * Test Description
         * Pass a vehicle and a charge to chargeAccount()
         * We expect a nothing to happen (just the driver charged normally)
         */
        VehicleInterface vehicle = Vehicle.withRegistration("A123 XYZ");
        BigDecimal charge = new BigDecimal(0);

        context.checking(new Expectations() {{
            exactly(0).of(operationsTeam).issuePenaltyNotice((Vehicle) vehicle,charge);
        }});

        calculator.charge_account(vehicle,charge);
    }

    @Test
    public void checkChargesForEntryBefore2LessThan4Hours(){
        /*
         * Test Description
         * Pass a list of events to getCharge()
         * We assert that the returned charge is correct
         */
        List<ZoneBoundaryCrossing> crossingsForVehicle = new ArrayList<>();
        VehicleInterface vehicle = Vehicle.withRegistration("A123 XYZ");
        crossingsForVehicle.add((new EntryEvent(vehicle))); // Adding an entry
        crossingsForVehicle.get(0).setTimeStamp(13*3600);
        crossingsForVehicle.add((new ExitEvent(vehicle))); // Adding an exit
        crossingsForVehicle.get(1).setTimeStamp(15*3600);

        assertThat(calculator.getCharge(crossingsForVehicle), is(new BigDecimal(6)));
    }

    @Test
    public void checkChargesForEntryBefore2MoreThan4hours(){
        /*
         * Test Description
         * Pass a list of events to getCharge()
         * We assert that the returned charge is correct
         */
        List<ZoneBoundaryCrossing> crossingsForVehicle = new ArrayList<>();
        VehicleInterface vehicle = Vehicle.withRegistration("A123 XYZ");
        crossingsForVehicle.add((new EntryEvent(vehicle))); // Adding an entry
        crossingsForVehicle.get(0).setTimeStamp(13*3600);
        crossingsForVehicle.add((new ExitEvent(vehicle))); // Adding an exit
        crossingsForVehicle.get(1).setTimeStamp(18*3600);

        assertThat(calculator.getCharge(crossingsForVehicle), is(new BigDecimal(12)));
    }

    @Test
    public void checkChargesForEntryAfter2LessThan4hours(){
        /*
         * Test Description
         * Pass a list of events to getCharge()
         * We assert that the returned charge is correct
         */
        List<ZoneBoundaryCrossing> crossingsForVehicle = new ArrayList<>();
        VehicleInterface vehicle = Vehicle.withRegistration("A123 XYZ");
        crossingsForVehicle.add((new EntryEvent(vehicle))); // Adding an entry
        crossingsForVehicle.get(0).setTimeStamp(15*3600);
        crossingsForVehicle.add((new ExitEvent(vehicle))); // Adding an exit
        crossingsForVehicle.get(1).setTimeStamp(17*3600);

        assertThat(calculator.getCharge(crossingsForVehicle), is(new BigDecimal(4)));
    }

    @Test
    public void checkChargesForEntryAfter2MoreThan4hours(){
        /*
         * Test Description
         * Pass a list of events to getCharge()
         * We assert that the returned charge is correct
         */
        List<ZoneBoundaryCrossing> crossingsForVehicle = new ArrayList<>();
        VehicleInterface vehicle = Vehicle.withRegistration("A123 XYZ");
        crossingsForVehicle.add((new EntryEvent(vehicle))); // Adding an entry
        crossingsForVehicle.get(0).setTimeStamp(15*3600);
        crossingsForVehicle.add((new ExitEvent(vehicle))); // Adding an exit
        crossingsForVehicle.get(1).setTimeStamp(20*3600);

        assertThat(calculator.getCharge(crossingsForVehicle), is(new BigDecimal(12)));
    }

    @Test
    public void checkChargesForDoubleEntryBeforeAndAfter2LessThan4hours(){
        /*
         * Test Description
         * Pass a list of events to getCharge()
         * We assert that the returned charge is correct
         */
        List<ZoneBoundaryCrossing> crossingsForVehicle = new ArrayList<>();
        VehicleInterface vehicle = Vehicle.withRegistration("A123 XYZ");
        crossingsForVehicle.add((new EntryEvent(vehicle))); // Adding an entry
        crossingsForVehicle.get(0).setTimeStamp(12*3600);
        crossingsForVehicle.add((new ExitEvent(vehicle))); // Adding an exit
        crossingsForVehicle.get(1).setTimeStamp(13*3600);
        crossingsForVehicle.add((new EntryEvent(vehicle))); // Adding an entry
        crossingsForVehicle.get(2).setTimeStamp(15*3600);
        crossingsForVehicle.add((new ExitEvent(vehicle))); // Adding an exit
        crossingsForVehicle.get(3).setTimeStamp(16*3600);

        assertThat(calculator.getCharge(crossingsForVehicle), is(new BigDecimal(6)));
    }

    @Test
    public void checkChargesForDoubleEntryBeforeAndAfter2StayingForMoreThan4hours(){
        /*
         * Test Description
         * Pass a list of events to getCharge()
         * We assert that the returned charge is correct
         */
        List<ZoneBoundaryCrossing> crossingsForVehicle = new ArrayList<>();
        VehicleInterface vehicle = Vehicle.withRegistration("A123 XYZ");
        crossingsForVehicle.add((new EntryEvent(vehicle))); // Adding an entry
        crossingsForVehicle.get(0).setTimeStamp(12*3600);
        crossingsForVehicle.add((new ExitEvent(vehicle))); // Adding an exit
        crossingsForVehicle.get(1).setTimeStamp(14*3600);
        crossingsForVehicle.add((new EntryEvent(vehicle))); // Adding an entry
        crossingsForVehicle.get(2).setTimeStamp(15*3600);
        crossingsForVehicle.add((new ExitEvent(vehicle))); // Adding an exit
        crossingsForVehicle.get(3).setTimeStamp(18*3600);

        assertThat(calculator.getCharge(crossingsForVehicle), is(new BigDecimal(12)));
    }

    @Test
    public void checkChargesForExtremeScenario() {
        /*
         * Test Description
         * Pass a list of events to getCharge()
         * We assert that the returned charge is correct
         */
        List<ZoneBoundaryCrossing> crossingsForVehicle = new ArrayList<>();
        VehicleInterface vehicle = Vehicle.withRegistration("A123 XYZ");
        crossingsForVehicle.add((new EntryEvent(vehicle))); // Adding an entry
        crossingsForVehicle.get(0).setTimeStamp(0);
        crossingsForVehicle.add((new ExitEvent(vehicle))); // Adding an exit
        crossingsForVehicle.get(1).setTimeStamp(60);
        crossingsForVehicle.add((new EntryEvent(vehicle))); // Adding an entry
        crossingsForVehicle.get(2).setTimeStamp(5*3600);
        crossingsForVehicle.add((new ExitEvent(vehicle))); // Adding an exit
        crossingsForVehicle.get(3).setTimeStamp(5*3600+1);
        crossingsForVehicle.add((new EntryEvent(vehicle))); // Adding an entry
        crossingsForVehicle.get(4).setTimeStamp(10*3600);
        crossingsForVehicle.add((new ExitEvent(vehicle))); // Adding an exit
        crossingsForVehicle.get(5).setTimeStamp(10*3600+1);

        assertThat(calculator.getCharge(crossingsForVehicle), is(new BigDecimal(18)));
    }
}
