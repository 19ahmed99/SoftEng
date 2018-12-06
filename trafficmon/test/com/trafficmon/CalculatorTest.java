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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class CalculatorTest {

    @Rule
    public JUnitRuleMockery context = new JUnitRuleMockery();
    private CheckerInterface checker = context.mock(CheckerInterface.class);
    private CongestionChargeSystemInterface system = context.mock(CongestionChargeSystemInterface.class);
    private Calculator calculator = new Calculator(checker);
    private PenaltiesService operationsTeam = context.mock(PenaltiesService.class);
    private AccountsService accountsService = context.mock(AccountsService.class);

    //Test 1
    //make a hashmap of the cars and stuff then send it into calculateCharges
    //expect a call to the checker
    //in this test it will return true and thence expect call to operations team

    @Test
    public void checkCalculateChargesWithCrossingsNotOrdered() {

        List<ZoneBoundaryCrossing> crossingsForVehicle = new ArrayList<>();
        Vehicle vehicle = Vehicle.withRegistration("A123 XYZ");
        crossingsForVehicle.add(new ExitEvent(vehicle));
        crossingsForVehicle.add(new EntryEvent(vehicle));
        //putting the entries in the wrong order

        context.checking(new Expectations() {{
            exactly(1).of(checker).checkOrderingOf(crossingsForVehicle);will(returnValue(false));
            //exactly(1).of(operationsTeam).triggerInvestigationInto(vehicle); ----------- why is this not working
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
        //entries in the correct order

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

//    @Test
//    public void checkChargeAccountForInsufficientFunds() throws AccountNotRegisteredException, InsufficientCreditException {
//        Vehicle vehicle = Vehicle.withRegistration("A123 XYZ");
//        BigDecimal ridiculous_charge = new BigDecimal(10000);
//
//        context.checking(new Expectations() {{
//            allowing (accountsService).accountFor(vehicle).deduct(ridiculous_charge);
//            will(throwException(new InsufficientCreditException());
//            //look at how to handle exceptions in Mockery
//        }});
//
//        calculator.charge_account(vehicle, ridiculous_charge);
//
//    }



    //test4
    //call the function charge_account
    //pass into charge account a vehicle not been registered and a charge of 0
    //thence expect a call like  accountsService.accountFor(vehicle).deduct(charge)
    // and also expect a call like operationsTeam.issuePenaltyNotice(vehicle, charge);

//    @Test
//    public void checkChargeAccountForInsufficientFunds() {
//        Vehicle vehicle = Vehicle.withRegistration("ZCAB FEL");
//        BigDecimal charge = new BigDecimal(10000);
//
//        context.checking(new Expectations() {{
//            exactly(1).of(accountsService).accountFor(vehicle).deduct(ridiculous_charge);will(return Exception)
//            //look at how to handle exceptions in Mockery
//        }});
//
//        calculator.charge_account(vehicle, ridiculous_charge);
//
//    }

    //test5
    //call the function charge_account
    //pass into charge account a vehicle that has been registered and a charge of 0
    //thence expect a call like  accountsService.accountFor(vehicle).deduct(charge)
    //and also DO NOT expect a call like operationsTeam.issuePenaltyNotice(vehicle, charge);

    //test 6
    //call the function getCharge with an arraylist of events following these rules:
    //1) less than 4 hours time in
    //2) before 2pm
    //assert that the returned value is £6

    //test 7
    //call the function getCharge with an arraylist of events following these rules:
    //1) more than 4 hours time in
    //2) before 2pm
    //assert that the returned value is £12

    //test 8
    //call the function getCharge with an arraylist of events following these rules:
    //1) less than 4 hours time in
    //2) after 2pm
    //assert that the returned value is £4

    //test 9
    //call the function getCharge with an arraylist of events following these rules:
    //1) more than 4 hours time in
    //2) after 2pm
    //assert that the returned value is £12

    //test 10
    //call the function getCharge with an arraylist of events following these rules:
    //1) less than 4 hours time in
    //2) first entry before 2pm , exits , second entry after 2pm
    //assert that the returned value is £6

    //test 11
    //call the function getCharge with an arraylist of events following these rules:
    //1) more than 4 hours time in
    //2) first entry before 2pm , exits , second entry after 2pm
    //assert that the returned value is £12








}
