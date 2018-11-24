package com.trafficmon;

import java.math.*;

public class Example2 {

    public static void main(String[] args) throws Exception {

        CongestionChargeSystem congestionChargeSystem = new CongestionChargeSystem();
        congestionChargeSystem.vehicleEnteringZone(Vehicle.withRegistration("A123 XYZ"));
        congestionChargeSystem.vehicleLeavingZone(Vehicle.withRegistration("A123 XYZ"));
        BigDecimal our_value = congestionChargeSystem.getCalculatedCharge(congestionChargeSystem.getEventLogEntries(0),congestionChargeSystem.getEventLogEntries(1));
        System.out.println(congestionChargeSystem.getEventLogEntries(0).timestamp());
        System.out.println(congestionChargeSystem.getEventLogEntries(1).timestamp());
        congestionChargeSystem.calculateCharges();
    }

    private static void delayMinutes(int mins) throws InterruptedException {
        delaySeconds(mins * 60);
    }

    private static void delaySeconds(int secs) throws InterruptedException {
        Thread.sleep(secs * 1000);
    }
}

