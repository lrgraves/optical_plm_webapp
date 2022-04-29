package com.eleoptics.spark.api;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;
import java.util.Optional;

public class Metadata {

    @JsonProperty("messages")
    public Map<String, String> messages;
    @JsonProperty("optical")
    public Optional<OpticalMetadata> opticalMetadata;


    @Override
    public String toString() {
        return  "messages=" + messages +
                ", opticalMetadata=" + opticalMetadata +
                '}';
    }

    public Metadata() {

    }



}
