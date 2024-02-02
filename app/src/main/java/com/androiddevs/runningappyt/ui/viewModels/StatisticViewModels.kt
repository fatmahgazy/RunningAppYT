package com.androiddevs.runningappyt.ui.viewModels

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.ViewModel
import com.androiddevs.runningappyt.repositories.MainRepository
import javax.inject.Inject

class StatisticViewModels @ViewModelInject constructor(
    val mainRepository: MainRepository ) :ViewModel(){

        val totalTimeRun = mainRepository.getTotalTimeMillis()
        val totalDistance = mainRepository.getTotalDistance()
        val totalCaloriesBurned = mainRepository.getTotalCaloriesBurned()
        val totalAvgSpeed = mainRepository.getTotalAvgSpeed()

    val runSortedByData = mainRepository.getAllRunsStoredByData()

    }