package com.trafficmon;

public interface VehicleInterface {

    @Override
    String toString();

    @Override
    boolean equals(Object o);

    @Override
    int hashCode();

    String getRegistration();
}
