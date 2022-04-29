package com.eleoptics.spark.api;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class DesignPathForUpload {

    public DesignPathForUpload() {

    }

    @JsonProperty("parent_id")
    public String parentID;
    @JsonProperty("asset_name")
    public String assetName;
    @JsonProperty("asset_type")
    public String assetType;
    @JsonProperty("tags")
    public List<String> tags;
    public String description;

    public String getParentID() {
        return parentID;
    }

    public void setParentID(String parentID) {
        this.parentID = parentID;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getAssetName() {
        return assetName;
    }

    public void setAssetName(String assetName) {
        this.assetName = assetName;
    }

    public String getAssetType() {
        return assetType;
    }

    public void setAssetType(String assetType) {
        this.assetType = assetType;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }


}
