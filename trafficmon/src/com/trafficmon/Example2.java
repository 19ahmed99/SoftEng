package com.trafficmon;

public class Example2 {

    public static void main(String[] args) throws Exception {

        CongestionChargeSystem congestionChargeSystem = new CongestionChargeSystem();
        congestionChargeSystem.vehicleEnteringZone(Vehicle.withRegistration("A123 XYY"));
        delaySeconds(1);
        congestionChargeSystem.vehicleLeavingZone(Vehicle.withRegistration("A123 XYY"));
        congestionChargeSystem.calculateCharges();
    }

    private static void delayMinutes(int mins) throws InterruptedException {
        delaySeconds(mins * 60);
    }

    private static void delaySeconds(int secs) throws InterruptedException {
        Thread.sleep(secs * 1000);
    }
}

