package com.eleoptics.spark.views.project;

import com.eleoptics.spark.api.*;
import com.eleoptics.spark.events.NavigationKeyStrings;
import com.eleoptics.spark.views.main.MainView;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentUtil;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.BinderValidationStatus;
import com.vaadin.flow.data.binder.BindingValidationStatus;
import com.vaadin.flow.data.validator.StringLengthValidator;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.*;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;
import java.util.stream.Collectors;

@Route(value = "newDesignPath", layout = MainView.class)
@PageTitle("New Design Path")
@CssImport(value = "./styles/my-grid-style.css", themeFor = "vaadin-grid")
@JsModule("./styles/shared-styles.js")
@Slf4j
public class NewDesignPath extends VerticalLayout
        implements HasUrlParameter<String> {

    @Autowired
    OpticsApi opticsApi;

    VerticalLayout page = new VerticalLayout();

    Asset parentProject = new Asset();
    String parentProjectID = new String();

    @SneakyThrows
    @Override
    public void setParameter(BeforeEvent event, String parameter) {
        // fetch parent project ID
        parentProjectID = parameter;

        // use api to fetch the parent asset and the list of assets
        parentProject = opticsApi.getProjectNoData(parentProjectID, null);

    }

    public NewDesignPath() {
        FormLayout layoutWithBinder = new FormLayout();
        // Setting the desired responsive steps for the columns in the layout
        layoutWithBinder.setResponsiveSteps(
                new FormLayout.ResponsiveStep("25em", 1),
                new FormLayout.ResponsiveStep("32em", 2),
                new FormLayout.ResponsiveStep("40em", 3));

        Binder<DesignPathForUpload> binder = new Binder<>();

// The object that will be edited
        DesignPathForUpload asssetBeingUploaded = new DesignPathForUpload();

// Create the fields
        TextField assetName = new TextField();
        assetName.setValueChangeMode(ValueChangeMode.EAGER);
        assetName.setPlaceholder("Design Path Name");
        TextField assetTags = new TextField();
        assetTags.setPlaceholder("Design Path Tags (Comma Separated)");
        assetTags.setValueChangeMode(ValueChangeMode.EAGER);
        TextField description = new TextField();
        description.setValueChangeMode(ValueChangeMode.EAGER);
        description.setPlaceholder("Enter a short description of the design path.");

        Label infoLabel = new Label();
        Button save = new Button("Create");
        Button reset = new Button("Reset");

        layoutWithBinder.add(assetName, assetTags);
        layoutWithBinder.add(description, 2);

// Button bar
        HorizontalLayout actions = new HorizontalLayout();
        actions.add(save, reset);
        save.getStyle().set("marginRight", "10px");


// First name and last name are required fields
        assetName.setRequiredIndicatorVisible(true);
        assetTags.setRequiredIndicatorVisible(true);

        binder.forField(assetName)
                .withValidator(new StringLengthValidator(
                        "Please add a design path name at least 5 characters in length.", 5, null))
                .bind(DesignPathForUpload::getAssetName, DesignPathForUpload::setAssetName);

        binder.forField(description)
                .withValidator(new StringLengthValidator(
                        "Please add a few more words to your design path description.", 10, null))
                .bind(DesignPathForUpload::getDescription, DesignPathForUpload::setDescription);


        // Click listeners for the buttons
        save.addClickListener(event -> {
            if (binder.writeBeanIfValid(asssetBeingUploaded)) {
                infoLabel.setText("Saved bean values: " + asssetBeingUploaded);
                List<String> taglist = Arrays.stream(assetTags.getValue().split(","))
                        .map(String::strip).collect(Collectors.toList());
                asssetBeingUploaded.setTags(taglist);
                // For a new project at the top level, it is always a project type
                asssetBeingUploaded.setAssetType("Project");
                asssetBeingUploaded.setDescription(description.getValue().toString());

               try {
                    Asset uploadedAsset = sendNewDesign(asssetBeingUploaded);

                   // Navigate back to the parent project design path page
                   UI.getCurrent().navigate(
                           DesignPathGrid.class,
                           parentProject.key);
                } catch (JsonProcessingException e) {
                    e.printStackTrace();
                }


                log.info(asssetBeingUploaded.toString());
            } else {
                BinderValidationStatus<DesignPathForUpload> validate = binder.validate();
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
        page.setClassName("new-design-path-view");
        page.add(layoutWithBinder, actions, infoLabel);

        add(page);

    }

    private Asset sendNewDesign(DesignPathForUpload uploadData) throws JsonProcessingException {

        HashMap<String, ProjectRequirement> requirements = new HashMap<>();
        ProjectRequirement focalLength = new ProjectRequirement(RequirementType.FocalLength, "100");
        requirements.put("Focal Length", focalLength);
        List<String> assetIds = new ArrayList<>();
        List<Integer> versions = new ArrayList<>();


        ProjectDataForProjectUpload projectData =
                new ProjectDataForProjectUpload(requirements, assetIds, versions);

        uploadData.setParentID(parentProject.key);

        Asset uploadedAsset = opticsApi.sendNewDesignPath(uploadData, projectData);

        return uploadedAsset;
    }


    }
