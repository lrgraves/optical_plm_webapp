package com.eleoptics.spark.views.project;

import com.eleoptics.spark.api.Asset;
import com.eleoptics.spark.api.OpticsApi;
import com.eleoptics.spark.views.main.MainView;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.PreserveOnRefresh;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteAlias;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Route(value = "projects/", layout = MainView.class)
@PreserveOnRefresh
@RouteAlias(value = "", layout = MainView.class)
@PageTitle("PROJECTS")
@Slf4j
public class ProjectsView extends VerticalLayout {

    @Autowired
    OpticsApi opticsApi;

    ProjectCardGrid projectCardGrid = new ProjectCardGrid();
    List<Asset> projectList = new ArrayList<>();
    List<Asset> fullAssetList = new ArrayList<>();

    FilterProjects filterProjectsTool = new FilterProjects();
    HorizontalLayout projectsAndSearchLayout = new HorizontalLayout();


    @PostConstruct
    private void initializeAllAssets() {

        // if the assets have not been fetched, fetch them via api call and place in cache
        fullAssetList = opticsApi.getAllAssets().assets;

        // get the top level projects only
        projectList = Asset.getTopLevelProjects(fullAssetList);

        filterProjectsTool.defineProjectLists(projectList, fullAssetList);
        filterProjectsTool.defineProjectLevel(true);
        projectCardGrid.setCardGridView(projectList, fullAssetList, opticsApi);

        projectsAndSearchLayout.setWidthFull();
    }

    public ProjectsView() throws IOException {
        //  EventBusFactory.getEventBus().post(new AllProjectsPageEvent());

        projectCardGrid.deleteProjectHandler(project -> {
            opticsApi.deleteAsset(project.key, project.version);
            UI.getCurrent().navigate(ProjectsView.class);
        });

        filterProjectsTool.filterHandler(filteredList -> {
            projectCardGrid.setCardGridView(filteredList, fullAssetList, opticsApi);
            log.info("The search button was clicked");
        });


        filterProjectsTool.getStyle().set("margin-right", "20px");
        filterProjectsTool.getStyle().set("padding-right", "16px");

        projectsAndSearchLayout.getStyle().set("margin-top", "20px");
        filterProjectsTool.getStyle().set("width", "auto");
        projectsAndSearchLayout.add(projectCardGrid, filterProjectsTool);
        projectsAndSearchLayout.setFlexGrow(1, projectCardGrid);
        add(new VerticalLayout());
        add(projectsAndSearchLayout);
    }

}
