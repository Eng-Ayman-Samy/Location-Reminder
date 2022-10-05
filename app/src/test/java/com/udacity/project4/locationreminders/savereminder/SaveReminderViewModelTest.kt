package com.udacity.project4.locationreminders.savereminder

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.udacity.project4.locationreminders.data.FakeDataSource
import com.udacity.project4.locationreminders.getOrAwaitValue
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin
import com.udacity.project4.R
import kotlinx.coroutines.*

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
class SaveReminderViewModelTest {

    //TODO: provide testing to the SaveReminderView and its live data objects -> done

    // Subject under test
    private lateinit var remindersViewModel: SaveReminderViewModel

    private lateinit var remindersDataSource: FakeDataSource

    // Set the main coroutines dispatcher for unit testing.
//    @ExperimentalCoroutinesApi
//    @get:Rule
//    var mainCoroutineRule = MainCoroutineRule()

    // Executes each task synchronously using Architecture Components.
    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    //https://kotlinlang.org/api/kotlinx.coroutines/kotlinx-coroutines-test/
    @OptIn(DelicateCoroutinesApi::class)
    private val mainThreadSurrogate = newSingleThreadContext("UI thread")

    @Before
    fun setUp() {
        Dispatchers.setMain(mainThreadSurrogate)
        remindersDataSource = FakeDataSource()
        remindersViewModel = SaveReminderViewModel(
            ApplicationProvider.getApplicationContext(), remindersDataSource
        )
    }

    @After
    fun tearDown() {
        stopKoin()
        Dispatchers.resetMain()
        mainThreadSurrogate.close()
    }

    @Test
    fun check_show_loading() = runBlocking {
        val reminder = ReminderDataItem("Title", "Description", "location", 2.5, 3.5)
        remindersViewModel.saveReminderWithDelay(reminder)
        var loadingState = remindersViewModel.showLoading.getOrAwaitValue()
        assertThat(loadingState, `is`(true))
        //make delay
        delay(400)
        loadingState = remindersViewModel.showLoading.getOrAwaitValue()
        assertThat(loadingState, `is`(false))
    }

    @Test
    fun checkReminderAndRadiusValidation() {
        val reminder = ReminderDataItem("Title4", "Description4", "location4", 2.5, 3.5)
        val noError = remindersViewModel.checkValidationAndSaveReminder(reminder,"800")
        assertThat(noError, `is`(true))

    }

    @Test
    fun checkReminderValidationErrors() {
        val reminderWithNoTitle = ReminderDataItem("", "Description4", "location", 2.5, 3.5)
        val checkTitle = remindersViewModel.checkValidationAndSaveReminder(reminderWithNoTitle)
//        val title = remindersViewModel.reminderTitle.getOrAwaitValue()
//        val location = remindersViewModel.reminderSelectedLocationStr.getOrAwaitValue()
        var showSnackBarInt = remindersViewModel.showSnackBarInt.getOrAwaitValue()
        assertThat(checkTitle, `is`(false))
        assertThat(showSnackBarInt, `is`(R.string.err_enter_title))

        val reminderWithNoLocation = ReminderDataItem("title", "Description4", "", 2.5, 3.5)
        val checkLocation = remindersViewModel.checkValidationAndSaveReminder(reminderWithNoLocation)

        showSnackBarInt = remindersViewModel.showSnackBarInt.getOrAwaitValue()
        assertThat(checkLocation, `is`(false))
        assertThat(showSnackBarInt, `is`(R.string.err_select_location))

        val reminderWithNoError = ReminderDataItem("title", "Description4", "location", 2.5, 3.5)
        val checkRangeRadius = remindersViewModel.checkValidationAndSaveReminder(reminderWithNoError,"not float value")

        showSnackBarInt = remindersViewModel.showSnackBarInt.getOrAwaitValue()
        assertThat(checkRangeRadius, `is`(false))
        assertThat(showSnackBarInt, `is`(R.string.range_radius_error_message))
    }

}