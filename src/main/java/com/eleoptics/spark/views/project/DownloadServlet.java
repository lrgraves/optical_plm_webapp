package com.eleoptics.spark.views.project;

import com.eleoptics.spark.api.Asset;
import com.eleoptics.spark.api.Assets;
import com.eleoptics.spark.api.OpticsApi;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Nullable;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.Base64;
import java.util.HashMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Slf4j
@WebServlet("/download")
public class DownloadServlet extends HttpServlet {
    private final int ARBITARY_SIZE = 1048;

    class FileObject {
        String fileExtension;
        String fileName;
        byte[] fileData;
    }

    class ZipObject {
        String fileExtension;
        String fileName;
        FileOutputStream fileData;
    }

    OpticsApi opticsApi;

    public DownloadServlet(OpticsApi opticsApi) {
        this.opticsApi = opticsApi;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        //Determine which type of download is being called. If it is only a file or all files
        if (req.getParameter("fileID") != null && !req.getParameter("fileID").isEmpty()) {
            String downloadID = req.getParameter("fileID");

            // by default set version to 0
            Integer downloadIDVersion = 0;
            if (!req.getParameter("version").isEmpty()) {
                // if version is provided, overwrite value
                downloadIDVersion = Integer.valueOf(req.getParameter("version"));
            }

            FileObject fileObject = fetchFile(downloadID, downloadIDVersion);

            String fileName = fileObject.fileName + "." + fileObject.fileExtension;

            // resp.setContentType("text/plain");
            resp.setHeader("Content-disposition", "attachment; filename=" + fileName);

            try (InputStream in = new ByteArrayInputStream(fileObject.fileData);//req.getServletContext().getResourceAsStream("/WEB-INF/sample.txt");
                 OutputStream out = resp.getOutputStream()) {

                byte[] buffer = new byte[ARBITARY_SIZE];

                int numBytesRead;
                while ((numBytesRead = in.read(buffer)) > 0) {
                    out.write(buffer, 0, numBytesRead);
                }
            }
        } else if (req.getParameter("parentID") != null && !req.getParameter("parentID").isEmpty()) {
            String downloadID = req.getParameter("parentID");

            // by default set version to 0
            Integer downloadIDVersion = null;
            if (!req.getParameter("version").isEmpty()) {
                // if version is provided, overwrite value
                downloadIDVersion = Integer.valueOf(req.getParameter("version"));
            }

            File outputFile = fetchFiles(downloadID, downloadIDVersion);

            // resp.setContentType("text/plain");
            resp.setHeader("Content-disposition", "attachment; filename=" + outputFile.getName());

            try (InputStream in = new FileInputStream(outputFile);//req.getServletContext().getResourceAsStream("/WEB-INF/sample.txt");
                 OutputStream out = resp.getOutputStream()) {

                byte[] buffer = new byte[ARBITARY_SIZE];

                int numBytesRead;
                while ((numBytesRead = in.read(buffer)) > 0) {
                    out.write(buffer, 0, numBytesRead);
                }
            }
        }
    }

    private FileObject fetchFile(String fileID, @Nullable Integer version) {

        Integer downloadVersion = 0;
        if (!version.equals(null)) {
            downloadVersion = version;
        }
        // Get asset requested
        Asset asset = opticsApi.
                getFile(fileID, downloadVersion);

        log.info("Download Asset: " + asset.toString());

        FileObject file = new FileObject();

        HashMap<String, HashMap<String, Object>> assetDetails = asset.getData();
        if (assetDetails != null) {
            System.out.println(assetDetails.toString());
            file.fileName = assetDetails.get("File").get("file_name").toString();
            file.fileExtension = assetDetails.get("File").get("extension").toString();
            file.fileData = Base64.getDecoder().decode(assetDetails.get("File").get("data").toString().getBytes());
        }
        return file;
    }

    ;

    private File fetchFiles(String parentID, @Nullable Integer downloadVersion) throws FileNotFoundException {

        // Get all assets for that project
        Assets assets = opticsApi.
                getDesignPaths(parentID, downloadVersion);

        // log.info("The download partenid id : "+ parentID + "and the asset list is : " + assets.toString());

        //Create file object
        Asset parentProject = opticsApi.
                getProjectNoData(parentID, null);

        //log.info("The parent project was succesfully fethced");

        // Replace file data with zip file
        FileOutputStream fos = new FileOutputStream(parentProject.assetName + ".zip");
        ZipOutputStream zipOut = new ZipOutputStream(fos);

        // For each asset, get the data and download :)
        assets.assets.forEach(asset ->
        {
            log.info("asset searched is:" + asset.assetName + asset.key);
            if (asset.assetType.equals("File")) {
                Asset foundAsset = opticsApi.getFile(asset.key, 0);

                log.info("file found: " + foundAsset.toString());
                HashMap<String, HashMap<String, Object>> assetDetails = foundAsset.getData();
                if (assetDetails != null) {
                    String fileName = assetDetails.get("File").get("file_name").toString();
                    String fileExtension = assetDetails.get("File").get("extension").toString();

                    byte[] decodedBytes = Base64.getDecoder().decode(assetDetails.get("File").get("data").toString().getBytes());

                    File fileToZip = new File(fileName + "." + fileExtension);
                    ZipEntry zipEntry = new ZipEntry(fileToZip.getName());
                    try {
                        log.info("file added");
                        zipOut.putNextEntry(zipEntry);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    try {
                        zipOut.write(decodedBytes, 0, decodedBytes.length);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                }
            }


        });

        try {
            zipOut.close();
            fos.close();

        } catch (IOException e) {
            e.printStackTrace();
        }

        ZipObject zipObject = new ZipObject();

        zipObject.fileName = parentProject.getAssetName();
        zipObject.fileExtension = new String(".zip");
        zipObject.fileData = fos;

        File outputFile = new File(parentProject.assetName + ".zip");

        return outputFile;
    }


}

