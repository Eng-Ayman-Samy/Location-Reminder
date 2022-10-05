package com.udacity.project4.locationreminders.reminderslist

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.util.*

/**
 * data class acts as a data mapper between the DB and the UI
 */
//https://stackoverflow.com/questions/3323074/android-difference-between-parcelable-and-serializable
//data class ReminderDataItem(
//    var title: String?,
//    var description: String?,
//    var location: String?,
//    var latitude: Double?,
//    var longitude: Double?,
//    val id: String = UUID.randomUUID().toString()
//) : Serializable

@Parcelize
data class ReminderDataItem(
    var title: String?,
    var description: String?,
    var location: String?,
    var latitude: Double?,
    var longitude: Double?,
    val id: String = UUID.randomUUID().toString()
) : Parcelable