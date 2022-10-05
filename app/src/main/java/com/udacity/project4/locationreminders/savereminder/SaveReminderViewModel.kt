package com.udacity.project4.locationreminders.savereminder

import android.app.Application
import androidx.annotation.VisibleForTesting
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.udacity.project4.R
import com.udacity.project4.base.BaseViewModel
import com.udacity.project4.base.NavigationCommand
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SaveReminderViewModel(val app: Application, private val dataSource: ReminderDataSource) :
    BaseViewModel(app) {
    val reminderTitle = MutableLiveData<String>()
    val reminderDescription = MutableLiveData<String>()
    val reminderSelectedLocationStr = MutableLiveData<String>()

    //val selectedPOI = MutableLiveData<PointOfInterest>()
    val latitude = MutableLiveData<Double>()
    val longitude = MutableLiveData<Double>()
    val rangeRadius = MutableLiveData<String>().apply {
        value = GEOFENCE_RADIUS_IN_METERS.toInt().toString()
    }

    /**
     * Clear the live data objects to start fresh next time the view model gets called
     */
    fun onClear() {
        reminderTitle.value = "" //null
        reminderDescription.value = "" //null
        reminderSelectedLocationStr.value = ""//null
        //selectedPOI.value = null
        latitude.value = 0.0//null
        longitude.value = 0.0//null
    }

    /**
     * Validate the entered data then saves the reminder data to the DataSource
     */
//    fun validateAndSaveReminder(reminderData: ReminderDataItem) {
//        if (validateEnteredData(reminderData)) {
//            saveReminder(reminderData)
//        }
//    }

    /**
     * Validate the entered data then saves the reminder data to the DataSource
     * return true if the entered data is valid ,false otherwise
     */
    fun checkValidationAndSaveReminder(
        reminderData: ReminderDataItem,
        rangeRadius: String? = GEOFENCE_RADIUS_IN_METERS.toString()
    ): Boolean {
        return if (validateEnteredData(reminderData,rangeRadius)) {
            saveReminder(reminderData)
            true
        } else false
    }

    /**
     * Save the reminder to the data source
     */
    fun saveReminder(reminderData: ReminderDataItem) {
        showLoading.value = true
        viewModelScope.launch {
            dataSource.saveReminder(
                ReminderDTO(
                    reminderData.title,
                    reminderData.description,
                    reminderData.location,
                    reminderData.latitude,
                    reminderData.longitude,
                    reminderData.id
                )
            )
            showLoading.value = false
            showToast.value = app.getString(R.string.reminder_saved)
            navigationCommand.value = NavigationCommand.Back
        }
    }

    /**
     * Validate the entered data and show error to the user if there's any invalid data
     */
    fun validateEnteredData(reminderData: ReminderDataItem, rangeRadius: String?): Boolean {
        if (reminderData.title.isNullOrEmpty()) {
            showSnackBarInt.value = R.string.err_enter_title
            return false
        }

        if (reminderData.location.isNullOrEmpty()) {
            showSnackBarInt.value = R.string.err_select_location
            return false
        }

        if (rangeRadius?.toFloatOrNull() == null) {
            showSnackBarInt.value = R.string.range_radius_error_message
            return false
        }
        return true
    }

    @VisibleForTesting
    fun saveReminderWithDelay(reminderData: ReminderDataItem,delayInMilli :Long = 200){
        showLoading.value = true
        viewModelScope.launch {
            dataSource.saveReminder(
                ReminderDTO(
                    reminderData.title,
                    reminderData.description,
                    reminderData.location,
                    reminderData.latitude,
                    reminderData.longitude,
                    reminderData.id
                )
            )
            delay(delayInMilli) //for simulating database delay
            showLoading.value = false
            showToast.value = app.getString(R.string.reminder_saved)
            navigationCommand.value = NavigationCommand.Back
        }
    }
}

