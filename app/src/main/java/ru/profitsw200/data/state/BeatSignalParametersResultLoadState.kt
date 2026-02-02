package ru.profitsw200.data.state

import ru.profitsw200.data.model.BeatSignalParametersModel

sealed class BeatSignalParametersResultLoadState {
    data object Load: BeatSignalParametersResultLoadState()
    data object Error: BeatSignalParametersResultLoadState()
    data class Success(val beatSignalParametersModel: BeatSignalParametersModel): BeatSignalParametersResultLoadState()
}