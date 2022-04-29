package com.eleoptics.spark.api;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;

public class OpticalMetadata<Public> implements Serializable {

    public Performance performance;
    public Prescription prescription;

    public class Performance {
        @Override
        public String toString() {
            return "Performance{" +
                    "minimumWavelength=" + minimumWavelength +
                    ", maximumWavelength=" + maximumWavelength +
                    ", fullFieldOfView=" + fullFieldOfView +
                    ", objectDistance=" + objectDistance +
                    ", effectiveFocalLength=" + effectiveFocalLength +
                    ", backFocalDistance=" + backFocalDistance +
                    ", entrancePupilDiameter=" + entrancePupilDiameter +
                    ", exitPupilDiameter=" + exitPupilDiameter +
                    ", entrancePupilDistance=" + entrancePupilDistance +
                    ", exitPupilDistance=" + exitPupilDistance +
                    ", fNumber=" + fNumber +
                    ", imageNumericalAperture=" + imageNumericalAperture +
                    ", objectNumericalAperture=" + objectNumericalAperture +
                    ", workingFNumber=" + workingFNumber +
                    ", paraxialImageHeight=" + paraxialImageHeight +
                    ", paraxialMagnification=" + paraxialMagnification +
                    ", angularMagnification=" + angularMagnification +
                    '}';
        }

        @JsonProperty("minimum_wavelength")
        public Float minimumWavelength;
        @JsonProperty("maximum_wavelength")
        public Float maximumWavelength;
        @JsonProperty("full_field_of_view")
        public Float fullFieldOfView;
        @JsonProperty("object_distance")
        public Float objectDistance;
        @JsonProperty("effective_focal_length")
        public Float effectiveFocalLength;
        @JsonProperty("back_focal_distance")
        public Float backFocalDistance;
        @JsonProperty("entrance_pupil_diameter")
        public Float entrancePupilDiameter;
        @JsonProperty("exit_pupil_diameter")
        public Float exitPupilDiameter;
        @JsonProperty("entrance_pupil_distance")
        public Float entrancePupilDistance;
        @JsonProperty("exit_pupil_distance")
        public Float exitPupilDistance;
        @JsonProperty("f_number")
        public Float fNumber;
        @JsonProperty("image_numerical_aperture")
        public Float imageNumericalAperture;
        @JsonProperty("object_numerical_aperture")
        public Float objectNumericalAperture;
        @JsonProperty("working_f_number")
        public Float workingFNumber;
        @JsonProperty("paraxial_image_height")
        public Float paraxialImageHeight;
        @JsonProperty("paraxial_magnification")
        public Float paraxialMagnification;
        @JsonProperty("angular_magnification")
        public Float angularMagnification;
    }

    public class Prescription {

        @Override
        public String toString() {
            return "Prescription{" +
                    "xWidth=" + xWidth +
                    ", yHeight=" + yHeight +
                    ", zLength=" + zLength +
                    ", symmetry=" + symmetry +
                    ", systemClass=" + systemClass +
                    ", designForm=" + designForm +
                    ", manufacturingTolerance=" + manufacturingTolerance +
                    ", cost=" + cost +
                    ", polarizationSensitive=" + polarizationSensitive +
                    ", environment_Material='" + environment_Material + '\'' +
                    ", temperature=" + temperature +
                    ", pressure=" + pressure +
                    ", apertureType='" + apertureType + '\'' +
                    ", apertureValue=" + apertureValue +
                    ", apertureStopIndex=" + apertureStopIndex +
                    ", materials=" + materials +
                    ", materialVendors=" + materialVendors +
                    ", materialClasses=" + materialClasses +
                    ", numberOfGroups=" + numberOfGroups +
                    ", numberOfElements=" + numberOfElements +
                    ", numberOfInterfaces=" + numberOfInterfaces +
                    ", interfaceTypes=" + interfaceTypes +
                    ", numberOfInterfaceTypes=" + numberOfInterfaceTypes +
                    ", surfaceTypes=" + surfaceTypes +
                    ", numberOfSurfaceTypes=" + numberOfSurfaceTypes +
                    '}';
        }

        @JsonProperty("x_width")
        public Float xWidth;
        @JsonProperty("y_height")
         public Float yHeight;
        @JsonProperty("z_length")
         public Float zLength;
        @JsonProperty("symmetry")
         public Symmetry symmetry;
        @JsonProperty("system_class")
         public System_Class systemClass;
        @JsonProperty("design_form")
         public Design_Form designForm;
        @JsonProperty("manufacturing_tolerance")
         public Manufacturing_Tolerance manufacturingTolerance;
        @JsonProperty("cost")
         public Float cost;
        @JsonProperty("polarization_sensitive")
         public Boolean polarizationSensitive;
        @JsonProperty("environment_material")
         public String environment_Material;
        @JsonProperty("temperature")
         public Float temperature;
        @JsonProperty("pressure")
         public Float pressure;
        @JsonProperty("aperture_type")
         public String apertureType;
        @JsonProperty("aperture_value")
         public Float apertureValue;
        @JsonProperty("aperture_stop_index")
         public Integer apertureStopIndex;
        @JsonProperty("materials")
         public List<String> materials;
        @JsonProperty("material_vendors")
         public List<String> materialVendors;
        @JsonProperty("material_classes")
         public List<String> materialClasses;
        @JsonProperty("number_of_groups")
         public Integer numberOfGroups;
        @JsonProperty("number_of_elements")
         public Integer numberOfElements;
        @JsonProperty("number_of_interfaces")
         public Integer numberOfInterfaces;
        @JsonProperty("interface_types")
         public List<String> interfaceTypes;
        @JsonProperty("number_of_interface_types")
         public List<Integer> numberOfInterfaceTypes;
        @JsonProperty("surface_types")
        public List<String> surfaceTypes;
        @JsonProperty("number_of_surface_types")
        public List<Integer> numberOfSurfaceTypes;
    }

    enum Symmetry {
        Rotational,
        Xplane,
        Yplane,
        Freeform
    }

    enum System_Class {
        Refractive,
        Reflective,
        Catadioptric
    }

    enum Design_Form {
        Custom,
        Parabola,
        Cassegrain,
        SchmidtCassegrain,
        Schmidt,
        ThreeMirrorAnastigmat,
        RitcheyChretien,
        AchromaticDoublet,
        OpticalDiskObjective,
        MicroscopeObjective,
        Petzval,
        SplitTriplet,
        Triplet,
        Landscape,
        Tessar,
        DoubleGauss,
        Meniscus,
        Angulon,
        RetroFocus,
        Fisheye
    }

    enum Manufacturing_Tolerance {
        Commercial,
        Precision,
        HighPrecision
    }


    public OpticalMetadata() {
    }
    /*

    public void setPerformance(HashMap<String, Object> performanceHashmap) {
        this.performance.angularMagnification = (Float) performanceHashmap.get("angular_magnification");
        this.performance.backFocalDistance = (Float) performanceHashmap.get("back_focal_distance");
        this.performance.effectiveFocalLength = (Float) performanceHashmap.get("effective_focal_length");
        this.performance.entrancePupilDiameter = (Float) performanceHashmap.get("entrance_pupil_diameter");
        this.performance.entrancePupilDistance = (Float) performanceHashmap.get("entrance_pupil_distance");
        this.performance.exitPupilDiameter = (Float) performanceHashmap.get("exit_pupil_diameter");
        this.performance.exitPupilDistance = (Float) performanceHashmap.get("exit_pupil_distance");
        this.performance.fNumber = (Float) performanceHashmap.get("f_number");
        this.performance.fullFieldOfView = (Float) performanceHashmap.get("full_field_of_view");
        this.performance.imageNumericalAperture = (Float) performanceHashmap.get("image_numerical_aperture");
        this.performance.maximumWavelength = (Float) performanceHashmap.get("maximum_wavelength");
        this.performance.minimumWavelength = (Float) performanceHashmap.get("minimum_wavelength");
        this.performance.objectDistance = (Float) performanceHashmap.get("object_distance");
        this.performance.objectNumericalAperture = (Float) performanceHashmap.get("object_numerical_aperture");
        this.performance.paraxialImageHeight = (Float) performanceHashmap.get("paraxial_image_height");
        this.performance.paraxialImageHeight = (Float) performanceHashmap.get("paraxial_image_height");
        this.performance.paraxialMagnification = (Float) performanceHashmap.get("paraxial_magnification");
        this.performance.workingFNumber = (Float) performanceHashmap.get("working_f_number");

    }

    public void setPrescription(HashMap<String, Object> prescriptionHashmap) {
        this.prescription.apertureStopIndex = (Integer) prescriptionHashmap.get("aperture_stop_index");
        this.prescription.apertureType = (String) prescriptionHashmap.get("aperture_type");
        this.prescription.apertureValue= (String) prescriptionHashmap.get("aperture_type");


    }

     */

    @Override
    public String toString() {
        return "OpticalMetadata{" +
                "performance=" + performance +
                ", prescription=" + prescription +
                '}';
    }
}
