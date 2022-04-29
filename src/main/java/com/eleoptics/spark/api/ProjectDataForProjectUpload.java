package com.eleoptics.spark.api;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ProjectDataForProjectUpload {

    public ProjectDataForProjectUpload() {

    }

    @JsonProperty("requirements")
    public HashMap<String, ProjectRequirement> requirements = new HashMap<>();
    @JsonProperty("assets")
    public List<String> assets = new ArrayList<>();
    @JsonProperty("versions")
    public List<Integer> versions = new ArrayList<>();

    public ProjectDataForProjectUpload(HashMap<String, ProjectRequirement> requirements, List<String> assets, List<Integer>versions) {
        this.requirements = requirements;
        this.assets = assets;
        this.versions = versions;
    }
}
