package com.trafficmon;

import org.junit.Test;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class VehicleTest {

    @Test
    public void checkToString() {
        /*
         * Test Description
         * Create a Vehicle
         * Check the toString() method in the class
         */
        Vehicle vehicle = Vehicle.withRegistration("A123 XYZ");
        assertThat(vehicle.toString(), is("Vehicle [A123 XYZ]"));
    }

    @Test
    public void checkEqualsMethod(){
        /*
         * Test Description
         * Create three Vehicles
         * Check the equals() method in the class
         */
        Vehicle vehicle1 = Vehicle.withRegistration("A123 XYZ");
        Vehicle vehicle2 = Vehicle.withRegistration("A123 ABC");
        Vehicle vehicle3 = Vehicle.withRegistration("A123 XYZ");

        assertTrue(vehicle1.equals(vehicle1)); // Same object reference
        assertFalse(vehicle1.equals(vehicle2)); // Different registrations and different object references
        assertTrue(vehicle1.equals(vehicle3)); // Different object reference but same registration
    }

    @Test
    public void checkHashCode(){
        /*
         * Test Description
         * Create two Vehicles
         * Check the hashCode() method in the class
         */
        Vehicle vehicle1 = Vehicle.withRegistration(null);
        assertTrue(vehicle1.hashCode() == 0);
        Vehicle vehicle2 = Vehicle.withRegistration("A123 XYZ");
        assertTrue(vehicle2.hashCode() == vehicle2.getRegistration().hashCode());
    }
}