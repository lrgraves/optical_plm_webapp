package com.eleoptics.spark.views.project;

import com.eleoptics.spark.api.*;
import com.eleoptics.spark.events.NavigationKeyStrings;
import com.eleoptics.spark.views.main.MainView;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.vaadin.flow.component.*;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.BinderValidationStatus;
import com.vaadin.flow.data.binder.BindingValidationStatus;
import com.vaadin.flow.data.validator.StringLengthValidator;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.internal.MessageDigestUtil;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.StreamResource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.compress.utils.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;

@Route(value = "updateProject", layout = MainView.class)
@PageTitle("UPDATE PROJECT")
@CssImport(value = "./styles/my-grid-style.css", themeFor = "vaadin-grid")
@JsModule("./styles/shared-styles.js")
@Slf4j
public class UpdateProject extends VerticalLayout implements BeforeEnterObserver {

    @Autowired
    OpticsApi opticsApi;

    VerticalLayout page = new VerticalLayout();
    FormLayout layoutWithBinder = new FormLayout();
    Binder<AssetObjectForUpdate> binder = new Binder<>();
    AssetObjectForUpdate assetBeingUpdated = new AssetObjectForUpdate();
    TextField assetName = new TextField();
    TextField assetTags = new TextField();
    TextField description = new TextField();


    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        String key = NavigationKeyStrings.selectedProject;
        Asset project = (Asset) UI.getCurrent().getSession().getAttribute(key);

        setUpdatePage(project);
    }

    public UpdateProject() {

        // Setting the desired responsive steps for the columns in the layout
        layoutWithBinder.setResponsiveSteps(
                new FormLayout.ResponsiveStep("25em", 1),
                new FormLayout.ResponsiveStep("32em", 2),
                new FormLayout.ResponsiveStep("40em", 3));


// The object that will be edited

// Create the fields
        assetName.setValueChangeMode(ValueChangeMode.EAGER);
        assetName.setTitle("Project Name");
        assetTags.setTitle("Project Tags (Comma Separated)");
        assetTags.setValueChangeMode(ValueChangeMode.EAGER);
        description.setValueChangeMode(ValueChangeMode.EAGER);
        description.setTitle("Project Description");

        Label infoLabel = new Label();
        Button save = new Button("Save");
        Button reset = new Button("Reset");
        Button cancel = new Button("Cancel");

        layoutWithBinder.add(assetName, assetTags);
        layoutWithBinder.add(description, 2);
        // layoutWithBinder.add(upload, 2);

// Button bar
        HorizontalLayout actions = new HorizontalLayout();
        actions.add(save, reset, cancel);
        save.getStyle().set("marginRight", "10px");


// First name and last name are required fields
        assetName.setRequiredIndicatorVisible(true);
        assetTags.setRequiredIndicatorVisible(true);

        binder.forField(assetName)
                .withValidator(new StringLengthValidator(
                        "Please add a project name at least 5 characters in length.", 5, null))
                .bind(AssetObjectForUpdate::getAssetName, AssetObjectForUpdate::setAssetName);

        binder.forField(description)
                .withValidator(new StringLengthValidator(
                        "Please add a few more words to your project description.", 10, null))
                .bind(AssetObjectForUpdate::getDescription, AssetObjectForUpdate::setDescription);


// Click listeners for the buttons
        save.addClickListener(event -> {
            if (binder.writeBeanIfValid(assetBeingUpdated)) {
                infoLabel.setText("Saved bean values: " + assetBeingUpdated);
                List<String> taglist = Arrays.stream(assetTags.getValue().split(","))
                        .map(String::strip).collect(Collectors.toList());
                assetBeingUpdated.setTags(taglist);
                // For a new project at the top level, it is always a project type
                assetBeingUpdated.setAssetType("Project");
                assetBeingUpdated.setDescription(description.getValue().toString());


                try {

                    opticsApi.patchProject(assetBeingUpdated);

                    UI.getCurrent().navigate(ProjectsView.class);

                } catch (JsonProcessingException e) {
                    e.printStackTrace();
                }

            } else {
                BinderValidationStatus<AssetObjectForUpdate> validate = binder.validate();
                String errorText = validate.getFieldValidationStatuses()
                        .stream().filter(BindingValidationStatus::isError)
                        .map(BindingValidationStatus::getMessage)
                        .map(Optional::get).distinct()
                        .collect(Collectors.joining(", "));
                infoLabel.setText("There are errors: " + errorText);
            }
        });
        reset.addClickListener(event -> {
            // clear fields by setting null
            binder.readBean(null);
            infoLabel.setText("");
        });
        cancel.addClickListener(event ->{
            UI.getCurrent().navigate(ProjectsView.class);
        });
        page.setClassName("new-project-view");
        page.add(layoutWithBinder, actions, infoLabel);

        add(page);

    }

    private void setUpdatePage(Asset project) {

        assetName.setValue(project.assetName);

        String tags= project.tags.toString();
        log.info("tag pre edit is" + tags);

       tags = tags.substring(1, tags.length() - 1);

        log.info("tags are:" + tags);
        assetTags.setValue(tags);

        description.setValue(project.description);

        assetBeingUpdated.key = project.key;
        assetBeingUpdated.parentKey = project.parentKey;
        assetBeingUpdated.assetType = "Project";
    }

}
