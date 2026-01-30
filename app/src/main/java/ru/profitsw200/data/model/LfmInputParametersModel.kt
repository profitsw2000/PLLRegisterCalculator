package ru.profitsw200.data.model

data class LfmInputParametersModel(
    val lowestLfmFrequency: Long,
    val highestLfmFrequency: Long,
    val lfmDeviationPeriod: Double,
    val isSymmetricLfm: Boolean
)
