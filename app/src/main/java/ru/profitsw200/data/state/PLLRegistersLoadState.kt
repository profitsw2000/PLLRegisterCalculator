package ru.profitsw200.data.state

import ru.profitsw200.data.model.Registers1208PL1UDataModel

sealed class PLLRegistersLoadState{
    data object Load: PLLRegistersLoadState()
    data class Error(val errorCode: Int): PLLRegistersLoadState()
    data class Success(val registers1208PL1UDataModel: Registers1208PL1UDataModel): PLLRegistersLoadState()
}