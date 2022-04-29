package com.eleoptics.spark.api;

import org.springframework.core.io.FileSystemResource;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class AssetDataForProjectUpload {
    public FileSystemResource file;
    public AssetDataForProjectUpload() {

    }

    public FileSystemResource getFile() {
        return file;
    }

    public void setFile(FileSystemResource file) {
        this.file = file;
    }
}
