package com.eleoptics.spark.events;

import com.eleoptics.spark.api.Asset;
import com.eleoptics.spark.eventbus.SparkEvent;

public class DownloadAllFilesClickedEvent implements SparkEvent {
    public Asset project;

    public DownloadAllFilesClickedEvent(Asset project) {
        this.project = project;
    }
}
