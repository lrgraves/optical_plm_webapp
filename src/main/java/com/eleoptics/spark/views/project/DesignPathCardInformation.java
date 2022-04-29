package com.eleoptics.spark.views.project;

import com.eleoptics.spark.api.Asset;

import java.util.List;

public class DesignPathCardInformation {
    public List<Asset> assetList;
    public Asset parentProject;

    public DesignPathCardInformation(List<Asset> assetList, Asset parentProject) {
        this.assetList = assetList;
        this.parentProject = parentProject;
    }
}
