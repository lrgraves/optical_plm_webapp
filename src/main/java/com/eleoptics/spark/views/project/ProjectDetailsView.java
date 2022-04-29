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
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dependency.CssImport;
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
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.MemoryBuffer;
import com.vaadin.flow.data.renderer.LocalDateTimeRenderer;
import com.vaadin.flow.internal.MessageDigestUtil;
import com.vaadin.flow.router.*;
import com.vaadin.flow.server.StreamResource;
import com.vaadin.flow.server.VaadinService;
import com.vaadin.flow.server.VaadinServletRequest;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.compress.utils.IOUtils;
import org.apache.commons.io.FileUtils;
import org.apache.xpath.operations.Bool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.lang.Nullable;
import org.vaadin.olli.ClipboardHelper;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import javax.servlet.http.Part;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;

@Route(value = "projectDetails", layout = MainView.class)
@CssImport(value = "./styles/style.css", themeFor = "vaadin-app-layout")
@PageTitle("PROJECT DETAIL")
@Slf4j
public class ProjectDetailsView extends VerticalLayout
        implements HasUrlParameter<String> {


    @Autowired
    OpticsApi opticsApi;

    VerticalLayout HeaderLayout = new VerticalLayout();
    H1 projectName = new H1();
    Button projectVersion = new Button();
    Div projectImg = new Div();
    Span projectDescription = new Span();
    Asset fetchedProject = new Asset();
    VerticalLayout toolsLayout = new VerticalLayout();
    VerticalLayout requirementsLayout = new VerticalLayout();
    VerticalLayout assetListLayout = new VerticalLayout();
    HorizontalLayout projectFunctionsLayout = new HorizontalLayout();
    Grid<Asset> filesGrid = new Grid<>(Asset.class);
    Div gridDiv = new Div();
    List<Asset> projectList = new ArrayList<>();
    Dialog updateDialog = new Dialog();
    Icon closeIcon = new Icon(VaadinIcon.CLOSE);
    Button closeButton = new Button("", closeIcon);
    Image svgImage = new Image();

    String userID = SecurityUtils.getUserID();


    ProjectMetadataView metadataView = new ProjectMetadataView();
    OpticalMetadata metadataMap = new OpticalMetadata();
    OpticalMetadata priorMetadataMap = new OpticalMetadata();
    VerticalLayout versionLayout = new VerticalLayout();
    Grid<DesignPathVersionClass> versionGrid = new Grid<>(DesignPathVersionClass.class);
    HorizontalLayout breadcrumbLayout = new HorizontalLayout();
    String parentProjectNavString = new String();


    String parentProjectId = new String();
    Asset designPath = new Asset();
    Asset parentProject = new Asset();
    List<Asset> parentProjectDesignPaths = new ArrayList();
    Asset designPathPriorVersion = new Asset();
    Notification betaLimitations = new Notification();

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

        // fetch parent project ID
        String designPathId = parametersMap.get(QueryParameterNames.projectId).get(0);
        Integer designPathVersion = null;
        log.info(parametersMap.get(QueryParameterNames.version).get(0));
        if (!parametersMap.get(QueryParameterNames.version).get(0).isEmpty()
                && !parametersMap.get(QueryParameterNames.version).get(0).equals("")) {
            designPathVersion = Integer.valueOf(parametersMap.get(QueryParameterNames.version).get(0));
        }

        // use api to fetch the parent asset and the list of assets
        designPath = opticsApi.getProject(designPathId, designPathVersion);
        log.info("Design path is {} and deatails are {}", designPath.key, designPath.toString());
        parentProject = opticsApi.getProjectNoData(designPath.parentKey, null);
        List<Asset> assets = opticsApi.getDesignPaths(designPathId, null).assets;

        // We add the design path to the "1" position
        assets.add(0, designPath);
        //We add the primary project to the "0" position
        assets.add(0, parentProject);

        // fetch asset list
        String key = NavigationKeyStrings.selectedProjectDetails;
        List<Asset> allAssets = new ArrayList<>();
        List<Asset> sessionAssets = assets;

        if (!sessionAssets.isEmpty()) {
            allAssets = sessionAssets;
            log.info("session assets found");
        }

        //log.info("The asset list is: " + allAssets.toString());

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
            allAssets.remove(0);
            allAssets.remove(0);


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

            // Get prior version of the design as well


            setGrid(allAssets);
            //log.ingo("The design path clicked it:" + designPath.toString());

            // Asset grid
            assetGrid.setAssetGridView(allAssets);

            try {
                setProjectDetailsView(designPath, designPath.version, allAssets, true);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }


    }

    public ProjectDetailsView() {

        setClassName("project-details-view-page");
        add(closeButton);
        closeButton.setVisible(false);

        //Project Header Portion
        HorizontalLayout headerHorizontalLayout = new HorizontalLayout();
        headerHorizontalLayout.setWidthFull();
        Div imageDiv = new Div();
        // define svg
        //title_svg.add(projectName, svgImage);

        HeaderLayout.add(projectName);
        //HeaderLayout.add(svgImage);
        HeaderLayout.add(projectVersion);
        HeaderLayout.add(projectDescription);
        HeaderLayout.add(projectImg);
        HeaderLayout.setJustifyContentMode(JustifyContentMode.START);
        HeaderLayout.setClassName("project-details-view-header");

        imageDiv.add(svgImage);
        imageDiv.setWidthFull();
        imageDiv.getStyle().set("padding-top","90px");

        headerHorizontalLayout.add(HeaderLayout, imageDiv);
        headerHorizontalLayout.setJustifyContentMode(JustifyContentMode.START);

       // imageDiv.getStyle().set("margin-left","60px");

        add(headerHorizontalLayout);

        //add(svgImage);


        // Project Tools and Requirements
        projectFunctionsLayout.setWidthFull();
        requirementsLayout.setSizeFull();
        //Tools Portion
        toolsLayout.setSizeFull();

        projectFunctionsLayout.add(toolsLayout, requirementsLayout);
        add(projectFunctionsLayout);

        //Project Metadata Portion
        metadataView.setWidthFull();
        add(metadataView);
        /*

         */
        add(assetListLayout);

        H5 assetList = new H5("Asset List");
        assetList.addClickListener(event -> {
            // close the other view

            HeaderLayout.setVisible(false);
            projectFunctionsLayout.setVisible(false);
            metadataView.setVisible(false);
            assetListLayout.setVisible(false);
            closeButton.setVisible(true);

            assetGrid.setVisible(true);

            assetGrid.setSizeFull();

            closeButton.addClickListener(event1 -> {
                HeaderLayout.setVisible(true);
                projectFunctionsLayout.setVisible(true);
                metadataView.setVisible(true);
                assetListLayout.setVisible(true);

                assetGrid.setVisible(false);
                closeButton.setVisible(false);
            });


        });
        assetList.setClassName("project-details-asset-list");
        assetListLayout.add(assetList, gridDiv);
        assetListLayout.setSizeFull();
        assetListLayout.setWidthFull();
        assetListLayout.setClassName("project-details-asset-list-layout");
        //assetListLayout.add(gridDiv);

        gridDiv.add(filesGrid);
        gridDiv.setHeightFull();
        gridDiv.setWidthFull();
        //add(gridDiv);

        add(updateDialog);
        //   add(breadcrumbLayout);
        //breadcrumbLayout.setJustifyContentMode(JustifyContentMode.START);


        add(versionLayout);
        versionLayout.setVisible(false);

        add(assetGrid);
        assetGrid.setVisible(false);

        //filesGrid.setVisible(false);


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
        projectVersion.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        projectVersion.setText(projectVersionString);
        projectVersion.getStyle().set("text-decoration", "underline");
        log.info("setting project version layout called");
        if (populateVersions) {
            populateVersionLayout(project);
        }
        projectVersion.addClickListener(event -> showVersionLayout());

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
            setFunctionLayout(project, assetList, null);
        } else {
            metadataView.setVisible(true);
            Asset zemaxAssetPrior = zemaxAsset;
            log.info("asset version is : " + zemaxAsset.version);
            if (zemaxAsset.version > 0) {
                zemaxAssetPrior = getPriorDesign(zemaxAsset);
            }
            // log.info("Current: " + zemaxAsset + "Prior : " + zemaxAssetPrior);
            metadataView.setMetadataViewLayout(zemaxAsset, zemaxAssetPrior);
            getSystemVisualizationPoints(zemaxAsset);
            setFunctionLayout(project, assetList, zemaxAsset);

        }

        gridDiv.setHeightFull();
        gridDiv.setWidthFull();
        // add(filesGrid);


        gridDiv.add(filesGrid);

        if (assetList.size() >= 0) {
            setGrid(assetList);
        }

        filesGrid.addItemClickListener(e -> {
            Asset selectedAsset = e.getItem();
            //log.ingo("The asset with version: " + selectedAsset.version + "was clicked");
            updateProjectDialog(selectedAsset);
            updateDialog.open();

        });


    }

    public void populateVersionLayout(Asset project) {
        //versionGrid.removeAllColumns();

        List<DesignPathVersionClass> pathVersions = new ArrayList<>();
        Asset asset = opticsApi.getProjectNoData(project.key, null);
        //log.info("The maximum version is: " + asset.version);
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");

        log.info("The metadata is: " + asset.metadata.messages.toString());
        // Loop through all versions
        for (int pathVersion = 0; pathVersion <= asset.version; pathVersion++) {
            // By default, report there is no commit message and the date of the commit is unknown
            String defaultCommit = new String("No commit message is available for this version. DATE = NA");

            // If the commit message is present for the version, overwrite the commit message
            if (asset.metadata.messages != null &&
                    asset.metadata.messages.containsKey("commit - " + pathVersion)) {
                defaultCommit = asset.metadata.messages.get("commit - " + pathVersion).toString();
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

        versionGrid.setColumns("versionNumber", "versionCommitMessage", "versionCreationDate");
        log.info("version items : " + pathVersions);
        versionGrid.setItems(pathVersions);

        versionGrid.getColumnByKey("versionNumber").setHeader("Version");
        versionGrid.getColumnByKey("versionNumber").setFlexGrow(0).setWidth("90px");
        versionGrid.getColumnByKey("versionNumber").setResizable(true);
        versionGrid.getColumnByKey("versionCommitMessage").setHeader("Message");
        versionGrid.getColumnByKey("versionCommitMessage").setResizable(true);
        versionGrid.getColumnByKey("versionCreationDate").setHeader("Date");
        versionGrid.getColumnByKey("versionCreationDate").setResizable(true);

        List<GridSortOrder<DesignPathVersionClass>> sortByVersion = new GridSortOrderBuilder<DesignPathVersionClass>()
                .thenDesc(versionGrid.getColumnByKey("versionNumber")).build();
        versionGrid.sort(sortByVersion);
        // filesGrid.setPageSize(pathVersions.size());
        versionGrid.setHeightByRows(true);
        https:
//github.com/redisson/redisson#quick-start
        versionLayout.removeAll();

        versionLayout.add(new Span(project.assetName));

        versionLayout.add(versionGrid);

        versionGrid.addItemClickListener(
                event -> {
                    log.info("event " + event.toString());

                    //versionGrid.removeAllColumns();

                    getDetailsForVersion(project,
                            event.getItem().
                                    versionNumber);
                    closeButton.click();

                });

    }

    public void showVersionLayout() {
        closeButton.setVisible(true);


        svgImage.setVisible(false);
        HeaderLayout.setVisible(false);
        projectFunctionsLayout.setVisible(false);
        metadataView.setVisible(false);
        assetListLayout.setVisible(false);

        versionLayout.setVisible(true);


        closeButton.addClickListener(event -> {
            HeaderLayout.setVisible(true);
            projectFunctionsLayout.setVisible(true);
            metadataView.setVisible(true);
            assetListLayout.setVisible(true);

            versionLayout.setVisible(false);
            closeButton.setVisible(false);
        });
    }


    public void getDetailsForVersion(Asset designPath, Integer version) {

        Asset selectedVersionDesignPath = opticsApi.getProjectNoData(designPath.key, version);

        // default to empty, if the asset has data it will change
        metadataView.setVisible(false);
        svgImage.setVisible(false);

        // Fetch the list of assets for the given design path version
        List<Asset> assets = new ArrayList<>();
        if (version == designPath.getVersion()) {
            assets = opticsApi.getDesignPaths(designPath.getKey(), null).assets;
        } else {
            assets = opticsApi.getDesignPaths(designPath.getKey(), version).assets;

        }

        // reset the page view
        try {
            log.info(selectedVersionDesignPath.toString() + "version : " + version);
            setProjectDetailsView(selectedVersionDesignPath, version, assets, false);
        } catch (IOException e) {
            e.printStackTrace();
        }
        /*

        //We add the primary project to the "0" position
        assets.add(0, parentProject);
        log.info("primary project added to 0 position");
        // We add the design path to the "1" position
        assets.add(1, selectedVersionDesignPath);
        log.info("design path added to the 1 pos");

        // log.info("The card gird level assets are" + assets.toString().substring(0, 20));
        UI.getCurrent().getSession().setAttribute("designPathClicked", true);

        Component c = UI.getCurrent();
        String key = NavigationKeyStrings.selectedProjectDetails;
        ComponentUtil.setData(c, key, assets);
        log.info("asset list is now: " + assets.toString());
        UI.getCurrent().getPage().reload();
        */
    }

    private Asset getPriorDesign(Asset designFile) {
        //  log.info("The current file is : " + designFile.toString());
        Asset priorDesign = opticsApi.getFile(designFile.key, designFile.version - 1);
        // log.info("The new file is:  " + priorDesign);

        return priorDesign;
    }


    private Asset checkOpticalDesignFile(List<Asset> assetList) {
        Asset zemaxAsset = new Asset();

        ListIterator<Asset> assetIterator = assetList.listIterator();

        while (assetIterator.hasNext()) {
            Asset tempAsset = assetIterator.next();

            log.info("temp asset is {}", tempAsset);

            //log.ingo("the messages is: " + tempAsset.toString());
            if (tempAsset.metadata.opticalMetadata != null) {
                if (tempAsset.metadata.opticalMetadata.isPresent() &&
                        !tempAsset.metadata.opticalMetadata.get().prescription.zLength.equals(null)) {
                    // Assign the zemax file
                    zemaxAsset = tempAsset;
                }
            }
        }

        return zemaxAsset;

    }

    private void setGrid(List<Asset> assets) {
        filesGrid.setItems(assets);

        setHeightFull();
        setWidthFull();

        filesGrid.setColumns("assetName", "tags", "version");
        filesGrid.addColumn(new LocalDateTimeRenderer<>(
                Asset::getLastModified,
                "HH:mm MM/dd/yyyy")
        ).setHeader("Last Modified").setSortable(true);


        filesGrid.setSelectionMode(Grid.SelectionMode.NONE);

    }

    private void setToolsLayout(Asset project, List<Asset> assetList, @Nullable Asset designFile) {
        toolsLayout.removeAll();
        H5 toolsHeader = new H5("Tools");
        toolsHeader.setClassName("tools-header");
        toolsLayout.add(toolsHeader);
        toolsLayout.setClassName("project-details-tools-layout");

        HorizontalLayout toolOptions = new HorizontalLayout();

        Button downloadIcon = new Button("Download", new Icon(VaadinIcon.CLOUD_DOWNLOAD_O));
        Anchor downloadAnchor = new Anchor("/download?parentID=" + designPath.key + "&version=" +
                designPath.version, downloadIcon);

        // Tool Buttons
        Button addIcon = new Button("Add Files", new Icon(VaadinIcon.FILE_ADD));
        Button trashIcon = new Button("Delete", new Icon(VaadinIcon.TRASH));
        Button shareIcon = new Button("Share", new Icon(VaadinIcon.SHARE_SQUARE));
        shareIcon.setVisible(false);

        if (designFile != null) {
            shareIcon.setVisible(true);

            Dialog shareDialog = createShareDialog(project, designFile);

            add(shareDialog);
            shareIcon.addClickListener(event -> {
                shareDialog.open();
            });
        }

        Dialog confirmDialog = createDownloadConfirmDialog(project);
        add(confirmDialog);


        addIcon.addClickListener(e -> {


            // We add the design path to the "1" position
            assetList.add(0, designPath);
            //We add the primary project to the "0" position
            assetList.add(0, parentProject);

            Component c = UI.getCurrent();
            String key = NavigationKeyStrings.selectedProjectAssets;
            ComponentUtil.setData(c, key, assetList);

            UI.getCurrent().navigate(NewFiles.class);
        });
        trashIcon.addClickListener(e ->
                confirmDialog.open());

        toolOptions.add(downloadAnchor, addIcon, trashIcon, shareIcon);
        toolsLayout.add(toolOptions);


    }

    private void setRequirementsLayout(Asset project) {
        requirementsLayout.removeAll();
        H5 requirementsHeader = new H5("Requirements");
        requirementsHeader.setClassName("requirements-header");
        Span focalLengthRequirement = new Span("Coming Soon.");
        requirementsLayout.add(requirementsHeader, focalLengthRequirement);

    }

    private void setFunctionLayout(Asset project, List<Asset> assetList, @Nullable Asset designFile) {

        projectFunctionsLayout.removeAll();

        //Tools portion
        setToolsLayout(project, assetList, designFile);
        setRequirementsLayout(project);

        //Requirements portion
        requirementsLayout.setSizeFull();
        //Tools Portion
        toolsLayout.setSizeFull();

        projectFunctionsLayout.add(toolsLayout, requirementsLayout);


    }


    private Dialog createDownloadConfirmDialog(Asset project) {
        Dialog dialog = new Dialog();

        dialog.setCloseOnEsc(false);
        dialog.setCloseOnOutsideClick(false);

        Span message = new Span("Are you sure you want to delete this design path?" +
                " This cannot be undone and will delete the design path and all files in this path.");

        Button confirmButton = new Button("Confirm", event -> {
            message.setText("Confirmed!");
            dialog.close();
            deleteProject(project);

        });
        Button cancelButton = new Button("Cancel", event -> {
            message.setText("Cancelled...");
            dialog.close();
        });
        dialog.add(message, new HorizontalLayout(confirmButton, cancelButton));
        return dialog;
    }

    @SneakyThrows
    private Dialog createShareDialog(Asset project, Asset designFile) {
        Dialog dialog = new Dialog();

        dialog.setCloseOnEsc(true);
        dialog.setCloseOnOutsideClick(true);

        // Establish the query parameters map
        Map<String, String> queryMap = new HashMap<>();
        queryMap.put(QueryParameterNames.projectId, project.key);
        queryMap.put(QueryParameterNames.version, project.version.toString());
        // URL name
        Map<String, Object> jwtClaims = SecurityUtils.getJWTClaims();
        String apiUrl = jwtClaims.get("http://eleoptics.com/asset-server-uri").toString() ;


        queryMap.put(QueryParameterNames.apiURL, apiUrl);
        QueryParameters queryParameters = QueryParameters.simple(queryMap);


        // Set a router link t the detail page
        RouterLink detailsPageLink = new RouterLink();
        detailsPageLink.setRoute(ReportView.class);
        detailsPageLink.setQueryParameters(queryParameters);

        VaadinServletRequest cr = (VaadinServletRequest) VaadinService.getCurrentRequest();
        String urlName = cr.getServerName();
        String urlSecurity = "http";
        String urlPort = "";
        /*
        if (cr.getLocalPort() != 80 && cr.getLocalPort() != 443) {
            urlPort = ":" + cr.getLocalPort();
        }
        ;
        */

        String shareUrl = urlSecurity + "://" + urlName + urlPort + "/" + detailsPageLink.getHref();

        H4 linkHeader = new H4("Get share link");
        //Button link = new Button(shareUrl.substring(0,12) + "...");
        //link.getStyle().set("overflow", "hidden");
        Anchor linkAnchor = new Anchor(shareUrl, shareUrl);
        linkAnchor.setTarget("_blank");
        linkAnchor.getStyle().set("margin-bottom", "6 px");

        // Create text and share link
        H3 message = new H3("Share a view only report");
        Icon shareIcon = new Icon(VaadinIcon.LINK);
        shareIcon.getStyle().set("margin-top", "16px");
        Button copyLinkButton = new Button("Copy Link");
        copyLinkButton.addThemeVariants(ButtonVariant.MATERIAL_CONTAINED);
        copyLinkButton.setEnabled(false);

        // Create clipboard action
        ClipboardHelper clipboardHelper = new ClipboardHelper(shareUrl, copyLinkButton);

        // Create the share or private selector:
        Select<String> labelSelect = new Select<>();
        String restrictedSelection = "Restricted. Only workspace members can view and edit.";
        String shareSelection = "Public. Anyone with the share link can see a view only version of the project.";
        labelSelect.setItems(restrictedSelection, shareSelection);
        labelSelect.setLabel("Share");
        labelSelect.setPlaceholder("Restricted");



        ArrayList<String> assetIds = new ArrayList<String>();
        assetIds.add(project.key);
        assetIds.add(designFile.key);

        AssetsForSharing assetKeys = new AssetsForSharing();
        assetKeys.ids = assetIds;


        if(project.isPublic){
            copyLinkButton.setEnabled(true);
        }

        // Create options for select
        labelSelect.addValueChangeListener(event -> {
            if (labelSelect.getValue().equals(restrictedSelection) && project.isPublic) {
                copyLinkButton.setEnabled(false);
                makeProjectPublicShare(assetKeys);
            } else if (labelSelect.getValue().equals(shareSelection) && !project.isPublic) {
                copyLinkButton.setEnabled(true);
                makeProjectPublicShare(assetKeys);
            }
        });


        // Define button actions
        Button doneButton = new Button("Done", event -> {
            dialog.close();
        });
        doneButton.addThemeVariants(ButtonVariant.MATERIAL_CONTAINED);

        // Add components to dialog
        dialog.add(message,
                new HorizontalLayout(shareIcon, linkHeader),
                new VerticalLayout(labelSelect, linkAnchor),
                new HorizontalLayout(clipboardHelper, doneButton)
        );
        return dialog;
    }

    @SneakyThrows
    private void makeProjectPublicShare(AssetsForSharing assetIds) {
        // flip the status of the public viewability of an asset
        //log.ingo("the upload is: " + assetBeingUploaded.toString());
        opticsApi.makePublic(assetIds);

    }

    private void deleteProject(Asset projectForDeletion) {
        Asset mostUpdatedProjectForDeletion = opticsApi.getProjectNoData(projectForDeletion.key, null);


        // Delete the current design path
        opticsApi.deleteAsset(projectForDeletion.getKey(), mostUpdatedProjectForDeletion.version);


        // Navigate back to the parent project design path page
        UI.getCurrent().navigate(
                DesignPathGrid.class,
                parentProject.key);


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

                    log.info("SVG : " + svgString.toString().substring(0, 20));


                    byte[] imageBytes = svgString.getBytes(StandardCharsets.UTF_8);

                    StreamResource resource = new StreamResource("dummyImageName.svg", () -> new ByteArrayInputStream(imageBytes));

                    svgImage.setSrc(resource);
                    svgImage.setVisible(true);
                }
            }
        }


    }

    private Optional<VisualizationData> getDataPoints(Asset assetFile, ArrayList<Integer> samplingPoints, OpticsApi opticsApi) {
        VisualizationData dataPoints = null;

        try {
            dataPoints = opticsApi.
                    getVisualizationPoints(
                            assetFile.key,
                            assetFile.version,
                            samplingPoints,
                            numberOfRays);
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


    private void updateProjectDialog(Asset asset) {

        updateDialog.removeAll();

        updateDialog.setCloseOnEsc(true);
        updateDialog.setCloseOnOutsideClick(true);

        Span assetDescription = new Span(asset.description);

        TextArea uploadText = new TextArea();
        uploadText.setPlaceholder("Please provide a description (at least 30 characters) of the updated file changes.");
        uploadText.setMinLength(30);

        //Attempt to determine file extension of existing file and set that as the only allowed upload type.
        HashMap<String, HashMap<String, Object>> assetDetails = asset.getData();

        Button downloadButton = new Button("Download Asset", new Icon(VaadinIcon.DOWNLOAD_ALT));
        downloadButton.addClickListener(event -> updateDialog.close());

        Anchor download = new Anchor("/download?fileID=" + asset.key + "&version=" + asset.version, "");
        download.add(new Button("Download Asset", new Icon(VaadinIcon.DOWNLOAD_ALT)));
        //        download.setTarget("_blank");


        // Upload
        MemoryBuffer buffer = new MemoryBuffer();
        Upload upload = new Upload(buffer);
        upload.setMaxFileSize(50000000);
        if (asset.metadata.opticalMetadata.isPresent()) {
            upload.setAcceptedFileTypes(".zmx, .seq");
        }
        Button uploadButton = new Button("Update");
        upload.setUploadButton(uploadButton);
        //upload.setAcceptedFileTypes(fileExtension);

        Span dropLabel = new Span("Drag and drop to upload.");
        upload.setDropLabel(dropLabel);
        Div output = new Div();
        upload.setClassName("file-upload-button");


        AssetDataForProjectUpload assetData = new AssetDataForProjectUpload();

        Metadata assetMetadta = new Metadata();
        assetMetadta.messages = asset.metadata.messages;

        AssetObjectForUpdate assetBeingUploaded = new AssetObjectForUpdate(
                asset.key,
                asset.parentKey,
                asset.assetName,
                asset.assetType,
                asset.tags,
                asset.description,
                assetMetadta);


        Button confirmButton = new Button("Send Update", event -> {
            updateDialog.close();
            try {
                sendUpdate(assetBeingUploaded, assetData, uploadText.getValue(), asset.version);
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
        });

        confirmButton.setEnabled(false);
        Button cancelButton = new Button("Cancel", event -> {
            updateDialog.close();
        });

        uploadText.setVisible(false);
        upload.addSucceededListener(event ->

        {
            Component component = createComponent(event.getMIMEType(),
                    event.getFileName(), buffer.getInputStream());
            try {
                //Get bytes, convert to string, and set assetData
                InputStream is = buffer.getInputStream();
                File tempFile = new File(event.getFileName());
                FileUtils.copyInputStreamToFile(is, tempFile);
                FileSystemResource uploadFile = new FileSystemResource(tempFile);

                assetData.setFile(uploadFile);

            } catch (IOException e) {
                e.printStackTrace();
            }
            showOutput(event.getFileName(), component, output);


            uploadText.setVisible(true);

        });

        Span updateNotifierText = new Span();
        uploadText.addValueChangeListener(event -> {

            if (uploadText.getValue().length() >= 50) {
                confirmButton.setEnabled(true);
                updateNotifierText.setText("");

            }
            if (uploadText.getValue().length() < 50) {
                confirmButton.setEnabled(false);
                updateNotifierText.setText("Please add a few more words to your description.");

            }
        });

        HorizontalLayout commitMessageLayout = new HorizontalLayout(uploadText);
        uploadText.setWidthFull();
        commitMessageLayout.setWidthFull();
        updateDialog.add(assetDescription, upload, commitMessageLayout, updateNotifierText,
                new HorizontalLayout(download, confirmButton, cancelButton));

    }

    private void sendUpdate(AssetObjectForUpdate assetBeingUploaded, AssetDataForProjectUpload assetData,
                            String updateString, Integer assetCurrentVersion)
            throws JsonProcessingException {

        HashMap<String, String> commitMap = new HashMap<String, String>();
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
        LocalDateTime now = LocalDateTime.now();

        commitMap.put("commit - " + (assetCurrentVersion + 1), assetBeingUploaded.assetName +
                " updated. " + updateString + "DATE = " + dtf.format(now));
        assetBeingUploaded.setMetadata(commitMap);

        //log.ingo("the upload is: " + assetBeingUploaded.toString());
        opticsApi.
                patchAsset(assetBeingUploaded, assetData);
        //designPath.version = designPath.version;


        //refresh
        designPath.version = designPath.version + 1;

        Map<String, String> queryMap = new HashMap<>();
        queryMap.put(QueryParameterNames.projectId, designPath.key);
        queryMap.put(QueryParameterNames.version, designPath.version.toString());
        QueryParameters queryParameters = QueryParameters.simple(queryMap);
        // Set a router link t the detail page
        RouterLink detailsPageLink = new RouterLink();
        detailsPageLink.setRoute(ProjectDetailsView.class);

        // Navigate to the details page with the query parameters
        UI.getCurrent().navigate(detailsPageLink.getHref(), queryParameters);

    }

    private Component createComponent(String mimeType, String fileName,
                                      InputStream stream) {
        if (mimeType.startsWith("text")) {
            return createTextComponent(stream);
        } else if (mimeType.startsWith("image")) {
            Image image = new Image();
            try {

                byte[] bytes = IOUtils.toByteArray(stream);
                image.getElement().setAttribute("src", new StreamResource(
                        fileName, () -> new ByteArrayInputStream(bytes)));
                try (ImageInputStream in = ImageIO.createImageInputStream(
                        new ByteArrayInputStream(bytes))) {
                    final Iterator<ImageReader> readers = ImageIO
                            .getImageReaders(in);
                    if (readers.hasNext()) {
                        ImageReader reader = readers.next();
                        try {
                            reader.setInput(in);
                            image.setWidth(reader.getWidth(0) + "px");
                            image.setHeight(reader.getHeight(0) + "px");
                        } finally {
                            reader.dispose();
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            return image;
        }
        Div content = new Div();
        String text = String.format("Mime type: '%s'\nSHA-256 hash: '%s'",
                mimeType, MessageDigestUtil.sha256(stream.toString()));
        content.setText(text);
        return content;

    }

    private Component createTextComponent(InputStream stream) {
        String text;
        text = stream.toString();

        return new Text(text);
    }

    private void showOutput(String text, Component content,
                            HasComponents outputContainer) {
        HtmlComponent p = new HtmlComponent(Tag.P);
        p.getElement().setText(text);
        outputContainer.add(p);
        outputContainer.add(content);
    }

    private String toBinary(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * Byte.SIZE);
        for (int i = 0; i < Byte.SIZE * bytes.length; i++)
            sb.append((bytes[i / Byte.SIZE] << i % Byte.SIZE & 0x80) == 0 ? '0' : '1');
        return sb.toString();
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


    //   */

}
