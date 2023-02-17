package com.seers.servicecheck.graph_new;

/*
* -1은 필요 없는 항목임
* */
public enum MeasurementType {

    TEMPERATURE (30f, 44f, 35f, 38f, 38f),
    SPO2        (85f, 100f, 95f, -1f, -1f),
    BLOOD_SUGAR (0f, 200f, 100f, 140f, -1f),
    BLOOD_PRESS (0f, 140f, 80f, 120f, -1f),
    HEART_RATE  (0f, 200f, -1, 100f, -1f);

    private float minValue;
    private float maxValue;
    private float lowDangerValue;
    private float highDangerValue;
    private float textValueLocation;

    MeasurementType(float minValue, float maxValue, float lowDangerValue, float highDangerValue, float textValueLocation) {
        this.minValue = minValue;
        this.maxValue = maxValue;
        this.lowDangerValue = lowDangerValue;
        this.highDangerValue = highDangerValue;
        this.textValueLocation = textValueLocation;

    }

    public float getMinValue() {
        return minValue;
    }

    public float getMaxValue() {
        return maxValue;
    }

    public float getLowDangerValue() {
        return lowDangerValue;
    }

    public float getHighDangerValue() {
        return highDangerValue;
    }

    public float getTextValueLocation() {
        return textValueLocation;
    }
}
