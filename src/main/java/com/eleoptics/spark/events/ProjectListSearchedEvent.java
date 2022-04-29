package com.eleoptics.spark.events;

import com.eleoptics.spark.api.Asset;
import com.eleoptics.spark.eventbus.SparkEvent;

import java.util.List;

public class ProjectListSearchedEvent implements SparkEvent {
    public List<Asset> searchedAndFilteredList;
            public ProjectListSearchedEvent(List<Asset> filteredAssets){
                searchedAndFilteredList = filteredAssets;
            }
}
