package ru.profitsw200.presentation.view

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import org.koin.androidx.viewmodel.ext.android.viewModel
import ru.profitsw200.R
import ru.profitsw200.data.model.LfmInputParametersModel
import ru.profitsw200.data.model.Registers1208PL1UDataModel
import ru.profitsw200.data.state.PLLRegistersLoadState
import ru.profitsw200.databinding.FragmentMainBinding
import ru.profitsw200.presentation.viewmodel.MainViewModel
import ru.profitsw200.utils.HIGH_FREQUENCY_ABOVE_INPUT_ERROR
import ru.profitsw200.utils.HIGH_FREQUENCY_UNDER_INPUT_ERROR
import ru.profitsw200.utils.LOW_FREQUENCY_ABOVE_INPUT_ERROR
import ru.profitsw200.utils.LOW_FREQUENCY_UNDER_INPUT_ERROR
import ru.profitsw200.utils.LOW_FREQ_HIGHER_THAN_HIGH_FREQ_INPUT_ERROR
import ru.profitsw200.utils.MODULATION_PERIOD_ABOVE_INPUT_ERROR
import ru.profitsw200.utils.MODULATION_PERIOD_UNDER_INPUT_ERROR
import ru.profitsw200.utils.REGISTERS_CALCULATION_ERROR

class MainFragment : Fragment() {

    private var _binding: FragmentMainBinding? = null
    private val binding get() = _binding!!
    private val mainViewModel: MainViewModel by viewModel()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentMainBinding.bind(inflater.inflate(R.layout.fragment_main, container, false))
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mainViewModel.setCoroutineScope(viewLifecycleOwner.lifecycleScope)
        observeData()
        initViews()
    }

    private fun initViews() = with(binding) {
        calculateRegisterValueButton.setOnClickListener {
            clearInputFormsErrors()

            val lowestInputIsNotEmpty = isInputNotEmpty(lowestFrequencyValueTextInputLayout, lowestFrequencyValueTextInputEditText)
            val highestInputIsNotEmpty = isInputNotEmpty(highestFrequencyValueTextInputLayout, highestFrequencyValueTextInputEditText)
            val periodInputIsNotEmpty = isInputNotEmpty(periodValueTextInputLayout, periodValueTextInputEditText)

            if (lowestInputIsNotEmpty && highestInputIsNotEmpty && periodInputIsNotEmpty) {
                mainViewModel.calculatePllRegisters(
                    LfmInputParametersModel(
                        lowestFrequencyValueTextInputEditText.text.toString().toLong()*1_000_000,
                        highestFrequencyValueTextInputEditText.text.toString().toLong()*1_000_000,
                        periodValueTextInputEditText.text.toString().toDouble()*0.001,
                        symmetrySelectionCheckbox.isChecked
                    )
                )
            }
        }
        registersTitleTextView.setOnClickListener {
            val beatSignalBottomSheetFragment = BeatSignalBottomSheetFragment()

            beatSignalBottomSheetFragment.show(parentFragmentManager, "beat signal")
        }
    }

    private fun clearInputFormsErrors() = with(binding) {
        lowestFrequencyValueTextInputLayout.error = null
        highestFrequencyValueTextInputLayout.error = null
        periodValueTextInputLayout.error = null
    }

    private fun isInputNotEmpty(
        textInputLayout: TextInputLayout,
        textInputEditText: TextInputEditText
    ): Boolean = with(binding) {
        return if (textInputEditText.text?.isEmpty() == true) {
            textInputLayout.error = getString(R.string.empty_input_error_text)
            false
        } else {
            true
        }
    }

    private fun observeData() {
        val observer = Observer<PLLRegistersLoadState> { renderData(it) }
        mainViewModel.pllRegistersLiveData.observe(viewLifecycleOwner, observer)
    }

    private fun renderData(pllRegistersLoadState: PLLRegistersLoadState) {
        when(pllRegistersLoadState) {
            is PLLRegistersLoadState.Error -> handleError(pllRegistersLoadState.errorCode)
            PLLRegistersLoadState.Load -> setProgressBarState(true)
            is PLLRegistersLoadState.Success -> populateRegisterValueViews(pllRegistersLoadState.registers1208PL1UDataModel)
        }
    }

    private fun handleError(errorCode: Int) {
        setProgressBarState(false)
        if ((errorCode and LOW_FREQUENCY_UNDER_INPUT_ERROR) != 0 ||
            (errorCode and LOW_FREQUENCY_ABOVE_INPUT_ERROR) != 0 ||
            (errorCode and LOW_FREQ_HIGHER_THAN_HIGH_FREQ_INPUT_ERROR) != 0)
            handleLowFrequencyInputError(errorCode)

        if ((errorCode and HIGH_FREQUENCY_ABOVE_INPUT_ERROR) != 0 ||
            (errorCode and HIGH_FREQUENCY_UNDER_INPUT_ERROR) != 0)
            handleHighFrequencyInputError(errorCode)

        if ((errorCode and MODULATION_PERIOD_UNDER_INPUT_ERROR) != 0 ||
            (errorCode and MODULATION_PERIOD_ABOVE_INPUT_ERROR) != 0)
            handleLfmPeriodInputError(errorCode)

        if ((errorCode and REGISTERS_CALCULATION_ERROR) != 0)
            handleRegistersCalculationError(errorCode)
    }

    private fun handleLowFrequencyInputError(errorCode: Int) = with(binding) {
        var errorString = ""

        if ((errorCode and LOW_FREQUENCY_UNDER_INPUT_ERROR) != 0) errorString = "$errorString${getString(R.string.low_freq_under_input_error_text)}"
        if ((errorCode and LOW_FREQUENCY_ABOVE_INPUT_ERROR) != 0) errorString = "$errorString${getString(R.string.low_freq_above_input_error_text)}"
        if ((errorCode and LOW_FREQ_HIGHER_THAN_HIGH_FREQ_INPUT_ERROR) != 0) errorString = "$errorString${getString(R.string.low_freq_higher_than_high_freq_input_error_text)}"

        lowestFrequencyValueTextInputLayout.error = errorString
    }

    private fun handleHighFrequencyInputError(errorCode: Int) = with(binding) {
        var errorString = ""

        if ((errorCode and HIGH_FREQUENCY_UNDER_INPUT_ERROR) != 0) errorString = "$errorString${getString(R.string.high_freq_under_input_error_text)}"
        if ((errorCode and HIGH_FREQUENCY_ABOVE_INPUT_ERROR) != 0) errorString = "$errorString${getString(R.string.high_freq_above_input_error_text)}"

        highestFrequencyValueTextInputLayout.error = errorString
    }

    private fun handleLfmPeriodInputError(errorCode: Int) = with(binding) {
        var errorString = ""

        if ((errorCode and MODULATION_PERIOD_ABOVE_INPUT_ERROR) != 0) errorString = "$errorString${getString(R.string.modulation_period_above_input_error_text)}"
        if ((errorCode and MODULATION_PERIOD_UNDER_INPUT_ERROR) != 0) errorString = "$errorString${getString(R.string.modulation_period_under_input_error_text)}"

        periodValueTextInputLayout.error = errorString
    }

    private fun handleRegistersCalculationError(errorCode: Int) = with(binding) {

        if ((errorCode and REGISTERS_CALCULATION_ERROR) != 0) {
            errorTextView.visibility = View.VISIBLE
            registersTitlesGroup.visibility = View.GONE
            profileRegistersValueGroup.visibility = View.GONE
            firstProfileRegistersValueGroup.visibility = View.GONE
        }
    }

    private fun setProgressBarState(isVisible: Boolean) = with(binding) {
        if (isVisible) progressBar.visibility = View.VISIBLE
        else progressBar.visibility = View.GONE
    }

    private fun populateRegisterValueViews(pllRegisters1208PL1UDataModel: Registers1208PL1UDataModel) = with(binding) {
        setProgressBarState(false)
        registersTitlesGroup.visibility = View.VISIBLE
        profileRegistersValueGroup.visibility = View.VISIBLE
        if (pllRegisters1208PL1UDataModel.isSymmetric) {
            firstProfileRegistersValueGroup.visibility = View.VISIBLE
            populateUpProfileRegisters(pllRegisters1208PL1UDataModel)
            populateDownProfileRegisters(pllRegisters1208PL1UDataModel)
        } else {
            firstProfileRegistersValueGroup.visibility = View.GONE
            populateUpProfileRegisters(pllRegisters1208PL1UDataModel)
        }
    }

    private fun populateUpProfileRegisters(pllRegisters1208PL1UDataModel: Registers1208PL1UDataModel) = with(binding) {
        refRegValueTitleTextView.text = pllRegisters1208PL1UDataModel.ref.toHexString()
        intRegValueTitleTextView.text = pllRegisters1208PL1UDataModel.int.toHexString()
        fracRegValueTitleTextView.text = pllRegisters1208PL1UDataModel.frac.toHexString()
        modRegValueTitleTextView.text = pllRegisters1208PL1UDataModel.mod.toHexString()
        ctr1RstRegValueTitleTextView.text = pllRegisters1208PL1UDataModel.ctr1Rst.toHexString()
        ctr1RegValueTitleTextView.text = pllRegisters1208PL1UDataModel.ctr1.toHexString()
        ctr2RegValueTitleTextView.text = pllRegisters1208PL1UDataModel.ctr2.toHexString()
        ctr3RegValueTitleTextView.text = pllRegisters1208PL1UDataModel.ctr3.toHexString()
        lfm1RegValueTitleTextView.text = pllRegisters1208PL1UDataModel.lfm1.toHexString()
        lfm2RegValueTitleTextView.text = pllRegisters1208PL1UDataModel.lfm2.toHexString()
        lfm3RegValueTitleTextView.text = pllRegisters1208PL1UDataModel.lfm3.toHexString()
    }

    private fun populateDownProfileRegisters(pllRegisters1208PL1UDataModel: Registers1208PL1UDataModel) = with(binding) {
        ref1RegValueTitleTextView.text = pllRegisters1208PL1UDataModel.ref1.toHexString()
        int1RegValueTitleTextView.text = pllRegisters1208PL1UDataModel.int1.toHexString()
        frac1RegValueTitleTextView.text = pllRegisters1208PL1UDataModel.frac1.toHexString()
        mod1RegValueTitleTextView.text = pllRegisters1208PL1UDataModel.mod1.toHexString()
        ctr11RstRegValueTitleTextView.text = pllRegisters1208PL1UDataModel.ctr1Rst1.toHexString()
        ctr11RegValueTitleTextView.text = pllRegisters1208PL1UDataModel.ctr11.toHexString()
        ctr21RegValueTitleTextView.text = pllRegisters1208PL1UDataModel.ctr21.toHexString()
        ctr31RegValueTitleTextView.text = pllRegisters1208PL1UDataModel.ctr31.toHexString()
        lfm11RegValueTitleTextView.text = pllRegisters1208PL1UDataModel.lfm11.toHexString()
        lfm21RegValueTitleTextView.text = pllRegisters1208PL1UDataModel.lfm21.toHexString()
        lfm31RegValueTitleTextView.text = pllRegisters1208PL1UDataModel.lfm31.toHexString()
    }

    fun Int.toHexString(): String {
        return "0x" + this.toString(16).uppercase()
    }

    companion object {
        @JvmStatic
        fun newInstance() = MainFragment()
    }
}