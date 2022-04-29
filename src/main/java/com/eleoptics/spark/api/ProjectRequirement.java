package com.eleoptics.spark.api;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ProjectRequirement {

    ProjectRequirement() {

    }

    @JsonProperty("requirement_type")
    public RequirementType requirementType;
    public String value;

    public ProjectRequirement(RequirementType requirementType, String value) {
        this.requirementType = requirementType;
        this.value = value;
    }
}
