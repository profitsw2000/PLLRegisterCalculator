package ru.profitsw200.data.domain

import ru.profitsw200.data.model.LfmInputParametersModel
import ru.profitsw200.data.model.Registers1208PL1UDataModel

interface PLLRegisters1208PL1URepository {

    suspend fun getRegistersValue(lfmInputParametersModel: LfmInputParametersModel): Registers1208PL1UDataModel

}