package com.eleoptics.spark.api;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

public class VisualizationData implements Serializable {
    @JsonProperty("system")
    public SystemVisualization system;
    @JsonProperty("ray_data")
    public RayContainer rayData;

    @Override
    public String toString() {
        return "VisualizationData{" +
                "visualizationLists=" + system +
                ", rayData=" + rayData +
                '}';
    }


    public static class SystemVisualization {
        @JsonProperty("elements")
        public List<InterfaceList> elements;


        @Override
        public String toString() {
            return "SystemVisualization{" +
                    "elements=" + elements +
                    '}';
        }
    }

    public static class RayContainer {
        @JsonProperty("sources")
        public List<SourceData> rayPoints;

        @Override
        public String toString() {
            return "RayContainer{" +
                    "rayPoints=" + rayPoints +
                    '}';
        }
    }

    public static class SourceData {
        @JsonProperty("marginal_ray")
        public List<RayInterfaces> marginalRays;

        @JsonProperty("chief_ray")
        public List<RayInterfaces> chiefRays;

        @JsonProperty("rays")
        public List<RayInterfaces> rays;

        @Override
        public String toString() {
            return "SourceData{" +
                    "marginalRays=" + marginalRays +
                    ", chiefRays=" + chiefRays +
                    ", rays=" + rays +
                    '}';
        }
    }

    public static class RayInterfaces {
        @JsonProperty("interfaces")
        public List<RayData> rayData;

        @Override
        public String toString() {
            return "RayInterfaces{" +
                    "rayData=" + rayData +
                    '}';
        }
    }

    public static class RayData {
        @JsonProperty("x")
        public Double x;
        @JsonProperty("y")
        public Double y;
        @JsonProperty("wavelength")
        public Double wavelength;

        @Override
        public String toString() {
            return "RayData{" +
                    "x=" + x +
                    ", y=" + y +
                    ", wavelength=" + wavelength +
                    '}';
        }
    }

    public static class InterfaceList {
        @JsonProperty("interfaces")
        public List<PointsList> interfaces;

        public InterfaceList() {
        }

        @Override
        public String toString() {
            return "InterfaceList{" +
                    "interfaces=" + interfaces + '\'' +
                    '}';
        }
    }

    public static class PointsList {
        @JsonProperty("points")
        public double[][] points;
        @JsonProperty("interface_type")
        public String interfaceType;

        public PointsList() {
        }

        @Override
        public String toString() {
            return "PointsList{" +
                    "points=" + Arrays.toString(points) +
                    ", interfaceType='" + interfaceType + '\'' +
                    '}';
        }
    }


    public VisualizationData() {
    }


}


