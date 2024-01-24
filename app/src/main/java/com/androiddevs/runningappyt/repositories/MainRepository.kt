package com.androiddevs.runningappyt.repositories

import org.codeforegypt.runningappyt.db.Run
import org.codeforegypt.runningappyt.db.RunDAO
import javax.inject.Inject

class MainRepository @Inject constructor(

    val runDao: RunDAO
) {
    suspend fun insertRun(run: Run) = runDao.insertRun(run)

    suspend fun deleteRun(run: Run) = runDao.deleteRun(run)

    fun getAllRunsStoredByData() = runDao.getAllRunesStoredByData()

    fun getAllRunsStoredByDistance() = runDao.getAllRunesStoredByDistance()

    fun getAllRunsStoredByTimeInMillis() = runDao.getAllRunesStoredByTimeInMillis()

    fun getAllRunsStoredByAvgSpeed() = runDao.getAllRunesStoredByAvgSpeed()

    fun getAllRunsStoredByCaloriesBurned() = runDao.getAllRunesStoredByCaloriesBurned()

    fun getTotalAvgSpeed() = runDao.getTotalAvgSpeed()

    fun getTotalDistance() = runDao.getTotalDistance()

    fun getTotalCaloriesBurned() = runDao.getTotalCaloriesBurned()

    fun getTotalTimeMillis() = runDao.getTotalTimeInMillis()

}