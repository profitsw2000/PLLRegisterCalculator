package ru.profitsw200.presentation.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import ru.profitsw200.data.data.PLLRegisters1208PL1URepositoryImpl
import ru.profitsw200.data.domain.PLLRegisters1208PL1URepository
import ru.profitsw200.data.model.LfmInputParametersModel
import ru.profitsw200.data.state.PLLRegistersLoadState
import ru.profitsw200.utils.NO_ERROR

class MainViewModel(
    private val pllRegisters1208PL1URepository: PLLRegisters1208PL1URepository
): ViewModel() {

    private val ioCoroutineScope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private lateinit var lifecycleScope: CoroutineScope

    private val _pllRegistersLiveData: MutableLiveData<PLLRegistersLoadState> =
        MutableLiveData<PLLRegistersLoadState>()
    val pllRegistersLiveData: LiveData<PLLRegistersLoadState> by this::_pllRegistersLiveData

    fun setCoroutineScope(coroutineScope: CoroutineScope) {
        this.lifecycleScope = coroutineScope
    }

    fun calculatePllRegisters(lfmInputParametersModel: LfmInputParametersModel) {

    }

    private fun checkInputValues(lfmInputParametersModel: LfmInputParametersModel): Int {
        return NO_ERROR
    }

}