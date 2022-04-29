package com.eleoptics.spark.api;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Assets {

    public List<Asset> assets;
    public Assets() {
    }

    @Override

    public String toString() {
        return "Assets{" +
                "assets=" + assets +
                '}';
    }
}