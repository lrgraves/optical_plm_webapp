package com.eleoptics.spark.events;

import com.eleoptics.spark.api.Asset;
import com.eleoptics.spark.eventbus.SparkEvent;

public class NewDesignPathClickedEvent implements SparkEvent {
    public Asset project;

    public NewDesignPathClickedEvent(Asset project) {
        this.project = project;
    }
}
