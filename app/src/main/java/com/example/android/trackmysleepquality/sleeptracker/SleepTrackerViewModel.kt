/*
 * Copyright 2018, The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.android.trackmysleepquality.sleeptracker

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.viewModelScope
import com.example.android.trackmysleepquality.database.SleepDatabaseDao
import com.example.android.trackmysleepquality.database.SleepNight
import com.example.android.trackmysleepquality.formatNights
import kotlinx.coroutines.launch

/**
 * ViewModel for SleepTrackerFragment.
 */
class SleepTrackerViewModel(
        val database: SleepDatabaseDao,
        application: Application) : AndroidViewModel(application) {

    //NOT REQUIRED (01) Declare Job() and cancel jobs in onCleared().

    //NOT REQUIRED  (02) Define uiScope for coroutines.

    //DONE (03) Create a MutableLiveData variable tonight for one SleepNight.
    private var tonight = MutableLiveData<SleepNight?>()

    //DONE (04) Define a variable, nights. Then getAllNights() from the database
    //and assign to the nights variable.
    private val nights = database.getAllNights()

    var nightsString = Transformations.map(nights) { sleepNights ->
        formatNights(sleepNights, application.resources)
    }


    //DONE (05) In an init block, initializeTonight(), and implement it to launch a coroutine
    //to getTonightFromDatabase().
    init {
        initializeTonight()
    }

    private fun initializeTonight() {
        //Use the viewModelScope.launch{} to start a coroutine in the ViewModelScope.
        viewModelScope.launch {
            tonight.value = getTonightFromDatabase()
        }
    }

    // Will run on working thread
    private suspend fun getTonightFromDatabase(): SleepNight? {
        var night = database.getTonight()
        if (night?.startTimeMilli != night?.endTimeMilli) {
            night = null
        }
        return night
    }

    fun onStartTracking() {
        viewModelScope.launch {
            val newNight = SleepNight()
            insert(newNight)
            tonight.value = getTonightFromDatabase()
        }
    }

    private suspend fun insert(newNight: SleepNight) {
        database.insert(newNight)
    }


    fun onStopTracking() {
        viewModelScope.launch {
            val oldNight = tonight.value ?: return@launch
            oldNight.endTimeMilli = System.currentTimeMillis()
            update(oldNight)
        }
    }

    private suspend fun update(night: SleepNight) {
        database.update(night)
    }


    fun onClear() {
        viewModelScope.launch {
            clear()
            tonight.value = null
        }
    }

    private suspend fun clear() {
        database.clear()
    }
    //DONE (06) Implement getTonightFromDatabase()as a suspend function.

    //DONE (07) Implement the click handler for the Start button, onStartTracking(), using
    //coroutines. Define the suspend function insert(), to insert a new night into the database.

    //DONE (08) Create onStopTracking() for the Stop button with an update() suspend function.

    //DONE (09) For the Clear button, created onClear() with a clear() suspend function.

    //DONE (12) Transform nights into a nightsString using formatNights().

}

