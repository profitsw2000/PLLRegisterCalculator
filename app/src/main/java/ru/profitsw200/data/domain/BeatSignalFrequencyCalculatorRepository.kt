package ru.profitsw200.data.domain

import ru.profitsw200.data.model.BeatSignalParametersModel
import ru.profitsw200.data.model.LfmInputParametersModel

interface BeatSignalFrequencyCalculatorRepository {

    suspend fun getBeatSignalParametersValue(
        lfmInputParametersModel: LfmInputParametersModel,
        delayTime: Double
    ): BeatSignalParametersModel

}