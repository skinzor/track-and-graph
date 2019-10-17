package com.samco.grapheasy.graphstatinput

import android.app.Activity
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.*

import com.samco.grapheasy.R
import com.samco.grapheasy.database.*
import com.samco.grapheasy.databinding.FragmentGraphStatInputBinding
import org.threeten.bp.Period

class GraphStatInputFragment : Fragment() {
    private lateinit var binding: FragmentGraphStatInputBinding
    private lateinit var viewModel: GraphStatInputViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_graph_stat_input, container, false)
        binding.lifecycleOwner = viewLifecycleOwner
        viewModel = ViewModelProviders.of(this).get(GraphStatInputViewModel::class.java)
        viewModel.initViewModel(requireActivity())
        listenToGraphTypeSpinner()
        listenToMovingAveragePeriod()
        listenToTimePeriod()
        listenToAllFeatures()
        return binding.root
    }

    private fun listenToAllFeatures() {
        viewModel.allFeatures.observe(this, Observer {
            val itemNames = it.map { ft -> "${ft.trackGroupName} -> ${ft.name}" }
            val adapter = ArrayAdapter<String>(context!!, android.R.layout.simple_spinner_dropdown_item, itemNames)
            binding.pieChartFeatureSpinner.adapter = adapter
            when (val selected = viewModel.selectedPieChartFeature.value) {
                null -> viewModel.selectedPieChartFeature.value = it[0]
                else -> binding.pieChartFeatureSpinner.setSelection(it.indexOf(selected))
            }
            binding.pieChartFeatureSpinner.onItemSelectedListener = object: AdapterView.OnItemSelectedListener {
                override fun onNothingSelected(p0: AdapterView<*>?) { }
                override fun onItemSelected(p0: AdapterView<*>?, p1: View?, index: Int, p3: Long) {
                    viewModel.selectedPieChartFeature.value = it[index]
                }
            }
        })
    }

    private fun listenToTimePeriod() {
        binding.samplePeriodSpinner.onItemSelectedListener = object: AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(p0: AdapterView<*>?) { }
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, index: Int, p3: Long) {
                viewModel.samplePeriod.value = when(index) {
                    0 -> null
                    1 -> Period.ofDays(1)
                    2 -> Period.ofWeeks(1)
                    3 -> Period.ofMonths(1)
                    4 -> Period.ofYears(1)
                    else -> null
                }
            }
        }
    }

    private fun listenToMovingAveragePeriod() {
        binding.movingAverageSpinner.onItemSelectedListener = object: AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(p0: AdapterView<*>?) { }
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, index: Int, p3: Long) {
                viewModel.movingAveragePeriod.value = when (index) {
                    0 -> null
                    1 -> Period.ofDays(1)
                    2 -> Period.ofDays(3)
                    3 -> Period.ofWeeks(1)
                    4 -> Period.ofMonths(1)
                    5 -> Period.ofMonths(3)
                    6 -> Period.ofMonths(6)
                    7 -> Period.ofYears(1)
                    else -> null
                }
            }
        }
    }

    private fun listenToGraphTypeSpinner() {
        binding.graphTypeSpinner.onItemSelectedListener = object: AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(p0: AdapterView<*>?) { }
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, index: Int, p3: Long) {
                viewModel.graphStatType.value = when(index) {
                    0 -> GraphStatType.LINE_GRAPH
                    1 -> GraphStatType.PIE_CHART
                    2 -> GraphStatType.AVERAGE_TIME_BETWEEN
                    3 -> GraphStatType.TIME_SINCE
                    else -> GraphStatType.LINE_GRAPH
                }
            }
        }
        viewModel.graphStatType.observe(this, Observer { graphType ->
            when(graphType) {
                GraphStatType.LINE_GRAPH -> {
                    binding.movingAverageLayout.visibility = View.VISIBLE
                    binding.pieChartSelectFeatureLayout.visibility = View.GONE
                }
                GraphStatType.PIE_CHART -> {
                    binding.movingAverageLayout.visibility = View.GONE
                    binding.pieChartSelectFeatureLayout.visibility = View.VISIBLE
                }
                GraphStatType.AVERAGE_TIME_BETWEEN -> {
                    binding.movingAverageLayout.visibility = View.GONE
                    binding.pieChartSelectFeatureLayout.visibility = View.GONE
                }
                GraphStatType.TIME_SINCE -> {
                    binding.movingAverageLayout.visibility = View.GONE
                    binding.pieChartSelectFeatureLayout.visibility = View.GONE
                }
            }
        })
    }

}

class GraphStatInputViewModel : ViewModel() {
    private var dataSource: GraphEasyDatabaseDao? = null

    val graphStatType = MutableLiveData<GraphStatType>(GraphStatType.LINE_GRAPH)
    val movingAveragePeriod = MutableLiveData<Period?>(null)
    val samplePeriod = MutableLiveData<Period?>(null)
    val selectedPieChartFeature = MutableLiveData<FeatureAndTrackGroup?>(null)

    lateinit var allFeatures: LiveData<List<FeatureAndTrackGroup>> private set

    fun initViewModel(activity: Activity) {
        if (dataSource != null) return
        dataSource = GraphEasyDatabase.getInstance(activity.application).graphEasyDatabaseDao
        allFeatures = dataSource!!.getAllFeaturesAndTrackGroups()
    }
}