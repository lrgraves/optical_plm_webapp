package com.eleoptics.spark.events;

import com.eleoptics.spark.api.Asset;
import com.eleoptics.spark.eventbus.SparkEvent;

public class DesignPathsEvent implements SparkEvent {
    public Asset parentProject;

    public DesignPathsEvent(Asset project) {
        this.parentProject = project;
    }
}
