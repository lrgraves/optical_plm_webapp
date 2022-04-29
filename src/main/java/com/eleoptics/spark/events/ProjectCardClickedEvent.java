package com.eleoptics.spark.events;

import com.eleoptics.spark.api.Asset;
import com.eleoptics.spark.eventbus.SparkEvent;

public class ProjectCardClickedEvent implements SparkEvent {
    public Asset project;

    public Asset getProject() {
        return project;
    }

    public ProjectCardClickedEvent(Asset project) {
        this.project = project;
    }
}
