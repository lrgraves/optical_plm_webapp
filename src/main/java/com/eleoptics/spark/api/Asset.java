package com.eleoptics.spark.api;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.extern.slf4j.Slf4j;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Slf4j
public class Asset implements Serializable {

    class ProjectData{
        HashMap<RequirementType, String> projectRequirements;
        List<String> assetList;
        String dueDate;
        String assignedTo;
        String focalLength;
        String totalLength;
        String fieldOfView;
        String wavelengthBand;
        String entrancePupilDia;
        String imageNumericalAperture;
        String objectNumericalAperture;
        String fNumber;
        String workingFnumber;
    }
    class AssetData{
        String fileName;
        String fileExtension;
        String fileData;
    }



    public String key;
    @JsonProperty("parent_key")
    public String parentKey;
    @JsonProperty("asset_name")
    public String assetName;
    @JsonProperty("asset_type")
    public String assetType;
    @JsonProperty("created_at")
    public String createdAt;
    @JsonProperty("last_modified")
    public String lastModified;
    @JsonProperty("metadata")
    public Metadata metadata;
    public List<String> tags;
    public Integer version;
    public HashMap<String, HashMap<String, Object>> data;
    public String description;
    public AssetData assetData;
    public ProjectData projectData;
    @JsonProperty("is_public")
    public Boolean isPublic;

    @Override
    public String toString() {
        return "Asset{" +
                "key='" + key + '\'' +
                ", parentKey='" + parentKey + '\'' +
                ", assetName='" + assetName + '\'' +
                ", assetType='" + assetType + '\'' +
                ", createdAt='" + createdAt + '\'' +
                ", lastModified='" + lastModified + '\'' +
                ", metadata=" + metadata +
                ", tags=" + tags +
                ", version=" + version +
                ", data=" + data +
                ", description='" + description + '\'' +
                ", assetData=" + assetData +
                ", projectData=" + projectData +
                ", isPublic=" + isPublic +
                ", formatter=" + formatter +
                '}';
    }

    public Asset() {
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getParentKey() {
        return parentKey;
    }

    public void setParentId(String parentKey) {
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

    public LocalDateTime getCreatedAt() {
        LocalDateTime dateTime = LocalDateTime.parse(createdAt);

        return dateTime;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    @JsonIgnore
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm MM/dd/yyyy");

    public String getLastModifiedFormatted() {
        String dateTime = LocalDateTime.parse(lastModified).format(formatter);

        return dateTime;
    }

    public LocalDateTime getLastModified() {
        LocalDateTime dateTime = LocalDateTime.parse(lastModified);

        return dateTime;
    }

    public void setLastModified(String lastModified) {
        this.lastModified = lastModified;
    }

    public Metadata getMetadata() {
        return metadata;
    }

    public void setMetadata(Metadata metadata) {
        this.metadata = metadata;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public static List<Asset> getAllProjects(List<Asset> assetList) {
        List<Asset> projectList = new ArrayList<>();
        for (int i = 0; i < assetList.size(); i++) {
            if(assetList.get(i).getAssetType().equals("Project")){
                projectList.add(createProject(assetList.get(i)));
            }
        }
        return projectList;
    }

    public static List<Asset> getTopLevelProjects(List<Asset> assetList){
        List<Asset> projectList = getAllProjects(assetList);

        // Get all keys that have children with matching parent keys
        List<String> assetKeys = new ArrayList<>();
        List<String> parentKeys = new ArrayList<>();
        Map<String, Asset> fullAssetMap = new HashMap<>();
        List<Asset> topLevelProjects = new ArrayList<>();

        projectList.forEach(asset -> {
            assetKeys.add(asset.key);
            parentKeys.add(asset.parentKey);

            //For the return, we want a collection of only asset files.
            fullAssetMap.put(asset.key, asset);
        });

        assetKeys.forEach(key -> {
            if(parentKeys.contains(key)){
                // then the asset is a top level project, not a bottom level file
                // thus, add it to collection
                topLevelProjects.add(fullAssetMap.remove(key));
            }
        });

        return topLevelProjects;
    }


    public static List<Asset> getAllFiles(List<Asset> assets) {
        List<Asset> assetList = new ArrayList<>();
        for (int i = 0; i < assets.size(); i++) {
            if(assets.get(i).getAssetType().equals("File")){
                log.info("file is {}", assets.get(i));
                assetList.add(createProject(assets.get(i)));
            }
        }
        return assetList;
    }


    public void setParentKey(String parentKey) {
        this.parentKey = parentKey;
    }

    public HashMap<String, HashMap<String, Object>> getData() {
        return data;
    }

    public void setData(HashMap<String, HashMap<String, Object>> data) {
        this.data = data;
    }


    public void setDescription(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public AssetData getAssetData() {
        return assetData;
    }

    public void setAssetData(AssetData assetData) {
        this.assetData = assetData;
    }

    public ProjectData getProjectData() {
        return projectData;
    }

    public void setProjectData(ProjectData projectData) {
        this.projectData = projectData;
    }

    private static Asset createProject(Asset asset) {
        Asset project = new Asset();
        project.setAssetName(asset.assetName);
        project.setKey(asset.key);
        project.setVersion(asset.version);
        project.setTags(asset.tags);
        project.setLastModified(asset.lastModified);
        project.setAssetType(asset.assetType);
        project.setParentId(asset.parentKey);
        project.setCreatedAt(asset.createdAt);
        project.setMetadata(asset.metadata);
        project.setData(asset.data);
        project.setDescription(asset.description);

        return project;
    }



}
