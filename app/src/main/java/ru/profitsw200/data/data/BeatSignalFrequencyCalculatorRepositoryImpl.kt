package ru.profitsw200.data.data

import ru.profitsw200.data.domain.BeatSignalFrequencyCalculatorRepository
import ru.profitsw200.data.model.BeatSignalParametersModel
import ru.profitsw200.data.model.LfmInputParametersModel

const val KHz_FACTOR = 1_000
const val MICRO_SECS_FACTOR = 1_000_000

class BeatSignalFrequencyCalculatorRepositoryImpl : BeatSignalFrequencyCalculatorRepository {

    override suspend fun getBeatSignalParametersValue(
        lfmInputParametersModel: LfmInputParametersModel,
        delayTime: Double
    ): BeatSignalParametersModel {
        return BeatSignalParametersModel(
            calculateBeatSignalFrequency(
                (lfmInputParametersModel.highestLfmFrequency - lfmInputParametersModel.lowestLfmFrequency),
                lfmInputParametersModel.lfmDeviationPeriod,
                delayTime,
                lfmInputParametersModel.isSymmetricLfm
            ),
            calculateBeatSignalPeriod(
                (lfmInputParametersModel.highestLfmFrequency - lfmInputParametersModel.lowestLfmFrequency),
                lfmInputParametersModel.lfmDeviationPeriod,
                delayTime,
                lfmInputParametersModel.isSymmetricLfm
            )
        )
    }

    private fun calculateBeatSignalFrequency(
        lfmDeviationFrequency: Long,
        lfmPeriod: Double,
        delayTime: Double,
        isSymmetricLfm: Boolean
    ): Double {
        return if (isSymmetricLfm)
                (((2*delayTime*lfmDeviationFrequency)/lfmPeriod)/KHz_FACTOR)
        else (((delayTime*lfmDeviationFrequency)/lfmPeriod)/KHz_FACTOR)
    }

    private fun calculateBeatSignalPeriod(
        lfmDeviationFrequency: Long,
        lfmPeriod: Double,
        delayTime: Double,
        isSymmetricLfm: Boolean
    ): Int {
        return if (isSymmetricLfm)
            (((lfmPeriod)/(2*delayTime*lfmDeviationFrequency))*MICRO_SECS_FACTOR).toInt()
        else ((lfmPeriod/(delayTime*lfmDeviationFrequency))*MICRO_SECS_FACTOR).toInt()
    }
}