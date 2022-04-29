package com.eleoptics.spark.views.project;

import com.eleoptics.spark.api.Asset;
import com.eleoptics.spark.api.OpticsApi;
import com.eleoptics.spark.eventbus.EventBusFactory;
import com.eleoptics.spark.eventbus.EventBus_depracated;
import com.eleoptics.spark.events.DesignPathCardClickedEvent;
import com.eleoptics.spark.events.DesignPathVersionClickedEvent;
import com.eleoptics.spark.events.NavigationKeyStrings;
import com.eleoptics.spark.events.NewDesignPathClickedEvent;
import com.eleoptics.spark.views.QueryParameterNames;
import com.eleoptics.spark.views.RouteNames;
import com.github.appreciated.card.RippleClickableCard;
import com.github.appreciated.card.content.IconItem;
import com.google.common.eventbus.EventBus;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.ComponentUtil;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.QueryParameters;
import com.vaadin.flow.router.Router;
import com.vaadin.flow.router.RouterLink;
import com.vaadin.flow.server.StreamResource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

@CssImport(value = "./styles/style.css", themeFor = "vaadin-grid")
@Slf4j
public class DesignPathCard extends RippleClickableCard {

    public Asset cardProject;

    public DesignPathCard(ComponentEventListener listener, Asset cardProject, Component... component) {
        super(listener, component);
        this.cardProject = cardProject;
    }


    public static DesignPathCard createCard(Asset project, String svgString, Consumer<Asset> versionHandler) {
        String projectName = project.getAssetName();
        String projectMetadataString = (">Version: " + project.getVersion().toString()
                + " | " + "Updated: " + project.getLastModifiedFormatted());



        HorizontalLayout card = new HorizontalLayout();
        VerticalLayout text = new VerticalLayout();
        VerticalLayout systemImage = new VerticalLayout();

        Span projectTitle = new Span(project.getAssetName());
        projectTitle.setClassName("design-path-card-title");
        Span projectMetadata = new Span(projectMetadataString);
        projectMetadata.setClassName("design-path-card-metadata");
        Span projectDescription = new Span("Description: " + project.description);
        projectDescription.setClassName("design-path-card-description");
        Span projectTags = new Span("Tags: " + project.getTags());
        projectDescription.setClassName("design-path-card-tags");
        text.setClassName("design-path-card-text-area");

        // Add in a button for the version
        Button versionButton = new Button(projectMetadata);
        versionButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        versionButton.getStyle().set("text-decoration","underline");
        versionButton.setClassName("design-path-card-version-button");

        versionButton.addClickListener(event -> versionHandler.accept(project));
        text.add(projectTitle, versionButton, projectDescription, projectTags);
        text.setHeightFull();
        text.setWidthFull();

        Image svgImage = new Image();

        if(svgString != null) {
            // Add image from svg

            byte[] imageBytes = svgString.getBytes(StandardCharsets.UTF_8);

            StreamResource resource = new StreamResource("dummyImageName.svg", () -> new ByteArrayInputStream(imageBytes));

            svgImage.setSrc(resource);

            svgImage.getStyle().set("padding-left", "16px");
            svgImage.getStyle().set("align-self", "left");
        }else{
            text.getStyle().set("padding-top","80px");
        }

        card.add(new VerticalLayout(svgImage, text));
        card.setWidthFull();

        DesignPathCard cardComponent = new DesignPathCard(

                onClick -> {
                    log.info("project card design path clicked");
                    // Establish the query parameters map
                    Map<String, String> queryMap = new HashMap<>();
                    queryMap.put(QueryParameterNames.projectId, project.key);
                    queryMap.put(QueryParameterNames.version, project.version.toString());
                    QueryParameters queryParameters = QueryParameters.simple(queryMap);

                    // Set a router link t the detail page
                    RouterLink detailsPageLink = new RouterLink();
                    detailsPageLink.setRoute(ProjectDetailsView.class);

                    // Navigate to the details page with the query parameters
                    UI.getCurrent().navigate(detailsPageLink.getHref(), queryParameters);
                },
                project,
                card);
        cardComponent.setClassName("design-path-card");
        cardComponent.withBorderRadius("16px");

        return cardComponent;
    }

    public static RippleClickableCard uploadCard(Asset parentProject) {
        Icon uploadIcon = new Icon(VaadinIcon.PLUS_CIRCLE_O);
        uploadIcon.setColor("#404041");
        uploadIcon.setSize("100px");
        IconItem uploadCardIcon = new IconItem(uploadIcon, "");
        uploadCardIcon.setClassName("upload-icon");

        Span cardTitle = new Span("New Design Path");
        cardTitle.setClassName("upload-design-path-title");

        VerticalLayout card = new VerticalLayout();
        card.add(uploadIcon, cardTitle);
        card.setJustifyContentMode(JustifyContentMode.CENTER);
        card.setAlignItems(Alignment.CENTER);
        card.setHeightFull();

        RippleClickableCard cardComponent = new RippleClickableCard(
                onClick -> {
                    log.info("project card design path clicked");
                    UI.getCurrent().navigate(
                            NewDesignPath.class,
                            parentProject.key
                    );
                },
                card);
        card.setClassName("design-path-upload-card-text-area");

        cardComponent.withBorderRadius("16px");
        cardComponent.setClassName("design-path-upload-card");


        return cardComponent;
    }

 
}