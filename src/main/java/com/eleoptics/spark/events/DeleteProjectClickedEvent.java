package com.eleoptics.spark.events;

import com.eleoptics.spark.api.Asset;
import com.eleoptics.spark.eventbus.SparkEvent;

public class DeleteProjectClickedEvent implements SparkEvent {
    public Asset project;

    public DeleteProjectClickedEvent(Asset project) {
        this.project = project;
    }
}
