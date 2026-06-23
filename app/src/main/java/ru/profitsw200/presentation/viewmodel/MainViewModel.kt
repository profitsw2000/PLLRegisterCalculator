package ru.profitsw200.presentation.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import ru.profitsw200.data.domain.BeatSignalFrequencyCalculatorRepository
import ru.profitsw200.data.domain.PLLRegisters1208PL1URepository
import ru.profitsw200.data.model.BeatSignalParametersModel
import ru.profitsw200.data.model.LfmInputParametersModel
import ru.profitsw200.data.model.Registers1208PL1UDataModel
import ru.profitsw200.data.state.BeatSignalParametersResultLoadState
import ru.profitsw200.data.state.PLLRegistersLoadState
import ru.profitsw200.utils.HIGH_FREQUENCY_ABOVE_INPUT_ERROR
import ru.profitsw200.utils.HIGH_FREQUENCY_UNDER_INPUT_ERROR
import ru.profitsw200.utils.LOW_FREQUENCY_ABOVE_INPUT_ERROR
import ru.profitsw200.utils.LOW_FREQUENCY_UNDER_INPUT_ERROR
import ru.profitsw200.utils.LOW_FREQ_HIGHER_THAN_HIGH_FREQ_INPUT_ERROR
import ru.profitsw200.utils.MAX_LFM_FREQ
import ru.profitsw200.utils.MAX_LFM_PERIOD_MS
import ru.profitsw200.utils.MIN_LFM_FREQ
import ru.profitsw200.utils.MIN_LFM_PERIOD_MS
import ru.profitsw200.utils.MODULATION_PERIOD_ABOVE_INPUT_ERROR
import ru.profitsw200.utils.MODULATION_PERIOD_UNDER_INPUT_ERROR
import ru.profitsw200.utils.NANO_SECONDS_FACTOR
import ru.profitsw200.utils.NO_ERROR
import ru.profitsw200.utils.REGISTERS_CALCULATION_ERROR

class MainViewModel(
    private val pllRegisters1208PL1URepository: PLLRegisters1208PL1URepository,
    private val beatSignalFrequencyCalculatorRepository: BeatSignalFrequencyCalculatorRepository
): ViewModel() {

    private val ioCoroutineScope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private lateinit var lifecycleScope: CoroutineScope
    private var lfmInputParametersModel = LfmInputParametersModel(
        lowestLfmFrequency = 13_250_000_000,
        highestLfmFrequency = 13_400_000_000,
        lfmDeviationPeriod = 0.05,
        isSymmetricLfm = false
    )

    private val _pllRegistersLiveData: MutableLiveData<PLLRegistersLoadState> =
        MutableLiveData<PLLRegistersLoadState>()
    val pllRegistersLiveData: LiveData<PLLRegistersLoadState> by this::_pllRegistersLiveData

    private val _beatSignalParamsLiveData: MutableLiveData<BeatSignalParametersResultLoadState> =
        MutableLiveData<BeatSignalParametersResultLoadState>()
    val beatSignalParamsLiveData: LiveData<BeatSignalParametersResultLoadState> by this::_beatSignalParamsLiveData

    fun calculatePllRegisters(
        lfmInputParametersModel: LfmInputParametersModel,
        coroutineScope: CoroutineScope
    ) {
        val errorCode = checkInputValues(lfmInputParametersModel)
        _pllRegistersLiveData.value = PLLRegistersLoadState.Load
        if (errorCode == NO_ERROR) {
            this.lfmInputParametersModel = lfmInputParametersModel
            coroutineScope.launch {
                val result = getPllRegistersFromRepository(lfmInputParametersModel)
                if (result != null) _pllRegistersLiveData.value = PLLRegistersLoadState.Success(result)
            }
        } else {
            _pllRegistersLiveData.value = PLLRegistersLoadState.Error(errorCode)
        }
    }

    private suspend fun getPllRegistersFromRepository(
        lfmInputParametersModel: LfmInputParametersModel
    ): Registers1208PL1UDataModel? {
        val deferred: Deferred<Registers1208PL1UDataModel?> = ioCoroutineScope.async {
            try {
                pllRegisters1208PL1URepository.getRegistersValue(lfmInputParametersModel)
            } catch (exc: Exception) {
                _pllRegistersLiveData.value = PLLRegistersLoadState.Error(REGISTERS_CALCULATION_ERROR)
                null
            }
        }
        return deferred.await()
    }

    fun calculateBeatSignalParameters(delayTimeNanoSeconds: Int,
                                      coroutineScope: CoroutineScope
    ) {
        _beatSignalParamsLiveData.value = BeatSignalParametersResultLoadState.Load
        coroutineScope.launch {
            val result = getBeatSignalParametersFromRepository(delayTimeNanoSeconds)
            if (result != null) _beatSignalParamsLiveData.value =
                BeatSignalParametersResultLoadState.Success(result)
        }
    }

    private suspend fun getBeatSignalParametersFromRepository(delayTimeNanoSeconds: Int): BeatSignalParametersModel? {
        val deferred: Deferred<BeatSignalParametersModel?> = ioCoroutineScope.async {
            try {
               beatSignalFrequencyCalculatorRepository.getBeatSignalParametersValue(
                   lfmInputParametersModel = lfmInputParametersModel,
                   delayTimeNanoSeconds*NANO_SECONDS_FACTOR
               )
            } catch (exc: Exception) {
                _beatSignalParamsLiveData.value = BeatSignalParametersResultLoadState.Error
                null
            }
        }
        return deferred.await()
    }

    private fun checkInputValues(lfmInputParametersModel: LfmInputParametersModel): Int {
        var errorCode = NO_ERROR

        lfmInputParametersModel.apply {
            if (lowestLfmFrequency < MIN_LFM_FREQ) errorCode = errorCode or LOW_FREQUENCY_UNDER_INPUT_ERROR
            if (lowestLfmFrequency > MAX_LFM_FREQ) errorCode = errorCode or LOW_FREQUENCY_ABOVE_INPUT_ERROR
            if (highestLfmFrequency - lowestLfmFrequency < 10_000_000) errorCode = errorCode or LOW_FREQ_HIGHER_THAN_HIGH_FREQ_INPUT_ERROR
            if (highestLfmFrequency < MIN_LFM_FREQ) errorCode = errorCode or HIGH_FREQUENCY_UNDER_INPUT_ERROR
            if (highestLfmFrequency > MAX_LFM_FREQ) errorCode = errorCode or HIGH_FREQUENCY_ABOVE_INPUT_ERROR
            if (lfmDeviationPeriod > MAX_LFM_PERIOD_MS) errorCode = errorCode or MODULATION_PERIOD_ABOVE_INPUT_ERROR
            if (lfmDeviationPeriod < MIN_LFM_PERIOD_MS) errorCode = errorCode or MODULATION_PERIOD_UNDER_INPUT_ERROR
        }
        return errorCode
    }

    fun getInputFormsData(): LfmInputParametersModel {
        return lfmInputParametersModel
    }

}