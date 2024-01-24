package org.codeforegypt.runningappyt.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.androiddevs.runningappyt.db.Converts

@Database(
    entities = [Run::class],
    version = 1
)
@TypeConverters(Converts::class)
abstract class RunningDatabase: RoomDatabase() {

    abstract fun getRunDao(): RunDAO
}