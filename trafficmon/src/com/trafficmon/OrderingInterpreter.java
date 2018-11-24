package com.trafficmon;

public interface OrderingInterpreter {
    void timestamp_error();
    void doubleEntry_error();
    void doubleExit_error();
    void perfect_ordering();
}
