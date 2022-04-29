package com.eleoptics.spark.views.project;

public class DesignPathVersionClass {
    public Integer versionNumber;
    public String versionCommitMessage;
    public String versionCreationDate;

    public Integer getVersionNumber() {
        return versionNumber;
    }

    public void setVersionNumber(Integer versionNumber) {
        this.versionNumber = versionNumber;
    }


    public String getVersionCommitMessage() {
        return versionCommitMessage;
    }

    public void setVersionCommitMessage(String versionCommitMessage) {
        this.versionCommitMessage = versionCommitMessage;
    }

    public String getVersionCreationDate() {
        return versionCreationDate;
    }

    public void setVersionCreationDate(String versionCreationDate) {
        this.versionCreationDate = versionCreationDate;
    }

    public DesignPathVersionClass(Integer versionNumber, String versionCommitMessage, String versionCreationDate) {
        this.versionNumber = versionNumber;
        this.versionCommitMessage = versionCommitMessage;
        this.versionCreationDate = versionCreationDate;
    }
}
