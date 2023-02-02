package com.seers.servicecheck.graph;

public class BloodPressGraphModel {
    private String dateStr;
    private float systolicValue;    //수축기 혈압
    private float diastolicValue;   //이완기 혈압

    public BloodPressGraphModel(String dateStr, float systolicValue, float diastolicValue) {
        this.dateStr = dateStr;
        this.systolicValue = systolicValue;
        this.diastolicValue = diastolicValue;
    }

    public String getDateStr() {
        return dateStr;
    }

    public void setDateStr(String dateStr) {
        this.dateStr = dateStr;
    }

    public float getSystolicValue() {
        return systolicValue;
    }

    public void setSystolicValue(float systolicValue) {
        this.systolicValue = systolicValue;
    }

    public float getDiastolicValue() {
        return diastolicValue;
    }

    public void setDiastolicValue(float diastolicValue) {
        this.diastolicValue = diastolicValue;
    }
}
