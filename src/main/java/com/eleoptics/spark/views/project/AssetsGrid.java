package com.eleoptics.spark.views.project;

import com.eleoptics.spark.api.Asset;
import com.eleoptics.spark.api.OpticsApi;
import com.vaadin.flow.component.*;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H5;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextAreaVariant;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.textfield.TextFieldVariant;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.MultiFileMemoryBuffer;
import com.vaadin.flow.data.renderer.LocalDateTimeRenderer;
import com.vaadin.flow.internal.MessageDigestUtil;
import com.vaadin.flow.server.StreamResource;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class AssetsGrid extends VerticalLayout {

    @Autowired
    OpticsApi opticsApi;

    Grid<Asset> grid = new Grid<>(Asset.class);
    List<Asset> fullAssetList = new ArrayList<>();
    Select<Asset> assetSelect = new Select<>();



    public void setAssetGridView(List<Asset> assetList) {
        fullAssetList = assetList;
        // add blank asset to start
        grid.removeAllColumns();
        grid.setItems(assetList);

        grid.setColumns("assetName");

        grid.addColumn(new LocalDateTimeRenderer<>(
                Asset::getLastModified,
                "HH:mm MM/dd/yyyy")
        ).setHeader("Last Modified").setSortable(true);

    }


    final DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm");


    public AssetsGrid() {

        // add upload area
        MultiFileMemoryBuffer buffer = new MultiFileMemoryBuffer();
        Upload upload = new Upload(buffer);
        Div output = new Div();

        upload.addSucceededListener(event -> {
            Component component = createComponent(event.getMIMEType(),
                    event.getFileName(),
                    buffer.getInputStream(event.getFileName()));
            showOutput(event.getFileName(), component, output);
        });
        upload.addFileRejectedListener(event -> {
            Paragraph component = new Paragraph();
            showOutput(event.getErrorMessage(), component, output);
        });

        add(upload, output);

        // define pages
        setHeightFull();
        setWidthFull();

        grid.setColumns("assetName");

        grid.addColumn(new LocalDateTimeRenderer<>(
                Asset::getLastModified,
                "HH:mm MM/dd/yyyy")
        ).setHeader("Last Modified").setSortable(true);

        add(new H5("Design Path Files"));

        add(grid);




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
            image.setSizeFull();
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
        try {
            text = IOUtils.toString(stream, StandardCharsets.UTF_8);
        } catch (IOException e) {
            text = "exception reading stream";
        }
        return new Text(text);
    }

    private void createFileSelectionComponent() {
        assetSelect.setItemLabelGenerator(Asset::getAssetName);
        assetSelect.setItems(fullAssetList);
        assetSelect.setPlaceholder("New Asset");
        assetSelect.setLabel("File Replaced?");
        assetSelect.setEmptySelectionCaption("New Asset");
        assetSelect.setEmptySelectionAllowed(false);
    }

    private void showOutput(String uploadString, Component content,
                            HasComponents outputContainer) {
        HorizontalLayout uploadedAssetLayout = new HorizontalLayout();

        TextField uploadTitle = new TextField("Asset Name");
        uploadTitle.setValue(uploadString);
        uploadTitle.addThemeVariants(TextFieldVariant.LUMO_HELPER_ABOVE_FIELD);

        createFileSelectionComponent();

        uploadedAssetLayout.add(uploadTitle, assetSelect);

        uploadedAssetLayout.setAlignItems(Alignment.START);

        outputContainer.add(uploadedAssetLayout);
    }


}
