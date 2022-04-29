package com.eleoptics.spark.views.project;

import com.eleoptics.spark.api.Asset;
import com.eleoptics.spark.api.OpticsApi;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;

@Slf4j
public class ProjectHeaderView extends VerticalLayout {

    @Autowired
    OpticsApi opticsApi;

    VerticalLayout layout = new VerticalLayout();
    H1 projectName = new H1();
    Span projectVersion = new Span();
    Div projectImg = new Div();
    Asset fetchedProject = new Asset();

    public ProjectHeaderView() {
        layout.add(projectName);
        layout.add(projectVersion);
        layout.add(projectImg);
        layout.setJustifyContentMode(JustifyContentMode.START);
        layout.setClassName("project-details-view-header");
        add(layout);
    }

    public void setProjectHeaderView(Asset project, Integer latestVersion) throws IOException {

        // In order, the project name, the version and last updated, the description, and the metadata table
        projectName.removeAll();
        projectVersion.removeAll();
        projectImg.removeAll();

        // Define the project name
        projectName.setText(project.assetName);

        Select<String> select = new Select<>();

        List<String> versionList = new ArrayList<>();
        for (int i = 0; i <= latestVersion; i++) {
            versionList.add(Integer.toString(i));
        }
        select.setItems(versionList);

        add(select);


        projectVersion.add("Version: ");
        projectVersion.add(select);

        projectVersion.add(" | Last Modified: " + project.getLastModifiedFormatted());



        /*
        byte[] decodedBytes = Base64.getDecoder().decode(project.getData().getBytes());
        //FileUtils.writeByteArrayToFile(new File("/home/logan/Downloads/testTrial.jpeg"), decodedBytes);
        //Image image1 = new Image("file:///home/logan/Downloads/testTrial.jpeg", "ASSMAN");
        projectImg.add(decodedBytes.toString());

*/
        getFileData(project);


    }

    private void getFileData(Asset project) throws IOException {
        HashMap<String, HashMap<String, Object>> assetDetails = project.getData();

        if(assetDetails.containsKey("File")) {
            HashMap<String, Object> fileDetails = assetDetails.get("File");
            String fileName = fileDetails.get("file_name").toString();
            String fileExtension = fileDetails.get("extension").toString();

            byte[] decodedBytes = Base64.getDecoder().decode(fileDetails.get("data").toString().getBytes());
            FileUtils.writeByteArrayToFile(new File("/home/logan/Downloads/" + fileName + "." + fileExtension), decodedBytes);
            Image image1 = new Image("file:///home/logan/Downloads/" + fileName + "." + fileExtension, "ASSMAN");
            projectImg.add(image1);

        }
        if(assetDetails.containsKey("Project")) {

            HashMap<String, Object> projectDetails = assetDetails.get("File");
        }
    }


}
