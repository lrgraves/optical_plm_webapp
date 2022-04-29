package com.eleoptics.spark.views.project;

import com.eleoptics.spark.api.*;
import com.eleoptics.spark.events.NavigationKeyStrings;
import com.eleoptics.spark.views.main.MainView;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.vaadin.flow.component.*;
import com.vaadin.flow.component.accordion.Accordion;
import com.vaadin.flow.component.accordion.AccordionPanel;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.details.DetailsVariant;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.MemoryBuffer;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.BinderValidationStatus;
import com.vaadin.flow.data.binder.BindingValidationStatus;
import com.vaadin.flow.data.validator.StringLengthValidator;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.internal.MessageDigestUtil;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteAlias;
import com.vaadin.flow.router.RouteConfiguration;
import com.vaadin.flow.server.StreamResource;
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
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Route(value = "newProject", layout = MainView.class)
@PageTitle("NEW PROJECT")
@CssImport(value = "./styles/my-grid-style.css", themeFor = "vaadin-grid")
@JsModule("./styles/shared-styles.js")
@Slf4j
public class NewProject extends VerticalLayout {

    @Autowired
    OpticsApi opticsApi;

    VerticalLayout projectLayout = new VerticalLayout();
    VerticalLayout designPathLayout = new VerticalLayout();
    VerticalLayout designFileLayout = new VerticalLayout();

    Binder<ProjectForUpload> projectBinder = new Binder<>();
    Binder<DesignPathForUpload> designPathBinder = new Binder<>();
    Binder<AssetObjectForUpload> designFileBinder = new Binder<>();
    Label infoLabel = new Label();
    Boolean uploadCompleted = false;
    Boolean projectUploaded = false;
    Boolean designPathUploaded = false;
    Boolean designFileUploaded = false;


    // Project for upload
    ProjectForUpload projectBeingUploaded = new ProjectForUpload();
    TextField projectName = new TextField();
    TextField projectTags = new TextField();
    TextField projectDescription = new TextField();
    String parentProjectKey = new String();

    // Design Path for upload
    DesignPathForUpload designPathBeingUploaded = new DesignPathForUpload();
    TextField designPathName = new TextField();
    TextField designPathTags = new TextField();
    TextField designPathDescription = new TextField();
    String designPathKey = new String();

    // Design File for upload
    AssetObjectForUpload designFileBeingUploaded = new AssetObjectForUpload();
    TextField designFileName = new TextField();
    TextField designFileTags = new TextField();
    TextField designFileDescription = new TextField();
    AssetDataForProjectUpload assetData = new AssetDataForProjectUpload();



    public NewProject() {
        Accordion accordion = new Accordion();

        // Create the project area layout
        createProjectLayout();
        createDesignPathLayout();
        createDesignFileLayout();

        AccordionPanel projectPanel = accordion.add("Project Information", projectLayout);
        AccordionPanel designPathPanel = accordion.add("Add Design Path?", designPathLayout);
        //designPathPanel.setEnabled(false);
        AccordionPanel designFilePanel = accordion.add("Add Design Path File?", designFileLayout);
        //designFilePanel.setEnabled(false);
        projectPanel.addThemeVariants(DetailsVariant.SMALL);
        designPathPanel.addThemeVariants(DetailsVariant.SMALL);
        designPathPanel.setOpened(false);
        designFilePanel.addThemeVariants(DetailsVariant.SMALL);
        designFilePanel.setOpened(false);
        accordion.setWidthFull();



        // Create Action layout
        Button save = new Button("Create");
        Button reset = new Button("Reset");
        Button cancel = new Button("Cancel");
        save.addThemeVariants(ButtonVariant.MATERIAL_CONTAINED);
        reset.addThemeVariants(ButtonVariant.MATERIAL_CONTAINED);
        cancel.addThemeVariants(ButtonVariant.MATERIAL_CONTAINED);

        // Button bar
        HorizontalLayout actions = new HorizontalLayout();
        actions.add(save, reset, cancel);
        save.getStyle().set("padding-left", "16px");
        // Click listeners for the buttons
        save.addClickListener(event -> {
            // ALL BINDER CHECKS DONE HERE AT TOP LEVEL!!!!

            // PROJECT UPLOAD
            if (projectBinder.writeBeanIfValid(projectBeingUploaded) && !projectUploaded) {
                // Send the project data
                projectUploaded = sendProject();

                // If the project is uploaded successfully and there is no other fields filled, we are done.
                if (projectUploaded &&
                        (designPathName.isEmpty() && designPathDescription.isEmpty() && designPathTags.isEmpty() ) &&
                        (designFileName.isEmpty() && designFileDescription.isEmpty() && designFileTags.isEmpty() )) {
                    uploadCompleted = true;
                }
            } else if (!projectBinder.isValid() && !projectUploaded) {
                BinderValidationStatus<ProjectForUpload> validate = projectBinder.validate();
                String errorText = validate.getFieldValidationStatuses()
                        .stream().filter(BindingValidationStatus::isError)
                        .map(BindingValidationStatus::getMessage)
                        .map(Optional::get).distinct()
                        .collect(Collectors.joining(", "));
                infoLabel.setText("There are errors: " + errorText);
            }

            // DESIGN PATH UPLOAD

            // if the upload succeeded and there is data in the design path, complete
            // nest design path action next
            if(!parentProjectKey.isEmpty() && projectUploaded && designPathBinder.writeBeanIfValid(designPathBeingUploaded)
            && !designPathUploaded){
                // Send the design path only now, and then mark as completed.
                designPathUploaded = sendDesignPath();
                if(projectUploaded && designPathUploaded &&
                        (designFileName.isEmpty() && designFileDescription.isEmpty() && designFileTags.isEmpty() )){
                    uploadCompleted = true;
                }

            } else if (!parentProjectKey.isEmpty() && projectUploaded && !designPathBinder.isValid() &&
                    (!designPathName.isEmpty() || !designPathDescription.isEmpty() || !designPathTags.isEmpty() && !designPathUploaded)
            ){
                BinderValidationStatus<DesignPathForUpload> validate = designPathBinder.validate();
                String errorText = validate.getFieldValidationStatuses()
                        .stream().filter(BindingValidationStatus::isError)
                        .map(BindingValidationStatus::getMessage)
                        .map(Optional::get).distinct()
                        .collect(Collectors.joining(", "));
                infoLabel.setText("There are errors: " + errorText);
            }

            // DESIGN FILE UPLOAD

            if(!parentProjectKey.isEmpty() && !designPathKey.isEmpty() &&
                    projectUploaded && designPathUploaded && designFileBinder.writeBeanIfValid(designFileBeingUploaded)
                    && !designFileUploaded){
                // Send the design file only now, and then mark as completed.
                designFileUploaded = sendDesignFile();
                if(projectUploaded && designPathUploaded && designFileUploaded){
                    uploadCompleted = true;
                }

            } else if (!parentProjectKey.isEmpty() && !designPathKey.isEmpty() && projectUploaded && designPathUploaded
                    && !designFileBinder.isValid() &&
                    (!designFileName.isEmpty() || !designFileDescription.isEmpty() || !designFileTags.isEmpty() && !designFileUploaded)
            ){
                BinderValidationStatus<AssetObjectForUpload> validate = designFileBinder.validate();
                String errorText = validate.getFieldValidationStatuses()
                        .stream().filter(BindingValidationStatus::isError)
                        .map(BindingValidationStatus::getMessage)
                        .map(Optional::get).distinct()
                        .collect(Collectors.joining(", "));
                infoLabel.setText("There are errors: " + errorText);
            }

            if(uploadCompleted){
                // If we are completed in uploading everything, navigate on home
                UI.getCurrent().navigate(ProjectsView.class);
            }


        });
        reset.addClickListener(event ->

        {
            // clear fields by setting null
            projectBinder.readBean(null);
            infoLabel.setText("");
        });
        cancel.addClickListener(event ->

        {
            UI.getCurrent().navigate(ProjectsView.class);
        });


        actions.getStyle().set("padding-top","10px");

        // Set overall layout



        add(accordion, actions, infoLabel);

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

    private VerticalLayout createProjectLayout() {
        projectLayout.removeAll();
        FormLayout layoutWithBinder = new FormLayout();
        // Setting the desired responsive steps for the columns in the layout
        layoutWithBinder.setResponsiveSteps(
                new FormLayout.ResponsiveStep("25em", 1),
                new FormLayout.ResponsiveStep("32em", 2),
                new FormLayout.ResponsiveStep("40em", 3));


// Create the fields
        projectName.setValueChangeMode(ValueChangeMode.EAGER);
        projectName.setPlaceholder("Project Name");
        projectTags.setPlaceholder("Comma Separated Tags");
        projectTags.setValueChangeMode(ValueChangeMode.EAGER);
       // projectTags.setHelperText("Optional tags that serve as quick descriptors of the project" +
            //    " and can be used in searching and filtering.");
        projectDescription.setValueChangeMode(ValueChangeMode.EAGER);
        projectDescription.setPlaceholder("Describe the overarching project goal.");


        layoutWithBinder.add(projectName, projectTags);
        layoutWithBinder.add(projectDescription, 2);
        // layoutWithBinder.add(upload, 2);


// First name and last name are required fields
        projectName.setRequiredIndicatorVisible(true);

        projectBinder.forField(projectName)
                .withValidator(new StringLengthValidator(
                        "Please add a project name at least 5 characters in length.", 5, null))
                .bind(ProjectForUpload::getAssetName, ProjectForUpload::setAssetName);

        projectBinder.forField(projectDescription)
                .withValidator(new StringLengthValidator(
                        "Please add a few more words to your project description.", 10, null))
                .bind(ProjectForUpload::getDescription, ProjectForUpload::setDescription);


        projectLayout.setClassName("new-project-view");
        H3 projectHeader = new H3("Project Details");
        Span projectInfo = new Span("Projects are the primary groups in your workspace. They hold design paths " +
                "and allow you to share, organize, and manage optical projects. You can also set project requirements, " +
                "which every design path in your project will be reconciled against.");
        projectInfo.setClassName("project-info");
        //projectDescription.setHelperText("Describe the overarching project goal.");

        //projectHeader.getStyle().set("text-decoration","underline");
        projectLayout.add(projectHeader, projectInfo, layoutWithBinder);
        return projectLayout;
    }

    private VerticalLayout createDesignPathLayout() {
        designPathLayout.removeAll();
        FormLayout layoutWithBinder = new FormLayout();
        // Setting the desired responsive steps for the columns in the layout
        layoutWithBinder.setResponsiveSteps(
                new FormLayout.ResponsiveStep("25em", 1),
                new FormLayout.ResponsiveStep("32em", 2),
                new FormLayout.ResponsiveStep("40em", 3));

        // Create the fields
        designPathName.setValueChangeMode(ValueChangeMode.EAGER);
        designPathName.setPlaceholder("Design Path Name");
        designPathTags.setPlaceholder("Comma Separated Tags");
        designPathTags.setValueChangeMode(ValueChangeMode.EAGER);
        //designPathTags.setHelperText("Optional descriptive tags for searching and filtering.");
        designPathDescription.setValueChangeMode(ValueChangeMode.EAGER);
        designPathDescription.setPlaceholder("Describe what the methodology and goal of the design path.");
        //designPathDescription.setHelperText("Describe what the methodology and goal of the design path.");


        layoutWithBinder.add(designPathName, designPathTags);
        layoutWithBinder.add(designPathDescription, 2);
        // layoutWithBinder.add(upload, 2);
        //
        // First name and last name are required fields
        designPathName.setRequiredIndicatorVisible(true);
        designPathTags.setRequiredIndicatorVisible(true);

        designPathBinder.forField(designPathName)
                .withValidator(new StringLengthValidator(
                        "Please add a designPath name at least 5 characters in length.", 5, null))
                .bind(DesignPathForUpload::getAssetName, DesignPathForUpload::setAssetName);

        designPathBinder.forField(designPathDescription)
                .withValidator(new StringLengthValidator(
                        "Please add a few more words to your designPath description.", 10, null))
                .bind(DesignPathForUpload::getDescription, DesignPathForUpload::setDescription);


        designPathLayout.setClassName("new-designPath-view");
        H3 designPathHeader = new H3("Design Path Details");
        Span designPathInfo = new Span("Design paths are the unique design approaches explored for a project. They " +
                "house all files which are associated with a design approach.");
        designPathInfo.setClassName("design-path-info");
        //designPathHeader.getStyle().set("text-decoration","underline");
        designPathLayout.add(designPathHeader, designPathInfo, layoutWithBinder);
        return designPathLayout;
    }
    private VerticalLayout createDesignFileLayout() {
        FormLayout layoutWithBinder = new FormLayout();
        // Setting the desired responsive steps for the columns in the layout
        layoutWithBinder.setResponsiveSteps(
                new FormLayout.ResponsiveStep("25em", 1),
                new FormLayout.ResponsiveStep("32em", 2),
                new FormLayout.ResponsiveStep("40em", 3));


// The object that will be edited

// Create the fields
        designFileName.setValueChangeMode(ValueChangeMode.EAGER);
        designFileName.setPlaceholder("File Name");
        designFileTags.setPlaceholder("Comma Separated Tags");
        designFileTags.setValueChangeMode(ValueChangeMode.EAGER);
        designFileDescription.setValueChangeMode(ValueChangeMode.EAGER);
        designFileDescription.setPlaceholder("Describe the file and how it fits into the design path.");


// Upload
        MemoryBuffer buffer = new MemoryBuffer();
        Upload upload = new Upload(buffer);

        // Max file size is now 50 mb
        upload.setMaxFileSize(50000000);

        Div output = new Div();


        upload.addSucceededListener(event -> {

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
        });


        layoutWithBinder.add(designFileName, designFileTags);
        layoutWithBinder.add(designFileDescription, 2);
        layoutWithBinder.add(upload, 2);

// First name and last name are required fields
        designFileName.setRequiredIndicatorVisible(true);
        designFileTags.setRequiredIndicatorVisible(true);

        designFileBinder.forField(designFileName)
                .withValidator(new StringLengthValidator(
                        "Please add a file name with at least 5 characters.", 5, null))
                .bind(AssetObjectForUpload::getAssetName, AssetObjectForUpload::setAssetName);

        designFileBinder.forField(designFileDescription)
                .withValidator(new StringLengthValidator(
                        "Please add a few more words to your file description.", 10, null))
                .bind(AssetObjectForUpload::getDescription, AssetObjectForUpload::setDescription);


        H3 designFileHeader = new H3("File Details");
        Span designFileInfo = new Span("The files in your design path are the actual key assets in a design. Any files " +
                "can be included, but only Zemax designs will be parsed and have their metadata provided.");
        designFileInfo.setClassName("file-class-upload");
        //designFileHeader.getStyle().set("text-decoration","underline");
        designFileLayout.add(designFileHeader, designFileInfo, layoutWithBinder);
        //designFileTags.setHelperText("Optional descriptors for each file for filtering and searching.");
        //designFileDescription.setHelperText("Describe the file and how it fits into the design path.");
        return designFileLayout;
    }

    private Boolean sendProject() {

        infoLabel.setText("Saved bean values: " + projectBeingUploaded);
        List<String> taglist = Arrays.stream(projectTags.getValue().split(","))
                .map(String::strip).collect(Collectors.toList());
        projectBeingUploaded.setTags(taglist);
        // For a new project at the top level, it is always a project type
        projectBeingUploaded.setAssetType("Project");
        projectBeingUploaded.setDescription(projectDescription.getValue().toString());


        HashMap<String, ProjectRequirement> requirements = new HashMap<>();
        ProjectRequirement focalLength = new ProjectRequirement(RequirementType.FocalLength, "100");
        requirements.put("Focal Length Requirement", focalLength);
        List<String> assetIds = new ArrayList<>();
        List<Integer> versions = new ArrayList<>();

        ProjectDataForProjectUpload projectData = new ProjectDataForProjectUpload(requirements, assetIds, versions);


        Boolean uploadSucceeded = false;
        try {
            Asset uploadedProject = opticsApi.sendNewProject(projectBeingUploaded, projectData);
            parentProjectKey = uploadedProject.getKey();
            uploadSucceeded = true;
            //UI.getCurrent().navigate(ProjectsView.class);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return uploadSucceeded;
    }

    private Boolean sendDesignPath() {
        infoLabel.setText("Saved bean values: " + designPathBeingUploaded);
        List<String> taglist = Arrays.stream(designPathTags.getValue().split(","))
                .map(String::strip).collect(Collectors.toList());
        designPathBeingUploaded.setTags(taglist);
        // For a new project at the top level, it is always a project type
        designPathBeingUploaded.setAssetType("Project");
        designPathBeingUploaded.setDescription(designPathDescription.getValue().toString());

        Boolean designSent = false;

        try {
            HashMap<String, ProjectRequirement> requirements = new HashMap<>();
            ProjectRequirement focalLength = new ProjectRequirement(RequirementType.FocalLength, "100");
            requirements.put("Focal Length", focalLength);
            List<String> assetIds = new ArrayList<>();
            List<Integer> versions = new ArrayList<>();


            ProjectDataForProjectUpload projectData =
                    new ProjectDataForProjectUpload(requirements, assetIds, versions);

            designPathBeingUploaded.setParentID(parentProjectKey);

            Asset uploadedAsset = opticsApi.sendNewDesignPath(designPathBeingUploaded, projectData);

            designPathKey = uploadedAsset.key;

            //return uploadedAsset;
            designSent = true;

            log.info("Design Path worked too");
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return designSent;


    }

    private Boolean sendDesignFile() {
            infoLabel.setText("Saved bean values: " + designFileBeingUploaded);
            List<String> taglist = Arrays.stream(designFileTags.getValue().split(","))
                    .map(String::strip).collect(Collectors.toList());
            designFileBeingUploaded.setTags(taglist);
            // For a new project at the top level, it is always a project type
            designFileBeingUploaded.setAssetType("File");
            designFileBeingUploaded.setDescription(designFileDescription.getValue().toString());

            Boolean fileSent = false;


            try {
                HashMap<String, Object> commitMap = new HashMap<String,Object>();
                DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
                LocalDateTime now = LocalDateTime.now();

                commitMap.put("commit - 0", designFileBeingUploaded.assetName + " added. "
                        + designFileBeingUploaded.description +
                        "DATE = " + dtf.format(now));
                designFileBeingUploaded.setMetadata(commitMap);

                designFileBeingUploaded.setParentKey(designPathKey);
                Asset uploadedAsset = opticsApi.sendFile(designFileBeingUploaded, assetData);

                fileSent = true;


            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
            return fileSent;

    }


}

