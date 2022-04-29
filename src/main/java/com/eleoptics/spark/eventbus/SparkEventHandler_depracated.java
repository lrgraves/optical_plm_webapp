package com.eleoptics.spark.eventbus;

import java.util.function.Consumer;

public class SparkEventHandler_depracated {
     public Class<?> eventType;
     public Consumer<SparkEvent> handler;

    public SparkEventHandler_depracated(Class<?> eventType, Consumer<SparkEvent> handler) {
        this.eventType = eventType;
        this.handler = handler;
    }
}
