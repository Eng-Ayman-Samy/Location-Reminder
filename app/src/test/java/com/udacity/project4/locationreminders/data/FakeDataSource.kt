package com.udacity.project4.locationreminders.data

import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result

//Use FakeDataSource that acts as a test double to the LocalDataSource
class FakeDataSource : ReminderDataSource {

    //    TODO: Create a fake data source to act as a double to the real data source -> done
    private var reminderDTOServiceData: LinkedHashMap<String, ReminderDTO> = LinkedHashMap()

    private var shouldReturnError = false

    //private val observableTasks = MutableLiveData<kotlin.Result<List<ReminderDTO>>>()

    fun setReturnError(value: Boolean) {
        shouldReturnError = value
    }

    override suspend fun getReminders(): Result<List<ReminderDTO>> {
        //       TODO("Return the reminders") -> done
        if (shouldReturnError) {
            return Result.Error("Test exception")
        }
        return Result.Success(reminderDTOServiceData.values.toList())
    }

    override suspend fun saveReminder(reminder: ReminderDTO) {
        //TODO("save the reminder") -> done
        reminderDTOServiceData[reminder.id] = reminder
        //reminderDTOServiceData.values.add(reminder)
    }

    override suspend fun getReminder(id: String): Result<ReminderDTO> {
        //TODO("return the reminder with the id") -> done
        if (shouldReturnError) {
            return Result.Error("Test exception")
        }
        reminderDTOServiceData[id]?.let {
            return Result.Success(it)
        }
        return Result.Error("Could not find task")
//        return try {
//            val reminder = reminderDTOServiceData.values.findLast {
//                it.id == id
//            }
//            if (reminder != null) {
//                Result.Success(reminder)
//            } else {
//                Result.Error("Reminder not found!")
//            }
//        } catch (e: Exception) {
//            Result.Error(e.localizedMessage)
//        }
    }

    override suspend fun deleteAllReminders() {
        //TODO("delete all the reminders") -> done
        reminderDTOServiceData.clear()
    }


    fun addReminders(vararg tasks: ReminderDTO) {
        for (task in tasks) {
            reminderDTOServiceData[task.id] = task
        }
        //runBlocking { refreshTasks() }
    }

}