package com.eleoptics.spark.events;

import com.eleoptics.spark.api.Asset;
import com.eleoptics.spark.eventbus.SparkEvent;

import java.util.List;

public class DesignPathDetailsEvent implements SparkEvent {
    public Asset parentProject;

    public Asset getParentProject() {
        return parentProject;
    }

    public void setParentProject(Asset parentProject) {
        this.parentProject = parentProject;
    }

    public DesignPathDetailsEvent(Asset parentProject) {

        this.parentProject = parentProject;
    }
}
