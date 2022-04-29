package com.eleoptics.spark.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.vaadin.flow.component.page.Meta;

import java.util.HashMap;
import java.util.List;

public class AssetObjectForUpdate {
    @JsonProperty("id")
    public String key;
    @JsonProperty("parent_id")
    public String parentKey;
    @JsonProperty("name")
    public String assetName;
    @JsonProperty("asset_type")
    public String assetType;
    @JsonProperty("tags")
    public List<String> tags;
    @JsonProperty("metadata")
    public HashMap<String, String> metadata;
    @JsonProperty("is_public")
    public Boolean isPublic;


    @Override
    public String toString() {
        return "AssetObjectForUpdate{" +
                "key='" + key + '\'' +
                ", parentKey='" + parentKey + '\'' +
                ", assetName='" + assetName + '\'' +
                ", assetType='" + assetType + '\'' +
                ", tags=" + tags +
                ", metadata=" + metadata +
                ", isPublic=" + isPublic +
                ", description='" + description + '\'' +
                '}';
    }

    public AssetObjectForUpdate(String key, String parentKey, String assetName,
                                String assetType, List<String> tags, String description,
                                HashMap<String, String> metadata) {
        this.key = key;
        this.parentKey = parentKey;
        this.assetName = assetName;
        this.assetType = assetType;
        this.tags = tags;
        this.metadata = metadata;
        this.description = description;
    }

    public AssetObjectForUpdate(String key, String parentKey, String assetName, String assetType, List<String> tags, String description, Metadata metadata) {
        this.key = key;
        this.parentKey = parentKey;
        this.assetName = assetName;
        this.assetType = assetType;
        this.tags = tags;
        this.description = description;
    }

    @JsonProperty("description")
    public String description;


    public void setMetadata(HashMap<String, String> metadata) {
        this.metadata = metadata;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getKey() {
        return key;
    }

    public AssetObjectForUpdate(String key, String parentKey, String assetName, String assetType, List<String> tags, String description) {
        this.key = key;
        this.parentKey = parentKey;
        this.assetName = assetName;
        this.assetType = assetType;
        this.tags = tags;
        this.description = description;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getDescription() {
        return description;
    }

    public AssetObjectForUpdate() {

    }

    public Boolean getPublic() {
        return isPublic;
    }

    public void setPublic(Boolean aPublic) {
        isPublic = aPublic;
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
