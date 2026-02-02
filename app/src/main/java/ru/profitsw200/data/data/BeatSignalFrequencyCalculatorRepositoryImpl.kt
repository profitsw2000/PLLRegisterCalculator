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
    ): Int {
        return if (isSymmetricLfm)
                ((lfmPeriod/(delayTime*lfmDeviationFrequency))*MICRO_SECS_FACTOR).toInt()
        else (((2*lfmPeriod)/(delayTime*lfmDeviationFrequency))*MICRO_SECS_FACTOR).toInt()
    }

    private fun calculateBeatSignalPeriod(
        lfmDeviationFrequency: Long,
        lfmPeriod: Double,
        delayTime: Double,
        isSymmetricLfm: Boolean
    ): Int {
        return if (isSymmetricLfm)
            (((delayTime*lfmDeviationFrequency)/lfmPeriod)/KHz_FACTOR).toInt()
        else (((delayTime*lfmDeviationFrequency)/(2*lfmPeriod))/KHz_FACTOR).toInt()
    }
}