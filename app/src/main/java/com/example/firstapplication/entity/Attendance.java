package com.example.firstapplication.entity;

import androidx.annotation.NonNull;

public class Attendance {
    private Integer id;
    private String info;
    private String type;
    private String scannedDate;
    private Boolean isSynced;

    public Attendance(String info, String type) {
        this.info = info;
        this.type = type;
    }

    public Attendance(Integer id, String info, String type, String scannedDate, Boolean isSynced) {
        this.id = id;
        this.info = info;
        this.type = type;
        this.scannedDate = scannedDate;
        this.isSynced = isSynced;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getInfo() {
        return info;
    }

    public void setInfo(String info) {
        this.info = info;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getScannedDate() {
        return scannedDate;
    }

    public void setScannedDate(String scannedDate) {
        this.scannedDate = scannedDate;
    }

    public Boolean getSynced() {
        return isSynced;
    }

    public void setSynced(Boolean synced) {
        isSynced = synced;
    }

    @NonNull
    @Override
    public String toString() {
        return String.format("ID: %s - Info: %s - Scanned Date: %s - Type: %s - isSynced: %s",
                getId(), getInfo(), getScannedDate(), getType(), getSynced());
    }
}
