package com.trafficmon;

import java.math.*;

public class Example2 {

    public static void main(String[] args) throws Exception {

        CongestionChargeSystem congestionChargeSystem = new CongestionChargeSystem();
        congestionChargeSystem.vehicleEnteringZone(Vehicle.withRegistration("A123 XYZ"));
        congestionChargeSystem.getEventLogEntries(0).setTimeStamp(1000000);
        congestionChargeSystem.vehicleLeavingZone(Vehicle.withRegistration("A123 XYZ"));
        congestionChargeSystem.getEventLogEntries(1).setTimeStamp(2000000);
        BigDecimal our_value = congestionChargeSystem.getCalculatedCharge(congestionChargeSystem.getEventLogEntries(0),congestionChargeSystem.getEventLogEntries(1));
        MathContext mc = new MathContext(2);
        System.out.println(our_value.round(mc));
        System.out.println("THIS IS OUR VALUE");
        congestionChargeSystem.calculateCharges();
    }

    private static void delayMinutes(int mins) throws InterruptedException {
        delaySeconds(mins * 60);
    }

    private static void delaySeconds(int secs) throws InterruptedException {
        Thread.sleep(secs * 1000);
    }
}

