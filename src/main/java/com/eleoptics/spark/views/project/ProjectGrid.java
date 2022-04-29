package com.eleoptics.spark.views.project;

import com.eleoptics.spark.api.Asset;
import com.eleoptics.spark.api.Assets;
import com.eleoptics.spark.api.OpticsApi;
import com.eleoptics.spark.eventbus.EventBus_depracated;
import com.eleoptics.spark.events.ProjectCardClickedEvent;
import com.eleoptics.spark.views.main.MainView;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.renderer.LocalDateTimeRenderer;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Route(value = "projectGrid", layout = MainView.class)
@PageTitle("PROJECTS")
@CssImport(value = "./styles/my-grid-style.css", themeFor = "vaadin-grid")
@JsModule("./styles/shared-styles.js")
@Slf4j
public class ProjectGrid extends VerticalLayout {


    @Autowired
    OpticsApi opticsApi;

    ProjectGridFilters projectGridFilters = new ProjectGridFilters();
    Grid<Asset> grid = new Grid<>(Asset.class);
    ProjectDetailsView projectDetailsView = new ProjectDetailsView();
    Asset selectedProject = new Asset();
    Asset fetchedProject = new Asset();
    List<Asset> projectList = new ArrayList<>();
    Icon closeIcon = new Icon(VaadinIcon.CLOSE);
    Div projectID = new Div();



    public void setProjectGridView(List<Asset> projectList) {
        grid.removeAllColumns();
        grid.setItems(projectList);

    }

    final DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm");


    public ProjectGrid() {


        setHeightFull();
        setWidthFull();


        grid.setColumns("assetName", "tags", "version");

        grid.addColumn(new LocalDateTimeRenderer<>(
                Asset::getCreatedAt,
                "MM/dd/yyyy")
        ).setHeader("Created").setSortable(true);

        grid.addColumn(new LocalDateTimeRenderer<>(
                Asset::getLastModified,
                "HH:mm MM/dd/yyyy")
        ).setHeader("Last Modified").setSortable(true);

        projectGridFilters.setVisible(false);

        add(projectGridFilters);
        add(grid);

        grid.addItemClickListener(
                event -> {
                    selectedProject = event.getItem();

                    //EventBus_depracated.broadcastEvent(new ProjectCardClickedEvent(selectedProject));


                });

        add(projectID);

    }


}
