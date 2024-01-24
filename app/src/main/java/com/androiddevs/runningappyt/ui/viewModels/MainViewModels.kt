package com.androiddevs.runningappyt.ui.viewModels

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.androiddevs.runningappyt.other.SortType
import com.androiddevs.runningappyt.repositories.MainRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.codeforegypt.runningappyt.db.Run

class MainViewModels @ViewModelInject constructor(
    val mainRepository: MainRepository
): ViewModel() {

    private val runsSortedByDate = mainRepository.getAllRunsStoredByData()
    private val runsSortedByDistance = mainRepository.getAllRunsStoredByDistance()
    private val runsSortedByCaloriesBurned = mainRepository.getAllRunsStoredByCaloriesBurned()
    private val runsSortedByTimeInMills = mainRepository.getAllRunsStoredByTimeInMillis()
    private val runsSortedByAvg = mainRepository.getAllRunsStoredByAvgSpeed()

    val runs = MediatorLiveData<List<Run>>()

    var sortType = SortType.DATE

    init {
        runs.addSource(runsSortedByDate){ result ->
            if(sortType == SortType.DATE) {
                 result?.let { runs.value = it }
            }
        }
        runs.addSource(runsSortedByAvg){ result ->
            if(sortType == SortType.AVG_SPEED) {
                result?.let { runs.value = it  }
            }
        }
        runs.addSource(runsSortedByCaloriesBurned){ result ->
            if(sortType == SortType.CALORIES_BURNED) {
                result?.let { runs.value = it }
            }
        }
        runs.addSource(runsSortedByDistance){ result ->
            if(sortType == SortType.DISTANCE) {
                result?.let { runs.value = it }
            }
        }
        runs.addSource(runsSortedByTimeInMills){ result ->
            if(sortType == SortType.RUNNING_TIME) {
                result?.let { runs.value = it }
            }
        }

    }

    fun sortRuns(sortType: SortType) = when(sortType){
        SortType.DATE -> runsSortedByDate.value?.let { runs.value = it }
        SortType.DISTANCE -> runsSortedByDistance.value?.let { runs.value = it }
        SortType.CALORIES_BURNED -> runsSortedByCaloriesBurned.value?.let { runs.value = it }
        SortType.RUNNING_TIME -> runsSortedByTimeInMills.value?.let { runs.value = it  }
        SortType.AVG_SPEED -> runsSortedByAvg.value?.let { runs.value = it }

    }.also {
        this.sortType = sortType
    }

    fun insertRun(run: Run) = viewModelScope.launch(Dispatchers.IO) {
        mainRepository.insertRun(run)
    }
}