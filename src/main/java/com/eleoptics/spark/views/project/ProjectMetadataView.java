package com.eleoptics.spark.views.project;

import com.eleoptics.spark.api.Asset;
import com.eleoptics.spark.api.Metadata;
import com.eleoptics.spark.api.OpticalMetadata;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.H5;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.nullness.Opt;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Slf4j
@CssImport("./styles/style.css")
public class ProjectMetadataView extends Div {

    VerticalLayout layout = new VerticalLayout();
    HorizontalLayout headerLayout = new HorizontalLayout();
    HorizontalLayout metaDataLayout = new HorizontalLayout();
    VerticalLayout surfaceMetaDataLayout = new VerticalLayout();
    VerticalLayout materialMetaDataLayout = new VerticalLayout();
    VerticalLayout performanceMetaDataLayout = new VerticalLayout();
    Metadata metadataMap = new Metadata();
    Metadata priorMetadataMap = new Metadata();


    public ProjectMetadataView() {
        layout.setClassName("project-details-view-metadata");
        layout.setWidthFull();
        headerLayout.setClassName("project-details-view-metadata-header");
        headerLayout.setWidthFull();
        metaDataLayout.setClassName("project-details-view-metadata-body");
        metaDataLayout.setWidthFull();

        layout.setWidthFull();
        layout.setMargin(false);
        layout.setAlignItems(FlexComponent.Alignment.CENTER);

        layout.add(headerLayout);
        layout.add(metaDataLayout);
        add(layout);
    }

    public void setMetadataViewLayout(Asset project, Asset previousVerionProject) {
        headerLayout.removeAll();
        VerticalLayout surfaceHeaderLayout = new VerticalLayout(new H3("Surfaces"));
        VerticalLayout materialHeaderLayout = new VerticalLayout(new H3("Materials"));
        VerticalLayout performanceHeaderLayout = new VerticalLayout(new H3("Performance"));

        surfaceHeaderLayout.setWidthFull();
        surfaceHeaderLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);
        surfaceHeaderLayout.setAlignItems(FlexComponent.Alignment.CENTER);
        surfaceHeaderLayout.getStyle().set("border-right-style", "inset");
        surfaceHeaderLayout.getStyle().set("border-right-width", "2px");
        surfaceHeaderLayout.getStyle().set("margin-left", "32px");

        materialHeaderLayout.setWidthFull();
        materialHeaderLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);
        materialHeaderLayout.setAlignItems(FlexComponent.Alignment.CENTER);
        materialHeaderLayout.getStyle().set("border-right-style", "inset");
        materialHeaderLayout.getStyle().set("border-right-width", "2px");
        materialHeaderLayout.getStyle().set("margin-left", "32px");

        performanceHeaderLayout.setWidthFull();
        performanceHeaderLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);
        performanceHeaderLayout.setAlignItems(FlexComponent.Alignment.CENTER);

        surfaceHeaderLayout.setWidth("100%");
        materialHeaderLayout.setWidth("100%");
        performanceHeaderLayout.setWidth("100%");

        headerLayout.add(surfaceHeaderLayout);
        headerLayout.add(materialHeaderLayout);
        headerLayout.add(performanceHeaderLayout);

        headerLayout.setAlignItems(FlexComponent.Alignment.CENTER);
        headerLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);
        setMetadataLayout(project, previousVerionProject);

    }

    public void clearMetadataLayout(){
        headerLayout.removeAll();
        surfaceMetaDataLayout.removeAll();
        materialMetaDataLayout.removeAll();
        performanceMetaDataLayout.removeAll();
    }

    private void setMetadataLayout(Asset project, Asset previousVersionProject) {
        log.info("the customization of metadata is called");
        surfaceMetaDataLayout.setClassName("project-details-view-metadata-body-surfaces");
        materialMetaDataLayout.setClassName("project-details-view-metadata-body-materials");
        performanceMetaDataLayout.setClassName("project-details-view-metadata-body-performance");

        surfaceMetaDataLayout.removeAll();
        materialMetaDataLayout.removeAll();
        performanceMetaDataLayout.removeAll();

        surfaceMetaDataLayout.setWidth("100%");
        materialMetaDataLayout.setWidth("100%");
        performanceMetaDataLayout.setWidth("100%");
        materialMetaDataLayout.getStyle().set("margin-left", "0px");

        Metadata metadataMap = project.getMetadata();
        Metadata priorMetadataMap = previousVersionProject.getMetadata();

        // handle empties
        log.info("The prior metadata map : " + priorMetadataMap.toString());
        if(priorMetadataMap.opticalMetadata.isEmpty()){
            log.info("We think the metadata is empty?");
            priorMetadataMap = metadataMap;
        }

        setSurfaceMetaDataLayout(metadataMap.opticalMetadata.get(), priorMetadataMap.opticalMetadata.get());
        setMaterialMetaDataLayout(metadataMap.opticalMetadata.get(), priorMetadataMap.opticalMetadata.get());
        setPerformanceMetaDataLayout(metadataMap.opticalMetadata.get(), priorMetadataMap.opticalMetadata.get());


        metaDataLayout.add(surfaceMetaDataLayout, materialMetaDataLayout, performanceMetaDataLayout);
        materialMetaDataLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.START);

    }

    public void setSurfaceMetaDataLayout(OpticalMetadata opticalMetadata, OpticalMetadata priorOpticalMetadata) {
        // This contains locations, sizes, shapes of all the system

        // Collect Current Optical Metadata Performance
        // Get number and type of interfaces
        List<String> interfaceTypes = opticalMetadata.prescription.interfaceTypes;
        List<Integer> numberInterfaceTypes = opticalMetadata.prescription.numberOfInterfaceTypes;

        // Get number and types of surfaces
        List<String> surfaceTypes = opticalMetadata.prescription.surfaceTypes;
        List<Integer> numberSurfaceTypes = opticalMetadata.prescription.numberOfSurfaceTypes;

        // Get Stop Position
        Integer stopPosition = opticalMetadata.prescription.apertureStopIndex;

        // Exit and Entrance Pupil Location
        Float exitPupilLocation = opticalMetadata.performance.exitPupilDistance;
        Float entrancePupilLocation = opticalMetadata.performance.entrancePupilDistance;

        // Pupil Diameters
        Float entrancePupilDiameter = opticalMetadata.performance.entrancePupilDiameter;
        Float exitPupilDiameter = opticalMetadata.performance.exitPupilDiameter;

        // System Shape
        Float systemLength = opticalMetadata.prescription.zLength;
        Float systemDiameter = opticalMetadata.prescription.yHeight;

        // Create Headers
        // Create Strings for the metadata
        String interfaceString = new String("Interfaces: ");
        for (int pos = 0; pos < interfaceTypes.size(); pos++) {
            if(pos < interfaceTypes.size() - 1) {
                interfaceString = interfaceString + interfaceTypes.get(pos) + " " +
                        numberInterfaceTypes.get(pos) + " | ";
            } else{
                interfaceString = interfaceString + interfaceTypes.get(pos) + " " + numberInterfaceTypes.get(pos);
            }
        }

        // Create Strings for the metadata
        String surfaceString = new String("Surfaces: ");
        for (int pos = 0; pos < surfaceTypes.size(); pos++) {
            if(pos < surfaceTypes.size() - 1) {
                surfaceString = surfaceString + surfaceTypes.get(pos) + " " +
                        numberSurfaceTypes.get(pos) + " | ";
            } else{
                surfaceString = surfaceString + surfaceTypes.get(pos) + " " + numberSurfaceTypes.get(pos);
            }
        }
        H5 interfacesHeader = new H5(interfaceString);
        H5 surfacesHeader = new H5(surfaceString);
        H5 stopPositionHeader = new H5("Stop Element: " + stopPosition);
        H5 exitPupilLocationHeader = new H5("Exit Pupil Dist.: " + String.format("%.2f", exitPupilLocation) + " mm");
        H5 entPupilLocationHeader = new H5("Entrance Pupil Dist.: " + String.format("%.2f", entrancePupilLocation) + " mm");
        H5 exitPupilDiameterHeader = new H5("Exit Pupil Diam.: " + String.format("%.2f", exitPupilDiameter) + " mm");
        H5 entrancePupilDiameterHeader = new H5("Entrance Pupil Diam.: " + String.format("%.2f", entrancePupilDiameter) + " mm");
        H5 systemLengthHeader = new H5("System Axial Length: " + String.format("%.2f", systemLength) + " mm");
        H5 systemDiameterhHeader = new H5("System Max Diameter: " + String.format("%.2f", systemDiameter) + " mm");

        // Collect Prior Optical Metadata Performance

        // Get number and type of interfaces
        List<String> priorInterfaceTypes = priorOpticalMetadata.prescription.interfaceTypes;
        List<Integer> priorNumberInterfaceTypes = priorOpticalMetadata.prescription.numberOfInterfaceTypes;

        // Get number and types of surfaces
        List<String> priorSurfaceTypes = opticalMetadata.prescription.surfaceTypes;
        List<Integer> priorNumberSurfaceTypes = opticalMetadata.prescription.numberOfSurfaceTypes;

        // Get Stop Position
        Integer priorStopPosition = priorOpticalMetadata.prescription.apertureStopIndex;

        // Exit and Entrance Pupil Location
        Float priorExitPupilLocation = priorOpticalMetadata.performance.exitPupilDistance;
        Float priorEntrancePupilLocation = priorOpticalMetadata.performance.entrancePupilDistance;

        // Principle Plane Locations
        Float priorEntrancePupilDiameter = priorOpticalMetadata.performance.entrancePupilDiameter;
        Float priorExitPupilDiameter = priorOpticalMetadata.performance.exitPupilDiameter;

        // System Shape
        Float priorSystemLength = priorOpticalMetadata.prescription.zLength;
        Float priorSystemDiameter = priorOpticalMetadata.prescription.yHeight;


        H5 changeInterfaces = new H5();
        H5 changeXPLocation = new H5();
        H5 changeEPLocation = new H5();
        H5 changeStopPosition = new H5();
        H5 changeXPDiameter = new H5();
        H5 changeEPDiameter = new H5();
        H5 changeSystemLength = new H5();
        H5 changeSystemDiameter = new H5();

        /*
        NOTE Not working right now
        // Interface Changes from Prior
        for (int pos = 0; pos < interfaceTypes.size(); pos++) {
            String interfaceDifferences = new String();
            if (priorInterfaceTypes.contains(interfaceTypes.get(pos))) {
                // we add on a change number to the interfaceDifferences
                Integer difference = numberInterfaceTypes.get(pos) -
                        priorNumberInterfaceTypes.get(priorInterfaceTypes.indexOf(interfaceTypes.get(pos)));
                interfaceDifferences = interfaceDifferences + difference + ", ";
            } else {
                // The change in interface types is just the new number itself, as it didn't have those interfaces previously
                interfaceDifferences = interfaceDifferences + numberInterfaceTypes.get(pos) + ", ";
            }
            changeInterfaces.add(interfaceDifferences);
            // add styling
            changeInterfaces.getStyle().set("color", "#2CD6FF");
        }

         */

        // Exit Pupil Location Change from Prior
        if (exitPupilLocation - priorExitPupilLocation != 0.0) {
            String exitPupilLocationDifference = String.format("%.2f", exitPupilLocation - priorExitPupilLocation) + " mm";
            changeXPLocation.add(exitPupilLocationDifference);
            changeXPLocation.getStyle().set("color", "#2CD6FF");
        }

        // Entrance Pupil Location Change From Prior
        if (entrancePupilLocation - priorEntrancePupilLocation != 0.0) {
            log.info("Current EP : " + priorEntrancePupilLocation + ", Prior EP : " + priorEntrancePupilLocation);
            String entrancePupilLocationDifference = String.format("%.2f", (entrancePupilLocation - priorEntrancePupilLocation)) + " mm";
            changeEPLocation.add(entrancePupilLocationDifference);
            changeEPLocation.getStyle().set("color", "#2CD6FF");
        }

        // Entrance Pupil Diameter Change From Prior
        if (entrancePupilDiameter - priorEntrancePupilDiameter != 0.0) {
            String entrancePupilDiamDifference = String.format("%.2f", entrancePupilDiameter - priorEntrancePupilDiameter) + " mm";
            changeEPDiameter.add(entrancePupilDiamDifference);
            changeEPDiameter.getStyle().set("color", "#2CD6FF");
        }
        // Exit Pupil Diameter Change From Prior
        if (exitPupilDiameter - priorExitPupilDiameter != 0.0) {
            String exitPupilDiamDifference = String.format("%.2f", exitPupilDiameter - priorExitPupilDiameter) + " mm";
            changeXPDiameter.add(exitPupilDiamDifference);
            changeXPDiameter.getStyle().set("color", "#2CD6FF");
        }

        // Stop Position
        if (stopPosition - priorStopPosition != 0) {
            String stopPositionDifference = String.valueOf(stopPosition - priorStopPosition);
            changeStopPosition.add(stopPositionDifference);
            changeStopPosition.getStyle().set("color", "#2CD6FF");
        }
        // System Length Change From Prior
        if (systemLength - priorSystemLength != 0.0) {
            String systemLengthDifference = String.format("%.2f", systemLength - priorSystemLength) + " mm";
            changeSystemLength.add(systemLengthDifference);
            changeSystemLength.getStyle().set("color", "#2CD6FF");
        }
        // System Diameter Change From Prior
        if (systemDiameter - priorSystemDiameter != 0.0) {
            String systemDiameterDifference = String.format("%.2f", systemDiameter - priorSystemDiameter) + " mm";
            changeSystemDiameter.add(systemDiameterDifference);
            changeSystemDiameter.getStyle().set("color", "#2CD6FF");
        }


        surfaceMetaDataLayout.add(
                new HorizontalLayout(interfacesHeader, changeInterfaces),
                new HorizontalLayout(surfacesHeader, changeInterfaces),
                new HorizontalLayout(stopPositionHeader, changeStopPosition),
                new HorizontalLayout(entPupilLocationHeader, changeEPLocation),
                new HorizontalLayout(exitPupilLocationHeader, changeXPLocation),
                new HorizontalLayout(entrancePupilDiameterHeader, changeEPDiameter),
                new HorizontalLayout(exitPupilDiameterHeader, changeXPDiameter),
                new HorizontalLayout(systemLengthHeader, changeSystemLength),
                new HorizontalLayout(systemDiameterhHeader, changeSystemDiameter)

        );
    }

    public void setMaterialMetaDataLayout(OpticalMetadata opticalMetadata, OpticalMetadata priorOpticalMetadata) {
        // Material List
        List<String> materialList = opticalMetadata.prescription.materials;
        // Material Classes
        List<String> classList = opticalMetadata.prescription.materialClasses;
        // Material Vendors List
        List<String> vendorList = opticalMetadata.prescription.materialVendors;

        // Collect Prior Data
        // Create Headers
        // Create Strings for the metadata
        String materialString = new String("Materials: ");
        for (int pos = 0; pos < materialList.size(); pos++) {
            if(pos < materialList.size() - 1) {
                materialString = materialString + materialList.get(pos) + " | ";
            } else{
                materialString = materialString + materialList.get(pos);
            }
        }
        String vendorString = new String("Vendors: ");
        for (int pos = 0; pos < vendorList.size(); pos++) {
            if (!vendorList.get(pos).equals("Fictional") && pos < vendorList.size() - 1) {
                vendorString = vendorString + vendorList.get(pos) + " | ";
            } else if(!vendorList.get(pos).equals("Fictional") && pos == vendorList.size() - 1){
                vendorString = vendorString + vendorList.get(pos);
            }
        }
        String classString = new String("Material Class: ");
        for (int pos = 0; pos < classList.size(); pos++) {
            if (!classList.get(pos).equals("Ficticious") && pos < classList.size() - 1) {
                classString = classString + classList.get(pos) + " | ";
            } else if(!classList.get(pos).equals("Ficticious") && pos == classList.size() - 1){
                classString = classString + classList.get(pos);
            }
        }


        H5 materialsHeader = new H5(materialString);
        H5 vendorHeader = new H5(vendorString);
        H5 classHeader = new H5(classString);

        // Collect Prior Optical Metadata Performance
        // Material List
        List<String> priorMaterialList = priorOpticalMetadata.prescription.materials;
        // Material Classes
        List<String> priorClassList = priorOpticalMetadata.prescription.materialClasses;
        // Material Vendors List
        List<String> priorVendorList = priorOpticalMetadata.prescription.materialVendors;

        H5 changeMaterials = new H5();
        H5 changeVendors = new H5();
        H5 changeClasses = new H5();


        // Interface Changes from Prior
        List<String> differences = new ArrayList<>(materialList);
        differences.removeAll(priorMaterialList);
        String diffString = differences.toString().replaceAll("\\[|\\]", "");
        changeMaterials.add(diffString);
        // add styling
        changeMaterials.getStyle().set("color", "#2CD6FF");

        // Interface Changes from Prior
        differences = vendorList;
        differences.removeAll(priorVendorList);
        diffString = differences.toString().replaceAll("\\[|\\]", "");
        changeVendors.add(diffString);
        // add styling
        changeVendors.getStyle().set("color", "#2CD6FF");

        // Interface Changes from Prior
        differences = classList;
        differences.removeAll(priorClassList);
        diffString = differences.toString().replaceAll("\\[|\\]", "");
        changeClasses.add(diffString);
        // add styling
        changeClasses.getStyle().set("color", "#2CD6FF");

        materialMetaDataLayout.add(
                new HorizontalLayout(materialsHeader, changeMaterials),
                new HorizontalLayout(vendorHeader, changeVendors),
                new HorizontalLayout(classHeader, changeClasses)
        );
    }

    //method to remove last character
    private String removeLastChar(String s) {
//returns the string after removing the last character
        return s.substring(0, s.length() - 1);
    }

    public void setPerformanceMetaDataLayout(OpticalMetadata opticalMetadata, OpticalMetadata priorOpticalMetadata) {
        // Paraxial Image Height and Location
        Float paraxialImgHeight = opticalMetadata.performance.paraxialImageHeight;
        Float paraxialMagnification = opticalMetadata.performance.paraxialMagnification;

        // Back Focal Distance
        Float backFocalDistance = opticalMetadata.performance.backFocalDistance;

        // F-Number
        Float fNumber = opticalMetadata.performance.fNumber;

        // Effective Focal Length
        Float effectiveFocalLength = opticalMetadata.performance.effectiveFocalLength;

        // Object Numerical Aperture
        Float objectNumericalAperture = opticalMetadata.performance.objectNumericalAperture;

        // Image Numerical Aperture
        Float imageNumericalAperture = opticalMetadata.performance.imageNumericalAperture;

        // Field of View
        Float fullFieldOfView = opticalMetadata.performance.fullFieldOfView;

        // Angular MAgnification
        Float angularMagnification = opticalMetadata.performance.angularMagnification;

        // Working F Number
        Float workingFNumber = opticalMetadata.performance.workingFNumber;

        // Create Headers

        H5 backFocalDistanceHeader = new H5("Back Focal Dist.: " + String.format("%.2f", backFocalDistance) + " mm");
        H5 fNumberHeader = new H5("F-Number: " + String.format("%.2f", fNumber));
        H5 effFocalLengthHeader = new H5("Eff. Focal Length: " + String.format("%.2f", effectiveFocalLength) + " mm");
        H5 objNumericalApertureHeader = new H5("Obj. Space NA: " + String.format("%.2f", objectNumericalAperture));
        H5 imgNumericalApertureHeader = new H5("Img. Space NA: " + String.format("%.2f", imageNumericalAperture));
        H5 fullFieldOfViewHeader = new H5("Full Field of View: " + String.format("%.2f", fullFieldOfView) + " deg");
        H5 paraxialImageHeightHeader = new H5("Paraxial Img. Height: " + String.format("%.2f", paraxialImgHeight) + " mm");
        H5 paraxialimageMagnificationHeader = new H5("Paraxial Magnification: " + String.format("%.2f", paraxialMagnification));
        H5 angularMagnificationHeader= new H5("Angular Magnification: " + String.format("%.2f", angularMagnification));
        H5 workingFNumberHeader = new H5("Working F-Number: " + String.format("%.2f", workingFNumber));


        // Collect Prior Optical Metadata Performance

        // Paraxial Image Height and Location
        Float priorParaxialImgHeight = priorOpticalMetadata.performance.paraxialImageHeight;
        Float priorParaxialMagnification = priorOpticalMetadata.performance.paraxialMagnification;

        // Back Focal Distance
        Float priorBackFocalDistance = priorOpticalMetadata.performance.backFocalDistance;
        // F-Number
        Float priorFNumber = priorOpticalMetadata.performance.fNumber;
        // Effective Focal Length
        Float priorEffectiveFocalLength = priorOpticalMetadata.performance.effectiveFocalLength;
        // Object Numerical Aperture
        Float priorObjectNumericalAperture = priorOpticalMetadata.performance.objectNumericalAperture;
        // Image Numerical Aperture
        Float priorImageNumericalAperture = priorOpticalMetadata.performance.imageNumericalAperture;
        // Field of View
        Float priorFullFieldOfView = priorOpticalMetadata.performance.fullFieldOfView;

        // Angular MAgnification
        Float priorAngularMagnification = priorOpticalMetadata.performance.angularMagnification;

        // Working F Number
        Float priorWorkingFNumber = priorOpticalMetadata.performance.workingFNumber;


        H5 changeFieldOfView = new H5();
        H5 changeBackFocalDistance = new H5();
        H5 changeFNumber = new H5();
        H5 changeObjNA = new H5();
        H5 changeImgNA = new H5();
        H5 changeEffFocalLength = new H5();
        H5 changeParaxialImgHeight = new H5();
        H5 changeParaxialMagnification = new H5();
        H5 changeAngularMagnification = new H5();
        H5 changeWorkingFNumber = new H5();

        // Field of View Changes from Prior
        if (fullFieldOfView - priorFullFieldOfView != 0.0) {
            String fieldOfViewDifferences = String.format("%.2f", fullFieldOfView - priorFullFieldOfView) + " deg";
            changeFieldOfView.add(fieldOfViewDifferences);
            // add styling
            changeFieldOfView.getStyle().set("color", "#2CD6FF");
        }
        // Paraxial Image Height Difference from Prior
        if (paraxialImgHeight - priorParaxialImgHeight != 0.00) {
            String paraxialImageHeightDifferences = String.format("%.2f", paraxialImgHeight - priorParaxialImgHeight) + " mm";
            changeParaxialImgHeight.add(paraxialImageHeightDifferences);
            changeParaxialImgHeight.getStyle().set("color", "#2CD6FF");
        }
        // Back Focal Distance Change from Prior
        if (backFocalDistance - priorBackFocalDistance != 0.0) {
            String backFocalDistanceDifference = String.format("%.2f", backFocalDistance - priorBackFocalDistance) + " mm";
            changeBackFocalDistance.add(backFocalDistanceDifference);
            changeBackFocalDistance.getStyle().set("color", "#2CD6FF");
        }
        // Eff. Focal Length Change from Prior
        if (effectiveFocalLength - priorEffectiveFocalLength != 0.0) {
            String effFocalLengthDifference = String.format("%.2f", effectiveFocalLength - priorEffectiveFocalLength) + " mm";
            changeEffFocalLength.add(effFocalLengthDifference);
            changeEffFocalLength.getStyle().set("color", "#2CD6FF");
        }
        // F-Number Change From Prior
        if (fNumber - priorFNumber != 0.0) {
            String fNumberDifference = String.format("%.2f", fNumber - priorFNumber);
            changeFNumber.add(fNumberDifference);
            changeFNumber.getStyle().set("color", "#2CD6FF");
        }
        // Obj NA Change from Prior
        if (objectNumericalAperture - priorObjectNumericalAperture != 0) {
            String objNADifference = String.format("%.2f", objectNumericalAperture - priorObjectNumericalAperture);
            changeObjNA.add(objNADifference);
            changeObjNA.getStyle().set("color", "#2CD6FF");
        }
        // Img NA Change from Prior
        if (imageNumericalAperture - priorImageNumericalAperture != 0) {
            String imgNADifference = String.format("%.2f", imageNumericalAperture - priorImageNumericalAperture);
            changeImgNA.add(imgNADifference);
            changeImgNA.getStyle().set("color", "#2CD6FF");
        }
        // Paraxial Magnfication Difference
        if (paraxialMagnification != null && priorParaxialMagnification != null &&
                paraxialMagnification - priorParaxialMagnification != 0.0) {
            String paraxialMagDifference = String.format("%.2f", paraxialMagnification - priorParaxialMagnification);
            changeParaxialMagnification.add(paraxialMagDifference);
            changeParaxialMagnification.getStyle().set("color", "#2CD6FF");
        }
        // Angular Magnification Difference
        if (angularMagnification != null && priorAngularMagnification != null &&
                angularMagnification - priorAngularMagnification != 0.0) {
            String angularMagDifference = String.format("%.2f", angularMagnification - priorAngularMagnification);
            changeAngularMagnification.add(angularMagDifference);
            changeAngularMagnification.getStyle().set("color", "#2CD6FF");
        }
        // Working F Num Difference
        if (workingFNumber != null && priorWorkingFNumber != null &&
                workingFNumber - priorWorkingFNumber != 0.0) {
            String workingFNumDifference = String.format("%.2f", workingFNumber - priorWorkingFNumber);
            changeWorkingFNumber.add(workingFNumDifference);
            changeWorkingFNumber.getStyle().set("color", "#2CD6FF");
        }


        performanceMetaDataLayout.add(
                new HorizontalLayout(effFocalLengthHeader, changeEffFocalLength),
                new HorizontalLayout(fullFieldOfViewHeader, changeFieldOfView),
                new HorizontalLayout(fNumberHeader, changeFNumber),
                new HorizontalLayout(workingFNumberHeader, changeWorkingFNumber),
                new HorizontalLayout(backFocalDistanceHeader, changeBackFocalDistance),
                new HorizontalLayout(paraxialImageHeightHeader, changeParaxialImgHeight),
                //new HorizontalLayout(paraxialimageMagnificationHeader, changeParaxialMagnification),
                new HorizontalLayout(objNumericalApertureHeader, changeObjNA),
                new HorizontalLayout(imgNumericalApertureHeader, changeImgNA),
                new HorizontalLayout(angularMagnificationHeader, changeAngularMagnification)
        );
    }


    public String readMap(HashMap<String, Object> hashMap, String key) {
        String result = new String(key + " Cannot be Calculated.");
        if (hashMap.containsKey(key)) {
            result = hashMap.get(key).toString();
        }
        return result;
    }

    public String parseNumber(String value) {
        String result = new String(value);

        if (value.contains("Cannot be Calculated") == false) {
            Float number = new Float(value);

            result = String.format("%.2f", number);
        }

        return result;

    }


}

