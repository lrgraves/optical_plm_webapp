package com.eleoptics.spark.api;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ReturnedFileData implements Serializable {

        @JsonProperty("file_name")
        public String fileName;
        @JsonProperty("extension")
        public String extension;
        public String data;

        public String getFileName() {
                return fileName;
        }

        public void setFileName(String fileName) {
                this.fileName = fileName;
        }

        public String getExtension() {
                return extension;
        }

        public void setExtension(String extension) {
                this.extension = extension;
        }

        public String getData() {
                return data;
        }

        public void setData(String data) {
                this.data = data;
        }


}
