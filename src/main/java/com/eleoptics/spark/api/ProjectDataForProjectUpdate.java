package com.eleoptics.spark.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.core.io.FileSystemResource;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ProjectDataForProjectUpdate {


    @JsonProperty("requirements")
    public HashMap<String, ProjectRequirement> requirements = new HashMap<>();
    @JsonProperty("assets")
    public List<String> assets = new ArrayList<>();
    @JsonProperty("versions")
    public List<Integer> versions = new ArrayList<>();
    public ProjectDataForProjectUpdate() {

    }

    @Override
    public String toString() {
        return "ProjectDataForProjectUpdate{" +
                "requirements=" + requirements +
                ", assets=" + assets +
                ", versions=" + versions +
                '}';
    }
}
