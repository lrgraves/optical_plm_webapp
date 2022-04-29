package com.eleoptics.spark.events;

import com.eleoptics.spark.api.Asset;
import com.eleoptics.spark.eventbus.SparkEvent;

public class DesignPathVersionClickedEvent implements SparkEvent {
    public Asset project;

    public DesignPathVersionClickedEvent(Asset project) {
        this.project = project;
    }
}
