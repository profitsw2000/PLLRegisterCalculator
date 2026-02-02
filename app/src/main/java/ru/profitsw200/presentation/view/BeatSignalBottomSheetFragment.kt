package ru.profitsw200.presentation.view

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import org.koin.androidx.viewmodel.ext.android.viewModel
import ru.profitsw200.R
import ru.profitsw200.data.model.BeatSignalParametersModel
import ru.profitsw200.data.state.BeatSignalParametersResultLoadState
import ru.profitsw200.databinding.FragmentBeatSignalBottomSheetBinding
import ru.profitsw200.presentation.viewmodel.MainViewModel

class BeatSignalBottomSheetFragment : BottomSheetDialogFragment() {

    private var _binding: FragmentBeatSignalBottomSheetBinding? = null
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
        _binding = FragmentBeatSignalBottomSheetBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val bottomSheetBehavior = BottomSheetBehavior.from(view.parent as View)
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_HALF_EXPANDED
        val layout: CoordinatorLayout = binding.rootCoordinatorLayout
        layout.minimumHeight = 1500

        //mainViewModel.setCoroutineScope(viewLifecycleOwner.lifecycleScope)
        initViews()
        observeData()
    }

    private fun initViews() = with(binding) {
        calculateBeatSignalParametersButton.setOnClickListener {
            mainViewModel.calculateBeatSignalParameters(
                delayTimeValueTextInputEditText.text.toString().toInt(),
                viewLifecycleOwner.lifecycleScope
            )
        }
    }

    private fun observeData() {
        val observer = Observer<BeatSignalParametersResultLoadState> { renderData(it) }
        mainViewModel.beatSignalParamsLiveData.observe(viewLifecycleOwner, observer)
    }

    private fun renderData(beatSignalParametersResultLoadState: BeatSignalParametersResultLoadState) {
        when(beatSignalParametersResultLoadState) {
            BeatSignalParametersResultLoadState.Error -> handleError()
            BeatSignalParametersResultLoadState.Load -> setProgressBar(true)
            is BeatSignalParametersResultLoadState.Success -> populateViews(beatSignalParametersResultLoadState.beatSignalParametersModel)
        }
    }

    private fun setProgressBar(isVisible: Boolean) = with(binding) {
        if (isVisible) progressBar.visibility = View.VISIBLE
        else progressBar.visibility = View.GONE
    }

    private fun handleError() = with(binding) {
        setProgressBar(false)
        errorTextView.visibility = View.VISIBLE
        beatSignalParametersGroup.visibility = View.GONE
    }

    private fun populateViews(beatSignalParametersModel: BeatSignalParametersModel) = with(binding) {
        setProgressBar(false)
        errorTextView.visibility = View.GONE
        beatSignalParametersGroup.visibility = View.VISIBLE

        beatSignalFrequencyValueTextView.text = getString(R.string.beat_signal_frequency_value, beatSignalParametersModel.beatSignalFrequencyKHz)
        beatSignalPeriodValueTextView.text = getString(R.string.beat_signal_period_value, beatSignalParametersModel.beatSignalPeriodMicroSeconds)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        @JvmStatic
        fun newInstance() = BeatSignalBottomSheetFragment()
    }
}