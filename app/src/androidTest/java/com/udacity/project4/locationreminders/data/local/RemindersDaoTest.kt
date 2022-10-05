package com.udacity.project4.locationreminders.data.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.notNullValue
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
//Unit test the DAO
@SmallTest
class RemindersDaoTest {

    // TODO: Add testing implementation to the RemindersDao.kt -> done

    // Executes each task synchronously using Architecture Components.
    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()


    private lateinit var database: RemindersDatabase
    private lateinit var remindersDao: RemindersDao

    @Before
    fun initDb() {
        // Using an in-memory database so that the information stored here disappears when the
        // process is killed.
        database = Room.inMemoryDatabaseBuilder(
            getApplicationContext(),
            RemindersDatabase::class.java
        ).allowMainThreadQueries().build()
        remindersDao = database.reminderDao()
        //FirebaseAuth.getInstance().useEmulator("10.0.2.2", 9099)
    }

    @After
    fun closeDb() = database.close()

    @Test
    fun saveReminderAndGetById() = runTest {
        // GIVEN - Insert a task.
        val reminder = ReminderDTO("Title1", "Description1", "location1", 2.5, 3.5)
        remindersDao.saveReminder(reminder)

        // load the reminder by id from the database.
        val loaded = remindersDao.getReminderById(reminder.id)

        // THEN - The loaded data contains the expected values.
        assertThat(loaded as ReminderDTO, notNullValue())
        assertThat(loaded.id, `is`(reminder.id))
        assertThat(loaded.title, `is`(reminder.title))
        assertThat(loaded.description, `is`(reminder.description))
        assertThat(loaded.latitude, `is`(reminder.latitude))
        assertThat(loaded.location, `is`(reminder.location))
        assertThat(loaded.longitude, `is`(reminder.longitude))
    }

    @Test
    fun saveReminders_getSameRemindersList() = runTest {
        val reminder1 = ReminderDTO("Title1", "Description1", "location1", 2.5, 3.5)
        remindersDao.saveReminder(reminder1)
        val reminder2 = ReminderDTO("Title2", "Description2", "location2", 3.5, 4.5)
        remindersDao.saveReminder(reminder2)

        val reminders = remindersDao.getReminders()
        assertThat(reminders, notNullValue())
        assertThat(reminders[0], `is`(reminder1))
        assertThat(reminders[1], `is`(reminder2))

    }

    @Test
    fun saveReminders_getNotNull_deleteAll_getEmptyList() = runTest {
        val reminder1 = ReminderDTO("Title1", "Description1", "location1", 2.5, 3.5)
        remindersDao.saveReminder(reminder1)
        val reminder2 = ReminderDTO("Title2", "Description2", "location2", 3.5, 4.5)
        remindersDao.saveReminder(reminder2)

        val reminders = remindersDao.getReminders()
        assertThat(reminders, `is`(notNullValue()))
        remindersDao.deleteAllReminders()
        val reminders2 = remindersDao.getReminders()
        //assertThat(reminders2, `is`(nullValue()))
        assertThat(reminders2, `is`(emptyList<ReminderDTO>()))

    }


}