package com.eleoptics.spark.events;

import com.eleoptics.spark.api.Asset;
import com.eleoptics.spark.eventbus.SparkEvent;

public class DesignPathCardClickedEvent implements SparkEvent {
    public Asset project;

    public DesignPathCardClickedEvent(Asset project) {
        this.project = project;
    }
}
