package com.example.firstapplication.entity;

public class Attendace {
    private Integer id;
    private String info;
    private String scannedDate;

    public Attendace(String info, String scannedDate) {
        this.info = info;
        this.scannedDate = scannedDate;
    }

    public Attendace(Integer id, String info, String scannedDate) {
        this.id = id;
        this.info = info;
        this.scannedDate = scannedDate;
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

    public String getScannedDate() {
        return scannedDate;
    }

    public void setScannedDate(String scannedDate) {
        this.scannedDate = scannedDate;
    }
}
