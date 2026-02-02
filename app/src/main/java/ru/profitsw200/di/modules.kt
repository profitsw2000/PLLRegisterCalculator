package ru.profitsw200.di

import org.koin.dsl.module
import ru.profitsw200.data.data.BeatSignalFrequencyCalculatorRepositoryImpl
import ru.profitsw200.data.data.PLLRegisters1208PL1URepositoryImpl
import ru.profitsw200.data.domain.BeatSignalFrequencyCalculatorRepository
import ru.profitsw200.data.domain.PLLRegisters1208PL1URepository
import ru.profitsw200.presentation.viewmodel.MainViewModel

val mainModule = module {
    single<PLLRegisters1208PL1URepository> { PLLRegisters1208PL1URepositoryImpl() }
    single<BeatSignalFrequencyCalculatorRepository> { BeatSignalFrequencyCalculatorRepositoryImpl() }
    single { MainViewModel(get(), get()) }
}