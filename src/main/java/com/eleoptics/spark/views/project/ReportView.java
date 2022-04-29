package com.eleoptics.spark.views.project;

import com.eleoptics.spark.api.*;
import com.eleoptics.spark.cache.AssetDAO;
import com.eleoptics.spark.config.SecurityUtils;
import com.eleoptics.spark.eventbus.EventBusFactory;
import com.eleoptics.spark.events.*;
import com.eleoptics.spark.views.QueryParameterNames;
import com.eleoptics.spark.views.RouteNames;
import com.eleoptics.spark.views.main.MainView;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.vaadin.flow.component.*;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.dependency.StyleSheet;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridSortOrder;
import com.vaadin.flow.component.grid.GridSortOrderBuilder;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.MemoryBuffer;
import com.vaadin.flow.data.renderer.LocalDateTimeRenderer;
import com.vaadin.flow.internal.MessageDigestUtil;
import com.vaadin.flow.router.*;
import com.vaadin.flow.server.StreamResource;
import com.vaadin.flow.theme.Theme;
import com.vaadin.flow.theme.lumo.Lumo;
import com.vaadin.flow.theme.material.Material;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.compress.utils.IOUtils;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;

@Route(value = "projectReport")
@Theme(value = Material.class)
@JsModule("./styles/shared-styles.js")
@StyleSheet("context://fonts/stylesheet.css")
@CssImport(value = "./styles/style.css")
@PageTitle("PROJECT REPORT")
@Slf4j
public class ReportView extends VerticalLayout
        implements HasUrlParameter<String> {


    @Autowired
    OpticsApi opticsApi;

    VerticalLayout HeaderLayout = new VerticalLayout();
    H1 projectName = new H1();
    Span projectVersion = new Span();
    Div projectImg = new Div();
    Span projectDescription = new Span();
    Image svgImage = new Image();

    ProjectMetadataView metadataView = new ProjectMetadataView();

    String parentProjectId = new String();
    Asset designPath = new Asset();
    Asset parentProject = new Asset();

    String apiURL = new String();

    // SVG
    RenderSVG renderSVG = new RenderSVG();
    Integer samplePoints = 50;
    Integer svgCardHeight = 300;
    Integer svgCardWidth = 800;
    Integer svgPadding = 40;
    Integer numberOfRays = 3;
    Boolean drawRays = true;

    // asset list
    AssetsGrid assetGrid = new AssetsGrid();

    @SneakyThrows
    @Override
    public void setParameter(BeforeEvent event, @OptionalParameter String parameter) {

        Location location = event.getLocation();
        QueryParameters queryParameters = location.getQueryParameters();

        Map<String, List<String>> parametersMap = queryParameters
                .getParameters();

        apiURL = parametersMap.get(QueryParameterNames.apiURL).get(0);

        // fetch parent project ID
        String designPathId = parametersMap.get(QueryParameterNames.projectId).get(0);
        Integer designPathVersion = null;
        log.info(parametersMap.get(QueryParameterNames.projectId).get(0));
        if (!parametersMap.get(QueryParameterNames.version).get(0).isEmpty()
                && !parametersMap.get(QueryParameterNames.version).get(0).equals("")) {
            designPathVersion = Integer.valueOf(parametersMap.get(QueryParameterNames.version).get(0));
        }



        // use api to fetch the parent asset and the list of assets
        List<Asset> assets = opticsApi.getSharedAsset(designPathId, designPathVersion, true, apiURL).assets;

        // get design path
        Asset designPath = Asset.getAllProjects(assets).get(0);
        log.info("Design path {}", designPath.toString());
        List<Asset> designFiles = Asset.getAllFiles(assets);
        designFiles.forEach(asset -> {
            log.info("Design file holds {}", asset);
        });

        assets.forEach(asset -> {
            log.info("design file asset {}", asset.toString());
        });
        // We add the design path to the "1" position
        designFiles.add(0, designPath);


        // fetch asset list
        String key = NavigationKeyStrings.selectedProjectDetails;
        List<Asset> allAssets = new ArrayList<>();
        List<Asset> sessionAssets = designFiles;

        if (!sessionAssets.isEmpty()) {
            allAssets = sessionAssets;
            log.info("session assets found");
        }

        log.info("The asset list is: " + designFiles.toString());

        // The parent project is the first project in the list
        if (assets.size() <= 1) {
            removeAll();
            add(new Span("There was an error navigating to the selected design path." +
                    " Please contact the help desk at support@eleoptics.com to address this issue."));
            Button returnHome = new Button("Homepage");
            Anchor emailSupport = new Anchor("mailto:support@eleoptics.com");
            emailSupport.setTarget("_blank");
            Button emailButton = new Button("Contact Support");

            emailSupport.add(emailButton);

            returnHome.addClickListener(homeEvent -> UI.getCurrent().navigate(ProjectsView.class));

            add(new HorizontalLayout(returnHome, emailSupport));

            UI.getCurrent().navigate(ProjectsView.class);
        } else {
            //  EventBusFactory.getEventBus().post(new DesignPathDetailsEvent(parentProject));

            // Then, remove it from the list
            designFiles.remove(0);

            // We default by assuming that the project itself is a top level project, and thus is also the parent project.
            parentProjectId = designPath.key;

            // If there are no subprojects, the parent project is the project itself.
            // Otherwise the parent project is the projects parent project (ie you are in a design path).
            for (int filePos = 0; filePos < allAssets.size(); filePos++) {
                Asset projectFile = allAssets.get(filePos);
                if (projectFile.getAssetType().equals("Project")) {
                    parentProjectId = designPath.parentKey;
                }
            }


            try {
                setProjectDetailsView(designPath, designPath.version, designFiles, true);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }


    }

    public ReportView() {

        setClassName("project-details-view-page");

        //Project Header Portion
        //Project Header Portion
        HorizontalLayout headerHorizontalLayout = new HorizontalLayout();
        headerHorizontalLayout.setWidthFull();
        Div imageDiv = new Div();
        // define svg
        //title_svg.add(projectName, svgImage);

        projectName.getStyle().set("margin-top","48px");

        HeaderLayout.add(projectName);
        //HeaderLayout.add(svgImage);
        HeaderLayout.add(projectVersion);
        HeaderLayout.add(projectDescription);
        HeaderLayout.add(projectImg);
        HeaderLayout.setJustifyContentMode(JustifyContentMode.START);
        HeaderLayout.setClassName("project-details-view-header");

        imageDiv.add(svgImage);
        imageDiv.setWidthFull();
        imageDiv.getStyle().set("margin-top","64px");

        headerHorizontalLayout.add(HeaderLayout, imageDiv);
        headerHorizontalLayout.setJustifyContentMode(JustifyContentMode.START);

        // imageDiv.getStyle().set("margin-left","60px");

        add(headerHorizontalLayout);
        headerHorizontalLayout.getStyle().set("margin-bottom","16px");

        //Project Metadata Portion
        metadataView.setWidthFull();
        add(metadataView);

    }

    public void setProjectDetailsView(Asset project, Integer latestVersion, List<Asset> assetList, Boolean populateVersions)
            throws IOException {

        // First, we want to globally set the design path as the correct version
        designPath = project;

        // Header portion
        // In order, the project name, the version and last updated,
        // the description, and the metadata table
        projectName.removeAll();
        projectVersion.setText("");
        projectDescription.removeAll();
        projectImg.removeAll();

        // Define the project name
        projectName.setText(project.assetName);

        String projectVersionString = new String(">Version: " + latestVersion.toString() +
                " | Last Modified: " + project.getLastModifiedFormatted());


        projectVersion.getStyle().set("padding-left", "0px");
        projectVersion.setText(projectVersionString);
        projectVersion.getStyle().set("text-decoration", "underline");
        log.info("setting project version layout called");

        projectDescription.add(project.getDescription());

        // Project Tools and Requirements

        // Check for zemax file
        Asset zemaxAsset = new Asset();
        zemaxAsset = checkOpticalDesignFile(assetList);
        log.info("The asset is " + zemaxAsset);

        // Metadata portion
        // This only gets populated if there is an optical file.
        metadataView.setVisible(false);

        //VisualizationData dataList = new VisualizationData();

        if (zemaxAsset.getAssetName() == null) {
            log.info("The asset is null");
            metadataView.clearMetadataLayout();
            metadataView.setVisible(false);
        } else {
            metadataView.setVisible(true);
            Asset zemaxAssetPrior = zemaxAsset;
            log.info("asset version is : " + zemaxAsset.version);
            if (zemaxAsset.version > 0) {
                //zemaxAssetPrior = getPriorDesign(zemaxAsset);
            }
            // log.info("Current: " + zemaxAsset + "Prior : " + zemaxAssetPrior);
            metadataView.setMetadataViewLayout(zemaxAsset, zemaxAssetPrior);
            getSystemVisualizationPoints(zemaxAsset);
        }

    }

    public void populateVersionLayout(Asset project) {
        //versionGrid.removeAllColumns();

        List<DesignPathVersionClass> pathVersions = new ArrayList<>();
        //log.info("The maximum version is: " + asset.version);
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");

        log.info("The metadata is: " + project.metadata.messages.toString());
        // Loop through all versions
        for (int pathVersion = 0; pathVersion <= project.version; pathVersion++) {
            // By default, report there is no commit message and the date of the commit is unknown
            String defaultCommit = new String("No commit message is available for this version. DATE = NA");

            // If the commit message is present for the version, overwrite the commit message
            if (project.metadata.messages != null &&
                    project.metadata.messages.containsKey("commit - " + pathVersion)) {
                defaultCommit = project.metadata.messages.get("commit - " + pathVersion).toString();
            }

            // handle the 0th commit
            if (pathVersion == 0) {
                defaultCommit = defaultCommit + "DATE = " + project.createdAt.toString().split("T")[0] +
                        " " + project.createdAt.toString().split("T")[1];
            }

            // Attempt to split at the term DATE
            String[] updateMessage = defaultCommit.split("DATE = ");
            String currentVersionCommit = updateMessage[0].toString();

            // Parse the date info
            String versionDate = "Unknown";
            if (updateMessage.length > 1) {
                //log.info(String.valueOf(updateMessage.length));
                versionDate = updateMessage[1];
            }

            pathVersions.add(new DesignPathVersionClass(pathVersion, currentVersionCommit, versionDate));
            //log.info("the version went to" + pathVersion);
        }

    }


/*
    private Asset getPriorDesign(Asset designFile) {
        //  log.info("The current file is : " + designFile.toString());
        Asset priorDesign = opticsApi.getFile(designFile.key, designFile.version - 1);
        // log.info("The new file is:  " + priorDesign);

        return priorDesign;
    }


*/

    private Asset checkOpticalDesignFile(List<Asset> assetList) {
        Asset zemaxAsset = new Asset();

        ListIterator<Asset> assetIterator = assetList.listIterator();

        while (assetIterator.hasNext()) {
            Asset tempAsset = assetIterator.next();

            log.info("temp asset is {}", tempAsset);

            //log.ingo("the messages is: " + tempAsset.toString());
            if(tempAsset.metadata.opticalMetadata!=null) {
                if (tempAsset.metadata.opticalMetadata.isPresent() &&
                        !tempAsset.metadata.opticalMetadata.get().prescription.zLength.equals(null)) {
                    // Assign the zemax file
                    zemaxAsset = tempAsset;
                }
            }
        }

        return zemaxAsset;

    }

    private void getSystemVisualizationPoints(Asset system) throws IOException {

        ArrayList<Integer> samplingPoints = new ArrayList<>();
        OpticalMetadata metadata = system.getMetadata().opticalMetadata.get();
        log.info("The metadata is: " + metadata.toString());
        List<Integer> numSurfaces = metadata.prescription.numberOfInterfaceTypes;
        var sum = numSurfaces.stream()
                .mapToInt(Integer::intValue).sum();
        samplingPoints.addAll(Collections.nCopies(sum, samplePoints));
        log.info("Sampling points are:" + samplingPoints.toString());

        Optional<VisualizationData> dataPoints = getDataPoints(system, samplingPoints, opticsApi);

        //log.info("the data poitns for {} are {}", assetFile, dataPoints.toString());


        if (dataPoints.isPresent()) {
            if (dataPoints.get().system != null) {
                if (dataPoints.get().system.elements != null) {
                    String svgString = renderSVG.createDoc(system, svgCardHeight, svgCardWidth, svgPadding, dataPoints.get(), drawRays);

                    //log.info("SVG : " + svgString.toString().substring(0, 20));


                    byte[] imageBytes = svgString.getBytes(StandardCharsets.UTF_8);

                    StreamResource resource = new StreamResource("dummyImageName.svg", () -> new ByteArrayInputStream(imageBytes));

                    svgImage.setSrc(resource);
                    svgImage.setVisible(true);
                    svgImage.getStyle().set("padding-left", "16px");

                }
            }
        }


    }

    private Optional<VisualizationData> getDataPoints(Asset assetFile, ArrayList<Integer> samplingPoints, OpticsApi opticsApi) {
        VisualizationData dataPoints = null;

        try {
            dataPoints = opticsApi.
                    publicVisualizationPoints(
                            assetFile.key,
                            assetFile.version,
                            samplingPoints,
                            numberOfRays, apiURL);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        // Define optional for return

        if (dataPoints.system != null) {
            if (dataPoints.system.elements != null) {
                return Optional.of(dataPoints);
            } else {
                return Optional.empty();
            }
        } else {
            return Optional.empty();
        }
    }


    public static Integer tryParseInt(String text) {
        try {
            return Integer.parseInt(text);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    public static Float tryParseFloat(String text) {
        try {
            return Float.parseFloat(text);
        } catch (NumberFormatException e) {
            return new Float(0.00);
        }
    }

    public String parseNumber(String value) {
        String result = new String(value);

        if (value.contains("Cannot be Calculated") == false) {
            Float number = new Float(value);

            result = String.format("%.2f", number);
        }

        return result;

    }


    //   */

}
