package com.eleoptics.spark.api;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class AssetsForSharing {
    @JsonProperty("ids")
    public List<String> ids;

    public AssetsForSharing() {
    }

    @Override
    public String toString() {
        StringBuilder output = new StringBuilder();

        output.append("{" +
                "\"ids\": [");
        for (String id:ids ) {
            output.append("\"" + id + "\",");
        }
        output.deleteCharAt(output.length()-1);
        output.append("]}");
        return output.toString();
    }
}
