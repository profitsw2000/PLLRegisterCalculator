package ru.profitsw200.data.data

import android.util.Log
import ru.profitsw200.data.domain.PLLRegisters1208PL1URepository
import ru.profitsw200.data.model.LfmInputParametersModel
import ru.profitsw200.data.model.Registers1208PL1UDataModel
import ru.profitsw200.utils.TAG

const val MOD = 32_000
const val Fref = 20_000_000L
const val CTR1_RST = 0x840609
const val CTR1 = 0x840608
const val CTR2 = 0xA00002
const val CTR3 = 0xC00001
const val LFM3 = 0x500204
const val LFM31 = 0x500004
const val LFM31_SYM = 0x500006
const val INT_REG = 0x200000
const val FRAC_REG = 0x400000
const val LFM1_REG = 0x100000
const val LFM2_REG = 0x300000
const val MOD_REG = 0x607D00

class PLLRegisters1208PL1URepositoryImpl : PLLRegisters1208PL1URepository {

    override suspend fun getRegistersValue(lfmInputParametersModel: LfmInputParametersModel): Registers1208PL1UDataModel {

        return with(lfmInputParametersModel) {
            Registers1208PL1UDataModel(
                ref = getRefRegister(
                    lfmDeviationPeriod,
                    isSymmetricLfm
                ),
                int = getIntRegister(
                    lowestLfmFrequency,
                    lfmDeviationPeriod,
                    isSymmetricLfm
                ),
                frac = getFracRegister(
                    lowestLfmFrequency,
                    lfmDeviationPeriod,
                    isSymmetricLfm
                ),
                mod = MOD_REG,
                ctr1Rst = CTR1_RST,
                ctr1 = CTR1,
                ctr2 = CTR2,
                ctr3 = CTR3,
                lfm1 = getLfm1Register(
                    lowestLfmFrequency,
                    highestLfmFrequency,
                    lfmDeviationPeriod,
                    isSymmetricLfm
                ),
                lfm2 = getLfm2Register(
                    lfmDeviationPeriod,
                    isSymmetricLfm
                ),
                lfm3 = getLfm3Register(isSymmetricLfm),
                isSymmetric = isSymmetricLfm,
                ref1 = getRefRegister(
                    lfmDeviationPeriod,
                    isSymmetricLfm
                ),
                int1 = getInt1Register(
                    highestLfmFrequency,
                    lfmDeviationPeriod,
                    isSymmetricLfm
                ),
                frac1 = getFrac1Register(
                    highestLfmFrequency,
                    lfmDeviationPeriod,
                    isSymmetricLfm
                ),
                mod1 = MOD_REG,
                ctr1Rst1 = CTR1_RST,
                ctr11 = CTR1,
                ctr21 = CTR2,
                ctr31 = CTR3,
                lfm11 = getLfm1Register(
                    lowestLfmFrequency,
                    highestLfmFrequency,
                    lfmDeviationPeriod,
                    isSymmetricLfm
                ),
                lfm21 = getLfm2Register(
                    lfmDeviationPeriod,
                    isSymmetricLfm
                ),
                lfm31 = LFM31_SYM
            )
        }
    }

    private fun getRefRegister(
        lfmDeviationPeriod: Double,
        isSymmetricLfm: Boolean
    ): Int {
        return if (lfmDeviationPeriod <= 0.05 || isSymmetricLfm) 1
        else 2
    }

    private fun getIntRegister(
        lowestLfmFrequency: Long,
        lfmDeviationPeriod: Double,
        isSymmetricLfm: Boolean
    ): Int {
        val ref = getRefRegister(lfmDeviationPeriod, isSymmetricLfm)
        return (((lowestLfmFrequency*ref)/(4*Fref)).toInt()) or INT_REG
    }

    private fun getInt1Register(
        highestLfmFrequency: Long,
        lfmDeviationPeriod: Double,
        isSymmetricLfm: Boolean
    ): Int {
        val ref = getRefRegister(lfmDeviationPeriod, isSymmetricLfm)
        return (((highestLfmFrequency*ref)/(4*Fref)).toInt()) or INT_REG
    }

    private fun getFracRegister(
        lowestLfmFrequency: Long,
        lfmDeviationPeriod: Double,
        isSymmetricLfm: Boolean
    ): Int {
        val ref = getRefRegister(lfmDeviationPeriod, isSymmetricLfm)
        val fractionalMultPart = (lowestLfmFrequency*ref)%(4*Fref)
        return (((MOD*ref*fractionalMultPart)/(4*Fref)).toInt()) or FRAC_REG
    }

    private fun getFrac1Register(
        highestLfmFrequency: Long,
        lfmDeviationPeriod: Double,
        isSymmetricLfm: Boolean
    ): Int {
        val ref = getRefRegister(lfmDeviationPeriod, isSymmetricLfm)
        val fractionalMultPart = (highestLfmFrequency*ref)%(4*Fref)
        return (((MOD*ref*fractionalMultPart)/(4*Fref)).toInt()) or FRAC_REG
    }

    private fun getLfm2Register(
        lfmDeviationPeriod: Double,
        isSymmetricLfm: Boolean
    ): Int {
        val Fpfd = Fref/getRefRegister(lfmDeviationPeriod, isSymmetricLfm)
        var sawStep = 4000
        val fracIncRemain = if(isSymmetricLfm) ((lfmDeviationPeriod*Fpfd)%(2*sawStep)).toInt()
        else ((lfmDeviationPeriod*Fpfd)%(sawStep)).toInt()
        var fracInc = if(isSymmetricLfm) ((lfmDeviationPeriod*Fpfd)/(2*sawStep)).toInt()
        else ((lfmDeviationPeriod*Fpfd)/sawStep).toInt()
        if (fracIncRemain != 0) {
            fracInc += 1
            sawStep = if(isSymmetricLfm) ((lfmDeviationPeriod*Fpfd)/(2*fracInc)).toInt()
            else ((lfmDeviationPeriod*Fpfd)/(fracInc)).toInt()
        }

        return (sawStep shl 8) or (fracInc - 1) or LFM2_REG
    }

    private fun getLfm1Register(
        lowestLfmFrequency: Long,
        highestLfmFrequency: Long,
        lfmDeviationPeriod: Double,
        isSymmetricLfm: Boolean
    ): Int {
        val Fpfd = Fref/getRefRegister(lfmDeviationPeriod, isSymmetricLfm)
        val deviationFreq = (highestLfmFrequency - lowestLfmFrequency)/4
        val sawStep = ((getLfm2Register(lfmDeviationPeriod, isSymmetricLfm)) and 0xFFFFF) shr 8
        val dfrac = (((deviationFreq)*16*MOD)/(sawStep*Fpfd)).toInt()

        return dfrac or LFM1_REG
    }

    private fun getLfm3Register(isSymmetricLfm: Boolean): Int {
        return if (isSymmetricLfm) LFM3
        else LFM31
    }
}