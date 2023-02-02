package com.seers.homemonitoring.data_class

data class BloodPressGraphModel(
    var dateStr: String,
    var systolicValue: Float,
    var diastolicValue: Float,
    var round: Int //차수
)