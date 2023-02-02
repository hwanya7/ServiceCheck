package com.seers.homemonitoring.data_class

enum class MeasurementType(
    val minValue: Float,
    val maxValue: Float,
    val lowDangerValue: Float,
    val highDangerValue: Float,
    val textValueLocation: Float
) {
    TEMPERATURE(33f, 40f, 34f, 37.7f, 38f),
    SPO2(85f, 100f, 95f, -1f, -1f),
    BLOOD_SUGAR(0f, 200f, 100f, 140f, -1f),
    BLOOD_PRESS(70f, 170f, 90f, 160f, -1f),
    HEART_RATE(0f, 200f, -1f, 100f, -1f);
}