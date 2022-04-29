package com.eleoptics.spark.views.project;

import com.eleoptics.spark.api.Asset;
import com.eleoptics.spark.api.OpticsApi;
import com.eleoptics.spark.eventbus.EventBusFactory;
import com.eleoptics.spark.eventbus.EventBus_depracated;
import com.eleoptics.spark.events.DeleteProjectClickedEvent;
import com.eleoptics.spark.events.NavigationKeyStrings;
import com.eleoptics.spark.events.ProjectCardClickedEvent;
import com.github.appreciated.card.RippleClickableCard;
import com.github.appreciated.card.content.IconItem;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.contextmenu.ContextMenu;
import com.vaadin.flow.component.contextmenu.MenuItem;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.details.Details;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.server.StreamResource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;


@CssImport(value = "./styles/style.css", themeFor = "vaadin-grid")
@Slf4j
public class ProjectCardView extends RippleClickableCard {


    public ProjectCardView(ComponentEventListener listener, Component... components) {
        super(listener, components);

    }


    public static ProjectCardView createCard(Asset project, String svgString, Consumer<Asset> deleteListener) {

        VerticalLayout card = new VerticalLayout();
        VerticalLayout text = new VerticalLayout();

        String projectName = project.getAssetName();

        Span projectTitle = new Span(projectName);
        projectTitle.setClassName("project-card-title");
        Span projectLastUpdate = new Span("Last Updated: " + project.getLastModifiedFormatted());
        projectLastUpdate.setClassName("project-card-last-update");

        List<String> projectTagsList = project.getTags();
        String projectTagString = new String("Tags: ");
        if (projectTagsList.size() > 3) {
            projectTagString = projectTagString + projectTagsList.subList(0, 2) + "...";
        } else {
            projectTagString = projectTagString + projectTagsList;
        }
        Span projectTags = new Span(projectTagString);
        projectTags.setClassName("project-card-tags");
        Icon optionsIcons = new Icon((VaadinIcon.PLUS_CIRCLE_O));
        optionsIcons.setSize("20px");

        ContextMenu contextMenu = new ContextMenu();
        contextMenu.setOpenOnClick(true);

        contextMenu.setTarget(optionsIcons);

        //Create tools for the project card
        Icon deleteProjectIcon = new Icon(VaadinIcon.TRASH);
        deleteProjectIcon.setSize("20px");
        Button deleteButton = new Button(deleteProjectIcon);

        Dialog dialog = new Dialog();

        dialog.setCloseOnEsc(true);
        dialog.setCloseOnOutsideClick(true);

        Span deleteMessage = new Span("Are you sure you want to delete this project?" +
                " This cannot be undone and will delete the project and all designs for the project.");

        Button confirmButton = new Button("Confirm", event -> {
            deleteMessage.setText("Confirmed");
            dialog.close();

            deleteListener.accept(project);
        });
        Button cancelButton = new Button("Cancel", event -> {
            deleteMessage.setText("Cancelled...");
            dialog.close();
        });

        confirmButton.addThemeVariants(ButtonVariant.MATERIAL_CONTAINED);
        cancelButton.addThemeVariants(ButtonVariant.MATERIAL_CONTAINED);
        dialog.add(deleteMessage, new

                HorizontalLayout(confirmButton, cancelButton));


        Label message = new Label("");

        contextMenu.addItem("Edit",
                e ->

                {
                    log.info("edit clicked");
                    String key = NavigationKeyStrings.selectedProject;
                    UI.getCurrent().getSession().setAttribute(key, project);
                    UI.getCurrent().navigate(UpdateProject.class);
                });

        contextMenu.addItem("Delete",
                e -> dialog.open()
        );


        text.setClassName("project-card-text-area");

        text.add(projectTitle, projectLastUpdate, optionsIcons, message);
        text.setWidthFull();
        text.setJustifyContentMode(JustifyContentMode.START);
        text.setHorizontalComponentAlignment(Alignment.CENTER, optionsIcons);

        // Image of system

        Image svgImage = new Image();
        svgImage.setClassName("svgImage");

        if (svgString != null) {
            // Add image from svg

            byte[] imageBytes = svgString.getBytes(StandardCharsets.UTF_8);

            StreamResource resource = new StreamResource("dummyImageName.svg", () -> new ByteArrayInputStream(imageBytes));

            svgImage.setSrc(resource);

            svgImage.getStyle().set("margin-top", "12px");
            svgImage.getStyle().set("align-self", "center");

        } else {
            text.getStyle().set("padding-top", "64px");
        }

        text.getElement().getStyle().set("margin-top", "auto");
        card.add(svgImage, text);
        card.setWidthFull();
        card.setAlignItems(Alignment.END);
        card.setHeightFull();

        ProjectCardView cardComponent = new ProjectCardView(
                onClick -> {

                    UI.getCurrent().navigate(
                            DesignPathGrid.class,
                            project.key
                    );
                },
                card);

        cardComponent.setClassName("project-card");
        cardComponent.withBorderRadius("16px");


        return cardComponent;
    }


    public static RippleClickableCard uploadCard() {

        Icon uploadIcon = new Icon(VaadinIcon.PLUS_CIRCLE_O);
        uploadIcon.setColor("#404041");
        uploadIcon.setSize("54px");
        IconItem uploadCardIcon = new IconItem(uploadIcon, "");
        uploadCardIcon.setClassName("upload-icon");

        Span cardTitle = new Span("New Project");
        cardTitle.setClassName("new-project-card-title");

        VerticalLayout card = new VerticalLayout();
        card.add(uploadIcon, cardTitle);
        card.setJustifyContentMode(JustifyContentMode.START);
        card.setAlignItems(Alignment.CENTER);
        card.setHeightFull();
        card.setClassName("upload-oroject-card-text-area");

        RippleClickableCard cardComponent = new RippleClickableCard(
                onClick -> {
                    UI.getCurrent().navigate("newProject");
                },
                card);
        card.setClassName("upload-title");

        cardComponent.withBorderRadius("16px");
        cardComponent.setClassName("upload-card");

        return cardComponent;
    }


}
