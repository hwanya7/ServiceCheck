package com.seers.homemonitoring.data_class

data class MeasurementGraphModel(
    var dateStr: String,
    var value: Float,
    var round: Int //차수
)