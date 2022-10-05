package com.udacity.project4.locationreminders.data.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result
import com.udacity.project4.locationreminders.data.dto.succeeded
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
//Medium Test to test the repository
@MediumTest
class RemindersLocalRepositoryTest {

//    TODO: Add testing implementation to the RemindersLocalRepository.kt -> done

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()


    private lateinit var database: RemindersDatabase
    private lateinit var remindersLocalRepository: RemindersLocalRepository

    @Before
    fun initDb() {
        // Using an in-memory database so that the information stored here disappears when the
        // process is killed.
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            RemindersDatabase::class.java
        ).allowMainThreadQueries().build()
        val remindersDao = database.reminderDao()
        remindersLocalRepository = RemindersLocalRepository(remindersDao)
    }

    @After
    fun closeDb() = database.close()


    @Test
    fun getReminders() = runTest {

        val reminders = remindersLocalRepository.getReminders()
        assertThat(reminders.succeeded, `is`(true))

    }

    @Test
    fun saveReminderAndGetById() = runTest {
        // GIVEN - Insert a task.
        val reminder = ReminderDTO("Title1", "Description1", "location1", 2.5, 3.5)
        remindersLocalRepository.saveReminder(reminder)

        // load the reminder by id .
        val loaded = remindersLocalRepository.getReminder(reminder.id)

        // THEN - The loaded data contains the expected values.
        assertThat(loaded.succeeded, `is`(true))
        loaded as Result.Success
        assertThat(loaded.data.id, `is`(reminder.id))
        assertThat(loaded.data.title, `is`(reminder.title))
        assertThat(loaded.data.description, `is`(reminder.description))
        assertThat(loaded.data.latitude, `is`(reminder.latitude))
        assertThat(loaded.data.location, `is`(reminder.location))
        assertThat(loaded.data.longitude, `is`(reminder.longitude))
    }

    @Test
    fun deleteReminders_getEmptyList() = runTest {
        // GIVEN - Insert a task.
        val reminder = ReminderDTO("Title1", "Description1", "location1", 2.5, 3.5)
        remindersLocalRepository.saveReminder(reminder)

        // load the reminders .
        val loaded = remindersLocalRepository.getReminders()

        // THEN - The loaded data is success.
        assertThat(loaded.succeeded, `is`(true))

        //delete all reminders
        remindersLocalRepository.deleteAllReminders()

        val emptyReminders = remindersLocalRepository.getReminders()
        emptyReminders as Result.Success
        assert(emptyReminders.data.isEmpty())
        assertThat(emptyReminders.data, `is`(emptyList<ReminderDTO>()))
    }

    @Test
    fun saveReminder_getByWrongId_getError() = runTest {
        // GIVEN - Insert a task.
        val reminder = ReminderDTO("Title1", "Description1", "location1", 2.5, 3.5)
        remindersLocalRepository.saveReminder(reminder)

        // load the reminder by wrong id .
        val loaded = remindersLocalRepository.getReminder("wrong id")

        // THEN - get error.
        //assertThat(loaded.succeeded, `is`(false))
        loaded as Result.Error
        assertThat(loaded.message,`is`("Reminder not found!"))

    }



}