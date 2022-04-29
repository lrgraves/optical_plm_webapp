package com.eleoptics.spark.views.project;

import com.eleoptics.spark.api.*;
import com.eleoptics.spark.views.main.MainView;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.github.appreciated.card.RippleClickableCard;
import com.github.appreciated.css.grid.GridLayoutComponent;
import com.github.appreciated.css.grid.sizes.Length;
import com.github.appreciated.css.grid.sizes.MinMax;
import com.github.appreciated.css.grid.sizes.Repeat;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;
import javax.swing.text.html.Option;
import java.util.*;
import java.util.function.Consumer;

@Route(value = "projectCardGrid", layout = MainView.class)
@PageTitle("PROJECTS")
@CssImport(value = "./styles/style.css", include = "lumo-badge")
@JsModule("./styles/shared-styles.js")
@Slf4j
public class ProjectCardGrid extends HorizontalLayout {


    List<RippleClickableCard> projectCards = new ArrayList<>();

    // SVG
    RenderSVG renderSVG = new RenderSVG();
    Integer samplePoints = 10;
    Integer svgCardHeight = 68;
    Integer svgCardWidth = 124;
    Integer svgPadding = 8;
    Integer numberRays = null;


    private Optional<Consumer<Asset>> deleteHandler = Optional.empty();

    public void deleteProjectHandler(Consumer<Asset> deleteListener) {
        deleteHandler = Optional.of(deleteListener);
    }


    public void setCardGridView(List<Asset> projectList, List<Asset> allAssets, OpticsApi opticsAPI) {
        removeAll();
        projectCards = new ArrayList();
        // Make list of project cards
        projectList.forEach(project -> {
            String svgString = projectSVG(allAssets, project, opticsAPI);
            ProjectCardView projectCard = ProjectCardView.createCard(project, svgString, deleteHandler.get());

            projectCards.add(projectCard);
        });

        setFlexibleGridLayout(projectCards);
    }

    public ProjectCardGrid() {

        setId("project-view");
        addClassName("project-view");


    }

    @SneakyThrows
    private String projectSVG(List<Asset> fullAssetList, Asset projectFile, OpticsApi opticsApi) {
        // Search through entire asset list for the assets with matching the system id

        String projectKey = projectFile.key;
        String designPathKey = new String();
        //log.info("we are searching for svg");

        // Find the matching design path that was last updated
        for (Asset designPath : fullAssetList) {
            if (designPath.parentKey.equals(projectKey)) {
                designPathKey = designPath.getKey();
                //log.info("found matching design path");
            }
        }

        // Find the asset with a design file inside said design path
        String designString = null;
        for (Asset assetFile : fullAssetList) {
            // Check if the file is for the given parent design;
            if (assetFile.parentKey.equals(designPathKey) && assetFile.metadata.opticalMetadata.isPresent() &&
                    !assetFile.metadata.opticalMetadata.get().prescription.zLength.equals(null)) {
                //log.info("found matching asset for given design path");
                // This is a matching zemax file for the parent asset, get the SVG line
                ArrayList<Integer> samplingPoints = new ArrayList<>();
                OpticalMetadata metadata = assetFile.getMetadata().opticalMetadata.get();
                List<Integer> numSurfaces = metadata.prescription.numberOfInterfaceTypes;

                var sum = numSurfaces.stream()
                        .mapToInt(Integer::intValue).sum();
                samplingPoints.addAll(Collections.nCopies(sum, samplePoints));

                Optional<VisualizationData> dataPoints = getDataPoints(assetFile, samplingPoints, opticsApi);

                //log.info("the data poitns for {} are {}", assetFile, dataPoints.toString());


                if (dataPoints.isPresent()) {
                    if (dataPoints.get().system != null) {
                        if (dataPoints.get().system.elements != null) {
                            designString = renderSVG.createDoc(assetFile, svgCardHeight, svgCardWidth, svgPadding, dataPoints.get(), false);
                        }
                    }
                }


            }
            // Check if the system has needed metadata

        }
        return designString;

    }

    public Optional<VisualizationData> getDataPoints(Asset assetFile, ArrayList<Integer> samplingPoints, OpticsApi opticsApi) {
        VisualizationData dataPoints = null;

        try {
            dataPoints = opticsApi.
                    getVisualizationPoints(
                            assetFile.key,
                            assetFile.version,
                            samplingPoints,
                            numberRays);
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

    public void setFlexibleGridLayout(List<RippleClickableCard> projectCards) {

        com.github.appreciated.layout.FlexibleGridLayout layout = new com.github.appreciated.layout.FlexibleGridLayout()
                .withColumns(Repeat.RepeatMode.AUTO_FILL, new MinMax(new Length("182px"), new Length("184px")))
                .withAutoRows(new Length("210px"))
                .withItemWithSize(ProjectCardView.uploadCard(), 1, 1)
                .withPadding(true)
                .withSpacing(true)
                .withGap(new Length("50px"))
                .withAutoFlow(GridLayoutComponent.AutoFlow.ROW_DENSE) // allow the item order to be changed to let elements fill empty spaces.
                .withOverflow(GridLayoutComponent.Overflow.AUTO);
        layout.setSizeFull();
        setSizeFull();

        for (int i = 0; i < projectCards.size(); i++) {
            layout.withItemWithSize(projectCards.get(i), 1, 1);

        }
        add(layout);

        layout.setSizeFull();
        layout.setClassName("project-card-grid");

    }
}



