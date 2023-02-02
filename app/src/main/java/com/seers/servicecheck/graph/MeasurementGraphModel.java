package com.seers.servicecheck.graph;

public class MeasurementGraphModel {
    private String dateStr;
    private float value;

    public MeasurementGraphModel(String dateStr, float value) {
        this.dateStr = dateStr;
        this.value = value;
    }

    public String getDateStr() {
        return dateStr;
    }

    public void setDateStr(String dateStr) {
        this.dateStr = dateStr;
    }

    public float getValue() {
        return value;
    }

    public void setValue(float value) {
        this.value = value;
    }
}
