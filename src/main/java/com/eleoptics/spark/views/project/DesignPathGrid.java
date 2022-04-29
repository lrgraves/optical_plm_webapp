package com.eleoptics.spark.views.project;

import com.eleoptics.spark.api.*;
import com.eleoptics.spark.cache.AssetDAO;
import com.eleoptics.spark.config.SecurityUtils;
import com.eleoptics.spark.eventbus.EventBusFactory;
import com.eleoptics.spark.events.*;
import com.eleoptics.spark.views.QueryParameterNames;
import com.eleoptics.spark.views.main.MainView;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.github.appreciated.card.RippleClickableCard;
import com.github.appreciated.css.grid.GridLayoutComponent;
import com.github.appreciated.css.grid.sizes.Length;
import com.github.appreciated.css.grid.sizes.MinMax;
import com.github.appreciated.css.grid.sizes.Repeat;
import com.google.common.eventbus.Subscribe;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.ComponentUtil;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridSortOrder;
import com.vaadin.flow.component.grid.GridSortOrderBuilder;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.*;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@Route(value = "designpaths", layout = MainView.class)
@PreserveOnRefresh
@PageTitle("DESIGN PATHS")
@CssImport(value = "./styles/style.css", include = "lumo-badge")
@JsModule("./styles/shared-styles.js")
@Slf4j
public class DesignPathGrid extends VerticalLayout implements HasUrlParameter<String> {
    @Autowired
    OpticsApi opticsApi;

    ArrayList<RippleClickableCard> designPathCards = new ArrayList<>();
    Asset parentProject = new Asset();
    String parentProjectID = new String();
    List<Asset> projectAssets = new ArrayList<>();
    VerticalLayout versionLayout = new VerticalLayout();
    Grid<DesignPathVersionClass> filesGrid = new Grid<>(DesignPathVersionClass.class);
    HorizontalLayout projectsAndSearchLayout = new HorizontalLayout();

    HorizontalLayout designGrid = new HorizontalLayout();
    Icon closeIcon = new Icon(VaadinIcon.CLOSE);
    Button closeVersionsButton = new Button("", closeIcon);
    //DesignPathEventListener designPathEventListener = new DesignPathEventListener(opticsApi);
    HorizontalLayout topBar = new HorizontalLayout();

    FilterProjects filterProjectsTool = new FilterProjects();

    // Asset list for the project
    List<Asset> designPathList = new ArrayList<>();
    List<Asset> originalFullDesignPathList = new ArrayList<>();
    List<Asset> allAssets = new ArrayList<>();

    // SVG
    RenderSVG renderSVG = new RenderSVG();
    Integer samplePoints = 20;
    Integer svgCardHeight = 80;
    Integer svgCardWidth = 264;
    Integer svgPadding = 10;

    String userID = SecurityUtils.getUserID();


    @SneakyThrows
    @Override
    public void setParameter(BeforeEvent event, String parameter) {
        // fetch parent project ID
        parentProjectID = parameter;

        // use api to fetch the parent asset and the list of assets
        parentProject = opticsApi.getProjectNoData(parentProjectID, null);

        designPathList = opticsApi.getDesignPaths(
                parentProjectID,
                null).
                assets;


        designPathList.add(0, parentProject);

        // log.info("The asset list is now:"+ designPathList.toString());
        log.info("Design path list here: {}", designPathList.toString());

        // The parent project is the first project in the list
        projectAssets = designPathList;
        // Then, remove it from the list

        // this is returning the detailed asset metadata i need for filtering. meaning that I need to
        // use this list for filtering, and just say anything that has this design path as the parent key, and is the l
        // latest version is the filter list.
        allAssets = opticsApi.getAllAssets().assets;
        //log.info("The full asset list is: " + allAssets.toString().trim(20));


        // Add event bubbler for the main view
        // EventBusFactory.getEventBus().post(new DesignPathsEvent(parentProject));

        // designPathEventListener = new DesignPathEventListener(opticsApi);

        // constructEventBus();


        try {


            originalFullDesignPathList = designPathList;
            originalFullDesignPathList.remove(0);
            filterProjectsTool.defineProjectLists(originalFullDesignPathList, allAssets);

            //log.info("original list 1st time: {}", originalFullDesignPathList.toString().substring(0, 15));
            populateGrid(originalFullDesignPathList);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private Consumer<Asset> versionHandler = new Consumer<Asset>() {
        @Override
        public void accept(Asset asset) {
            populateVersionLayout(asset);
        }
    };

    public DesignPathGrid() {

        filterProjectsTool.getStyle().set("margin-right", "20px");
        filterProjectsTool.getStyle().set("padding-right", "16px");
        projectsAndSearchLayout.getStyle().set("margin-top", "20px");
        filterProjectsTool.getStyle().set("width", "auto");


        filterProjectsTool.filterHandler(filteredList -> {
            try {
                populateGrid(filteredList);
            } catch (IOException e) {
                e.printStackTrace();
            }
            log.info("The search button was clicked");
        });


        projectsAndSearchLayout.add(designGrid, filterProjectsTool);
        projectsAndSearchLayout.setWidthFull();
        add(closeVersionsButton);

        add(versionLayout);


        add(projectsAndSearchLayout);


        closeVersionsButton.setVisible(false);
        closeVersionsButton.addThemeVariants(ButtonVariant.LUMO_CONTRAST);

    }

    @SneakyThrows
    private String designPathSVG(Asset system) {
        // Search through entire asset list for the assets with matching the system id

        String designString = null;
        for (int pos = 0; pos < allAssets.size(); pos++) {
            Asset currentFile = allAssets.get(pos);
            // Check if the file is for the given parent design
            if (currentFile.parentKey.equals(system.key) && currentFile.metadata.opticalMetadata.isPresent() &&
                    !currentFile.metadata.opticalMetadata.get().prescription.zLength.equals(null)) {
                // This is a matching zemax file for the parent asset, get the SVG line
                ArrayList<Integer> samplingPoints = new ArrayList<>();
                OpticalMetadata metadata = currentFile.getMetadata().opticalMetadata.get();
                List<Integer> numSurfaces = metadata.prescription.numberOfInterfaceTypes;

                var sum = numSurfaces.stream()
                        .mapToInt(Integer::intValue).sum();
                samplingPoints.addAll(Collections.nCopies(sum, samplePoints));

                Optional<VisualizationData> dataPoints = getDataPoints(currentFile, samplingPoints, opticsApi);


                if (dataPoints.isPresent()) {
                    if (dataPoints.get().system != null) {
                        if (dataPoints.get().system.elements != null) {
                            designString = renderSVG.createDoc(currentFile, svgCardHeight, svgCardWidth, svgPadding, dataPoints.get(), false);
                        }
                    }
                }
            }
        }
        return designString;

    }

    private Optional<VisualizationData> getDataPoints(Asset assetFile, ArrayList<Integer> samplingPoints, OpticsApi opticsApi) {
        VisualizationData dataPoints = null;

        try {
            dataPoints = opticsApi.
                    getVisualizationPoints(
                            assetFile.key,
                            assetFile.version,
                            samplingPoints,
                            null);
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


    public void gridClickHandler(ComponentEvent event) {
        log.info("clicked card" + event.getSource().toString());
        if (handler != null) {
            DesignPathCard clickedCard = (DesignPathCard) event.getSource();
            DesignPathCardInformation cardInformation = new DesignPathCardInformation(projectAssets, clickedCard.cardProject);
            handler.accept(cardInformation);
        }
    }


    private Consumer<DesignPathCardInformation> handler;

    private void addCardClickHandler(Consumer<DesignPathCardInformation> clickHandler) {
        handler = clickHandler;
    }


    public void populateGrid(List<Asset> assets) throws IOException {
        designPathCards.clear();
        designGrid.removeAll();


        if (assets.size() > 0) {
            List<Asset> projectList = assets.get(0).getAllProjects(assets);

            if (projectList.size() > 0) {
                log.info("the asset list is " + projectList.size() + "long");

                // Make list of project cards
                for (int pos = 0; pos < projectList.size(); pos++) {
                    Asset project = projectList.get(pos);
                    String svgString = designPathSVG(project);

                    RippleClickableCard newProjectCard = DesignPathCard.createCard(project, svgString, versionHandler);
                    newProjectCard.addClickListener(event -> gridClickHandler(event));
                    designPathCards.add(newProjectCard);
                }
                ;
            }
        }
        setFlexibleGridLayout(designPathCards, parentProject);


    }

    public void populateVersionLayout(Asset project) {
        //filesGrid.removeAllColumns();
        topBar.setVisible(false);
        closeVersionsButton.setVisible(true);
        versionLayout.setHeightFull();

        List<DesignPathVersionClass> pathVersions = new ArrayList<>();
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");

        log.info("The metadata is: " + project.metadata.messages.toString());
        for (int pathVersion = 0; pathVersion <= project.version; pathVersion++) {
            String defaultCommit = new String("No commit message is available for this version. DATE = NA");
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

            log.info("made it here brotha");
            String versionDate = "Unknown";
            if (updateMessage.length > 1) {
                log.info(String.valueOf(updateMessage.length));
                versionDate = updateMessage[1];
            }
            Integer currentVersionNumber = pathVersion;

            pathVersions.add(pathVersion, new DesignPathVersionClass(currentVersionNumber, currentVersionCommit, versionDate));
            //log.info("the version went to" + pathVersion);
        }

        filesGrid.setColumns("versionNumber", "versionCommitMessage", "versionCreationDate");
        filesGrid.setItems(pathVersions);

        filesGrid.getColumnByKey("versionNumber").setHeader("Version");
        filesGrid.getColumnByKey("versionNumber").setFlexGrow(0).setWidth("90px");
        filesGrid.getColumnByKey("versionNumber").setResizable(true);
        filesGrid.getColumnByKey("versionCommitMessage").setHeader("Message");
        filesGrid.getColumnByKey("versionCommitMessage").setResizable(true);
        filesGrid.getColumnByKey("versionCreationDate").setHeader("Date");
        filesGrid.getColumnByKey("versionCreationDate").setResizable(true);

        List<GridSortOrder<DesignPathVersionClass>> sortByVersion = new GridSortOrderBuilder<DesignPathVersionClass>()
                .thenDesc(filesGrid.getColumnByKey("versionNumber")).build();
        filesGrid.sort(sortByVersion);
        // filesGrid.setPageSize(pathVersions.size());
        filesGrid.setHeightByRows(true);
        //filesGrid.setHeightFull();


        versionLayout.removeAll();

        versionLayout.add(new Span(project.assetName));

        versionLayout.add(filesGrid);

        projectsAndSearchLayout.setVisible(false);
        versionLayout.setVisible(true);


        filesGrid.addItemClickListener(
                event -> {
                    //Notification versionClickedNotifier = new Notification("Version selection coming soon.");
                    //versionClickedNotifier.open();
                    //versionClickedNotifier.setDuration(2000);
                    getDetailsForVersion(project, event.getItem().versionNumber);
                });

        closeVersionsButton.addClickListener(event1 -> {
            projectsAndSearchLayout.setVisible(true);
            versionLayout.setVisible(false);
            versionLayout.removeAll();
            closeVersionsButton.setVisible(false);
        });
    }

    private List<Asset> getAllDesignPaths(Asset project) {
        List<Asset> assets = opticsApi.
                getDesignPaths(
                        project.key,
                        null).
                assets;
        assets.add(0, project);

        return assets;
    }

    public void getDetailsForVersion(Asset designPath, Integer version) {

        log.info("project card design path clicked");
        // Establish the query parameters map
        Map<String, String> queryMap = new HashMap<>();
        queryMap.put(QueryParameterNames.projectId, designPath.key);
        queryMap.put(QueryParameterNames.version, version.toString());
        QueryParameters queryParameters = QueryParameters.simple(queryMap);

        // Set a router link t the detail page
        RouterLink detailsPageLink = new RouterLink();
        detailsPageLink.setRoute(ProjectDetailsView.class);

        // Navigate to the details page with the query parameters
        UI.getCurrent().navigate(detailsPageLink.getHref(), queryParameters);
    }

    public void setCardGridView(List<Asset> projectList) {
        // define svglist again

        // Make list of project cards
        for (int pos = 0; pos < projectList.size(); pos++) {
            Asset project = projectList.get(pos);
            String svgString = designPathSVG(project);
            designPathCards.add(DesignPathCard.createCard(project, svgString, versionHandler));

        }


        setFlexibleGridLayout(designPathCards, parentProject);
    }


    public void setFlexibleGridLayout(List<RippleClickableCard> designPathCards, Asset parentProject) {
        designGrid.removeAll();
        com.github.appreciated.layout.FlexibleGridLayout layout = new com.github.appreciated.layout.FlexibleGridLayout()
                .withColumns(Repeat.RepeatMode.AUTO_FILL, new MinMax(new Length("264px"), new Length("266px")))
                .withAutoRows(new Length("264px"))
                .withItemWithSize(DesignPathCard.uploadCard(parentProject), 2, 1)
                .withPadding(true)
                .withSpacing(true)
                .withGap(new Length("50px"))
                .withAutoFlow(GridLayoutComponent.AutoFlow.ROW_DENSE) // allow the item order to be changed to let elements fill empty spaces.
                .withOverflow(GridLayoutComponent.Overflow.AUTO);
        layout.setSizeFull();

        for (int i = 0; i < designPathCards.size(); i++) {
            layout.withItemWithSize(designPathCards.get(i), 2, 1);

        }
        designGrid.add(layout);
        designGrid.setSizeFull();

        layout.setSizeFull();
        layout.setClassName("project-card-grid");
        projectsAndSearchLayout.removeAll();
        projectsAndSearchLayout.setFlexGrow(1, designGrid);
        projectsAndSearchLayout.add(designGrid, filterProjectsTool);

        //add(breadcrumbLayout);


    }


}



