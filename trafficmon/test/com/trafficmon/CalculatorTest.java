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
        //Test 1
        //make a hashmap of the cars and stuff then send it into calculateCharges
        //expect a call to the checker
        //in this test it will return true and thence expect call to operations team

        List<ZoneBoundaryCrossing> crossingsForVehicle = new ArrayList<>();
        Vehicle vehicle = Vehicle.withRegistration("A123 XYZ");
        crossingsForVehicle.add(new ExitEvent(vehicle));
        crossingsForVehicle.add(new EntryEvent(vehicle));
        //putting the entries in the wrong order

        context.checking(new Expectations() {{
            exactly(1).of(checker).checkOrderingOf(crossingsForVehicle);will(returnValue(false));
            exactly(1).of(operationsTeam).triggerInvestigationInto(vehicle);
        }});

        Map<Vehicle, List<ZoneBoundaryCrossing>> crossingsByVehicles = new HashMap<>();
        crossingsByVehicles.put(vehicle, crossingsForVehicle);
        calculator.calculateCharges(crossingsByVehicles);

    }


    //Test 2
    //make a hashmap of the cars and stuff then send it into calculateCharges
    //expect a call to the checker
    //in this test it will return false and thence expect a call to charge_account
    @Test
    public void checkCalculateChargesWithCrossingsOrdered() {

        List<ZoneBoundaryCrossing> crossingsForVehicle = new ArrayList<>();
        Vehicle vehicle = Vehicle.withRegistration("A123 XYZ");
        crossingsForVehicle.add(new EntryEvent(vehicle));
        crossingsForVehicle.add(new ExitEvent(vehicle));

        context.checking(new Expectations() {{
            exactly(1).of(checker).checkOrderingOf(crossingsForVehicle);will(returnValue(true));
        }});

        Map<Vehicle, List<ZoneBoundaryCrossing>> crossingsByVehicles = new HashMap<>();
        crossingsByVehicles.put(vehicle, crossingsForVehicle);
        calculator.calculateCharges(crossingsByVehicles);

    }

    //test3
    //call the function charge_account
    //pass into charge account a vehicle and a ridiculous charge that will make the account go negative
    //thence expect a call like  accountsService.accountFor(vehicle).deduct(charge)
    // and also expect a call like operationsTeam.issuePenaltyNotice(vehicle, charge);

    @Test
    public void checkChargeAccountForInsufficientFunds() {
        Vehicle vehicle = Vehicle.withRegistration("A123 XYZ");
        BigDecimal ridiculous_charge = new BigDecimal(10000);

        context.checking(new Expectations() {{
            exactly(1).of(operationsTeam).issuePenaltyNotice(vehicle,ridiculous_charge);
        }});
        calculator.charge_account(vehicle, ridiculous_charge);
    }



    //test4
    //call the function charge_account
    //pass into charge account a vehicle not been registered and a charge of 0
    //thence expect a call like  accountsService.accountFor(vehicle).deduct(charge)
    // and also expect a call like operationsTeam.issuePenaltyNotice(vehicle, charge);

    @Test
    public void checkChargeAccountForNotRegistered() {
        Vehicle vehicle = Vehicle.withRegistration("A123 XYZZ");
        BigDecimal charge = new BigDecimal(10000);

        context.checking(new Expectations() {{
            exactly(1).of(operationsTeam).issuePenaltyNotice(vehicle,charge);
        }});
        calculator.charge_account(vehicle, charge);
    }



    //test5
    //call the function charge_account
    //pass into charge account a vehicle that has been registered and a charge of 0
    //thence expect a call like  accountsService.accountFor(vehicle).deduct(charge)
    //and also DO NOT expect a call like operationsTeam.issuePenaltyNotice(vehicle, charge);

    @Test
    public void checkChargeAccountForNoExceptions() {

        Vehicle vehicle = Vehicle.withRegistration("A123 XYZ");
        BigDecimal charge = new BigDecimal(0);
        context.checking(new Expectations() {{
            exactly(0).of(operationsTeam).issuePenaltyNotice(vehicle,charge);
        }});

        calculator.charge_account(vehicle,charge);

    }

    //test 6
    //call the function getCharge with an arraylist of events following these rules:
    //1) less than 4 hours time in
    //2) before 2pm
    //assert that the returned value is £6

    @Test
    public void checkChargesForEntryBefore2LessThan4Hours(){
        List<ZoneBoundaryCrossing> crossingsForVehicle = new ArrayList<>();
        Vehicle vehicle = Vehicle.withRegistration("A123 XYZ");
        crossingsForVehicle.add((new EntryEvent(vehicle))); // Adding an entry
        crossingsForVehicle.get(0).setTimeStamp(50000);
        crossingsForVehicle.add((new ExitEvent(vehicle))); // Adding an entry
        crossingsForVehicle.get(1).setTimeStamp(54000);

        assertThat(calculator.getCalculatedCharge(crossingsForVehicle), is(new BigDecimal(6)));
    }

    //test 7
    //call the function getCharge with an arraylist of events following these rules:
    //1) more than 4 hours time in
    //2) before 2pm
    //assert that the returned value is £12

    @Test
    public void checkChargesForEntryBefore2MoreThan4hours(){
        List<ZoneBoundaryCrossing> crossingsForVehicle = new ArrayList<>();
        Vehicle vehicle = Vehicle.withRegistration("A123 XYZ");
        crossingsForVehicle.add((new EntryEvent(vehicle))); // Adding an entry
        crossingsForVehicle.get(0).setTimeStamp(46800);
        crossingsForVehicle.add((new ExitEvent(vehicle))); // Adding an entry
        crossingsForVehicle.get(1).setTimeStamp(64800);

        assertThat(calculator.getCalculatedCharge(crossingsForVehicle), is(new BigDecimal(12)));
    }

    //test 8
    //call the function getCharge with an arraylist of events following these rules:
    //1) less than 4 hours time in
    //2) after 2pm
    //assert that the returned value is £4

    @Test
    public void checkChargesForEntryAfter2LessThan4hours(){
        List<ZoneBoundaryCrossing> crossingsForVehicle = new ArrayList<>();
        Vehicle vehicle = Vehicle.withRegistration("A123 XYZ");
        crossingsForVehicle.add((new EntryEvent(vehicle))); // Adding an entry
        crossingsForVehicle.get(0).setTimeStamp(50500);
        crossingsForVehicle.add((new ExitEvent(vehicle))); // Adding an entry
        crossingsForVehicle.get(1).setTimeStamp(54000);

        assertThat(calculator.getCalculatedCharge(crossingsForVehicle), is(new BigDecimal(4)));
    }

    //test 9
    //call the function getCharge with an arraylist of events following these rules:
    //1) more than 4 hours time in
    //2) after 2pm
    //assert that the returned value is £12

    @Test
    public void checkChargesForEntryAfter2MoreThan4hours(){
        List<ZoneBoundaryCrossing> crossingsForVehicle = new ArrayList<>();
        Vehicle vehicle = Vehicle.withRegistration("A123 XYZ");
        crossingsForVehicle.add((new EntryEvent(vehicle))); // Adding an entry
        crossingsForVehicle.get(0).setTimeStamp(50500);
        crossingsForVehicle.add((new ExitEvent(vehicle))); // Adding an entry
        crossingsForVehicle.get(1).setTimeStamp(68400);

        assertThat(calculator.getCalculatedCharge(crossingsForVehicle), is(new BigDecimal(12)));
    }

    //test 10
    //call the function getCharge with an arraylist of events following these rules:
    //1) less than 4 hours time in
    //2) first entry before 2pm , exits , second entry after 2pm
    //assert that the returned value is £6

    @Test
    public void checkChargesForDoubleEntryBeforeandAfter2LessThan4hours(){
        List<ZoneBoundaryCrossing> crossingsForVehicle = new ArrayList<>();
        Vehicle vehicle = Vehicle.withRegistration("A123 XYZ");
        crossingsForVehicle.add((new EntryEvent(vehicle))); // Adding an entry
        crossingsForVehicle.get(0).setTimeStamp(46800);
        crossingsForVehicle.add((new ExitEvent(vehicle))); // Adding an entry
        crossingsForVehicle.get(1).setTimeStamp(50000);
        crossingsForVehicle.add((new EntryEvent(vehicle))); // Adding an entry
        crossingsForVehicle.get(2).setTimeStamp(54000);
        crossingsForVehicle.add((new ExitEvent(vehicle))); // Adding an entry
        crossingsForVehicle.get(3).setTimeStamp(56000);

        assertThat(calculator.getCalculatedCharge(crossingsForVehicle), is(new BigDecimal(6)));
    }

    //test 11
    //call the function getCharge with an arraylist of events following these rules:
    //1) more than 4 hours time in
    //2) first entry before 2pm , exits , second entry after 2pm
    //assert that the returned value is £12

    @Test
    public void checkChargesForDoubleEntryBeforeAndAfter2StayingForMoreThan4hours(){
        List<ZoneBoundaryCrossing> crossingsForVehicle = new ArrayList<>();
        Vehicle vehicle = Vehicle.withRegistration("A123 XYZ");
        crossingsForVehicle.add((new EntryEvent(vehicle))); // Adding an entry
        crossingsForVehicle.get(0).setTimeStamp(46800);
        crossingsForVehicle.add((new ExitEvent(vehicle))); // Adding an entry
        crossingsForVehicle.get(1).setTimeStamp(50000);
        crossingsForVehicle.add((new EntryEvent(vehicle))); // Adding an entry
        crossingsForVehicle.get(2).setTimeStamp(54000);
        crossingsForVehicle.add((new ExitEvent(vehicle))); // Adding an entry
        crossingsForVehicle.get(3).setTimeStamp(69000);

        assertThat(calculator.getCalculatedCharge(crossingsForVehicle), is(new BigDecimal(12)));
    }
























}
