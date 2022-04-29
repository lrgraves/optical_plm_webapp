package com.eleoptics.spark.api;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.HashMap;
import java.util.List;

public class AssetObjectForUpload {
    @JsonProperty("parent_id")
    public String parentKey;
    @JsonProperty("asset_name")
    public String assetName;
    @JsonProperty("asset_type")
    public String assetType;
    @JsonProperty("tags")
    public List<String> tags;
    @JsonProperty("description")
    public String description;
    @JsonProperty("metadata")
    public HashMap<String, Object> metadata; // For isaac, should this be a map in map with metadata:{Messages:{adfadsf}} ?

    public String getAssetDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        return "AssetObjectForUpload{" +
                "parentKey='" + parentKey + '\'' +
                ", assetName='" + assetName + '\'' +
                ", assetType='" + assetType + '\'' +
                ", tags=" + tags +
                ", description='" + description + '\'' +
                ", metadata=" + metadata +
                '}';
    }

    public HashMap<String, Object> getMetadata() {
        return metadata;
    }

    public void setMetadata(HashMap<String, Object> metadata) {
        this.metadata = metadata;
    }

    public String getDescription() {
        return description;
    }

    public AssetObjectForUpload() {

    }


    public String getParentKey() {
        return parentKey;
    }

    public void setParentKey(String parentKey) {
        this.parentKey = parentKey;
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
