package com.eleoptics.spark.views.project;

import com.eleoptics.spark.api.Asset;
import com.eleoptics.spark.api.AssetDataForProjectUpload;
import com.eleoptics.spark.api.AssetObjectForUpload;
import com.eleoptics.spark.api.OpticsApi;
import com.eleoptics.spark.events.NavigationKeyStrings;
import com.eleoptics.spark.events.ProjectCardClickedEvent;
import com.eleoptics.spark.views.QueryParameterNames;
import com.eleoptics.spark.views.main.MainView;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.vaadin.flow.component.*;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.MemoryBuffer;
import com.vaadin.flow.component.upload.receivers.MultiFileMemoryBuffer;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.BinderValidationStatus;
import com.vaadin.flow.data.binder.BindingValidationStatus;
import com.vaadin.flow.data.validator.StringLengthValidator;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.internal.MessageDigestUtil;
import com.vaadin.flow.router.*;
import com.vaadin.flow.server.StreamResource;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.compress.utils.IOUtils;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;

import javax.annotation.PostConstruct;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import java.awt.*;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Route(value = "newFile", layout = MainView.class)
@PageTitle("NEW FILE")
@CssImport(value = "./styles/my-grid-style.css", themeFor = "vaadin-grid")
@JsModule("./styles/shared-styles.js")

@Slf4j
public class NewFiles extends VerticalLayout
        implements BeforeEnterObserver {


    @Autowired
    OpticsApi opticsApi;

    VerticalLayout page = new VerticalLayout();
    String dataString = new String();
    Asset designPath = new Asset();
    Asset parentProject = new Asset();
    List<Asset> assetList = new ArrayList<>();
    String fileExtension = new String();

    @SneakyThrows
    @Override
    public void beforeEnter(BeforeEnterEvent beforeEnterEvent) {
        String key = NavigationKeyStrings.selectedProjectAssets;
        List<Asset> allAssets = (List<Asset>) ComponentUtil.getData(UI.getCurrent(), key);


        // reset list
        Component c = UI.getCurrent();
        ComponentUtil.setData(c, key, allAssets);
        log.info("the asset list is: " + allAssets);

        // The parent project is the first project in the list
        designPath = allAssets.get(1);
        parentProject = allAssets.get(0);

        //Remove parent project and design path
        allAssets.remove(0);
        allAssets.remove(0);

        // Assign assets to list
        assetList = allAssets;


    }


    public NewFiles() {

// Upload
        MultiFileMemoryBuffer buffer = new MultiFileMemoryBuffer();
        Upload upload = new Upload(buffer);

        // Set max file size for upload to 50 mb
        upload.setMaxFileSize(50000000);
        Button finished = new Button("Done");
        Button cancel = new Button("Cancel");
        cancel.addThemeVariants(ButtonVariant.MATERIAL_CONTAINED);
        finished.addThemeVariants(ButtonVariant.MATERIAL_CONTAINED);

        Label infoLabel = new Label();

        Notification zemaxErrorDialog = new Notification("You already have a Zemax file in this design path." +
                "Please either upload a non-optical design file, or create a new design path for your new optical design file.");

        upload.addStartedListener(startedEvent -> zemaxErrorDialog.close());

        upload.addAllFinishedListener(event -> {
            // create the file info areas
            Set<String> allFiles = buffer.getFiles();

            for (String file : allFiles) {
                log.info("The file name is {} and the file data is {}", file, buffer.getFileData(file));
                VerticalLayout fileInfo = fileDetails(file, buffer.getInputStream(file));
                fileInfo.setWidthFull();
                add(fileInfo);
            }

        });

// Button bar
        HorizontalLayout actions = new HorizontalLayout();
        actions.add(finished, cancel);
        cancel.getStyle().set("padding-left", "16px");
        actions.getStyle().set("margin-top", "10px");

        cancel.addClickListener(event -> {
            UI.getCurrent().navigate(ProjectDetailsView.class);
        });
        page.setClassName("new-project-view");

        page.add(upload, actions, infoLabel);
        add(page);


        finished.addClickListener(event -> {
            // Establish the query parameters map
            Map<String, String> queryMap = new HashMap<>();
            queryMap.put(QueryParameterNames.projectId, designPath.key);
            queryMap.put(QueryParameterNames.version, "");
            QueryParameters queryParameters = QueryParameters.simple(queryMap);

            // Set a router link t the detail page
            RouterLink detailsPageLink = new RouterLink();
            detailsPageLink.setRoute(ProjectDetailsView.class);

            // Navigate to the details page with the query parameters
            UI.getCurrent().navigate(detailsPageLink.getHref(), queryParameters);

        });

        cancel.addClickListener(event -> {
            // Establish the query parameters map
            Map<String, String> queryMap = new HashMap<>();
            queryMap.put(QueryParameterNames.projectId, designPath.key);
            queryMap.put(QueryParameterNames.version, "");
            QueryParameters queryParameters = QueryParameters.simple(queryMap);

            // Set a router link t the detail page
            RouterLink detailsPageLink = new RouterLink();
            detailsPageLink.setRoute(ProjectDetailsView.class);

            // Navigate to the details page with the query parameters
            UI.getCurrent().navigate(detailsPageLink.getHref(), queryParameters);

        });
    }

    private VerticalLayout fileDetails(String fileName, InputStream stream) {

        VerticalLayout fileInformation = new VerticalLayout();

        FormLayout layoutWithBinder = new FormLayout();
        layoutWithBinder.setWidthFull();
        // Setting the desired responsive steps for the columns in the layout
        layoutWithBinder.setResponsiveSteps(
                new FormLayout.ResponsiveStep("25em", 1),
                new FormLayout.ResponsiveStep("32em", 2),
                new FormLayout.ResponsiveStep("40em", 3));

        Binder<AssetObjectForUpload> binder = new Binder<>();

        // The object that will be edited
        AssetObjectForUpload assetBeingUploaded = new AssetObjectForUpload();
        AssetDataForProjectUpload assetData = new AssetDataForProjectUpload();


        // Create the fields
        TextField assetName = new TextField();
        assetName.setValueChangeMode(ValueChangeMode.EAGER);
        assetName.setValue(fileName);
        TextField assetTags = new TextField();
        assetTags.setPlaceholder("File Tags (Comma Separated)");
        assetTags.setValueChangeMode(ValueChangeMode.EAGER);
        TextField description = new TextField();
        description.setValueChangeMode(ValueChangeMode.EAGER);
        description.setPlaceholder("Enter a short description of the files");


        // Create file for upload
        File tempFile = new File(fileName);
        try {
            FileUtils.copyInputStreamToFile(stream, tempFile);
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
        FileSystemResource uploadFile = new FileSystemResource(tempFile);

        AssetDataForProjectUpload fileAssetData = new AssetDataForProjectUpload();
        fileAssetData.setFile(uploadFile);

        layoutWithBinder.add(assetName, assetTags);
        layoutWithBinder.add(description, 2);

        // Button bar
        HorizontalLayout actions = new HorizontalLayout();
        Label infoLabel = new Label();
        Button save = new Button("Upload");
        save.setEnabled(true);
        Button reset = new Button("Remove");
        save.addThemeVariants(ButtonVariant.MATERIAL_CONTAINED);
        reset.addThemeVariants(ButtonVariant.MATERIAL_CONTAINED);
        Notification zemaxErrorDialog = new Notification("You already have a Zemax file in this design path." +
                "Please either upload a non-optical design file, or create a new design path for your new optical design file.");
        actions.add(save, reset);
        reset.getStyle().set("padding-left", "16px");
        actions.getStyle().set("margin-top", "10px");

        assetName.setRequiredIndicatorVisible(true);
        assetTags.setRequiredIndicatorVisible(true);

        // First, check if a zmx file was uploaded
        String[] fileNameArray = fileName.split("\\.");
        fileExtension = fileNameArray[fileNameArray.length-1];
        save.setEnabled(true);


        if (fileNameArray[fileNameArray.length - 1].equals("zmx")) {
            //check if the original asset list also has a .zmx file
            System.out.println("Zemax file uplodaed");
            for (Asset asset : assetList) {
                if (!asset.metadata.opticalMetadata.isPresent()) {
                    System.out.println("Zemax asset found already");
                    save.setEnabled(false);
                    zemaxErrorDialog.open();
                }
            }
        }
        ;

        binder.forField(assetName)
                .withValidator(new StringLengthValidator(
                        "Please add a file name with at least 5 characters.", 5, null))
                .bind(AssetObjectForUpload::getAssetName, AssetObjectForUpload::setAssetName);

        binder.forField(description)
                .withValidator(new StringLengthValidator(
                        "Please a description with at least 10 characters.", 10, null))
                .bind(AssetObjectForUpload::getDescription, AssetObjectForUpload::setDescription);


// First name and last name are required fields
        assetName.setRequiredIndicatorVisible(true);
        assetTags.setRequiredIndicatorVisible(true);

        binder.forField(assetName)
                .withValidator(new StringLengthValidator(
                        "Please add a project name with at least 5 characters.", 5, null))
                .bind(AssetObjectForUpload::getAssetName, AssetObjectForUpload::setAssetName);

        binder.forField(description)
                .withValidator(new StringLengthValidator(
                        "Please add a few more words to your project description.", 10, null))
                .bind(AssetObjectForUpload::getDescription, AssetObjectForUpload::setDescription);

// Click listeners for the buttons
        save.addClickListener(event -> {


            if (binder.writeBeanIfValid(assetBeingUploaded)) {
                List<String> taglist = Arrays.stream(assetTags.getValue().split(","))
                        .map(String::strip).collect(Collectors.toList());
                assetBeingUploaded.setTags(taglist);
                // For a new project at the top level, it is always a project type
                assetBeingUploaded.setAssetType("File");
                assetBeingUploaded.setDescription(description.getValue().toString());


                try {
                    HashMap<String, Object> commitMap = new HashMap<String, Object>();
                    DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
                    LocalDateTime now = LocalDateTime.now();

                    commitMap.put("commit - 0", assetBeingUploaded.assetName + " added. "
                            + assetBeingUploaded.description +
                            "DATE = " + dtf.format(now));
                    assetBeingUploaded.setMetadata(commitMap);

                    assetData.setFile(uploadFile);

                    assetBeingUploaded.setParentKey(designPath.getKey());
                    Asset uploadedAsset = opticsApi.sendFile(assetBeingUploaded, assetData);
                    //Notification sendFileNotification = new Notification();
                    //sendFileNotification.setText("Sending file, please wait.");
                    //sendFileNotification.open();
                    //Thread.sleep(2000);

                    HorizontalLayout successLayout = new HorizontalLayout();
                    Icon checkIcon = new Icon(VaadinIcon.CHECK_SQUARE_O);
                    checkIcon.setColor("green");
                    successLayout.add(checkIcon);
                    save.setEnabled(false);
                    reset.setEnabled(false);
                   save.setVisible(false);
                   reset.setVisible(false);
                    fileInformation.add(successLayout);


                } catch (JsonProcessingException e) {
                    e.printStackTrace();
                }
                //returnToGrid();

            } else {
                BinderValidationStatus<AssetObjectForUpload> validate = binder.validate();
                String errorText = validate.getFieldValidationStatuses()
                        .stream().filter(BindingValidationStatus::isError)
                        .map(BindingValidationStatus::getMessage)
                        .map(Optional::get).distinct()
                        .collect(Collectors.joining(", "));
                infoLabel.setText("There are errors: " + errorText);
            }
        });
        reset.addClickListener(event -> {
           fileInformation.removeAll();
           fileInformation.setVisible(false);
        });

        H3 designFileHeader = new H3("" + fileName + " Details");

        fileInformation.add(designFileHeader, layoutWithBinder, actions, infoLabel);

        return fileInformation;
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


}

