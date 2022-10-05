package com.udacity.project4.locationreminders.reminderslist

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.udacity.project4.locationreminders.data.FakeDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.getOrAwaitValue
import kotlinx.coroutines.*
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.not
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin

@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
class RemindersListViewModelTest {

    //TODO: provide testing to the RemindersListViewModel and its live data objects -> done

    // Subject under test
    private lateinit var remindersViewModel: RemindersListViewModel

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
    }

    @After
    fun tearDown() {
        stopKoin()
        Dispatchers.resetMain()
        mainThreadSurrogate.close()
    }

    @Before
    fun setupViewModel() {
        remindersDataSource = FakeDataSource()
        remindersViewModel = RemindersListViewModel(
            ApplicationProvider.getApplicationContext(), remindersDataSource
        )
    }

    @Test
    fun check_show_loading() = runBlocking {
        remindersViewModel.loadRemindersWithDelay()
        var loadingState = remindersViewModel.showLoading.getOrAwaitValue()
        assertThat(loadingState, `is`(true))
        delay(400)
        loadingState = remindersViewModel.showLoading.getOrAwaitValue()
        assertThat(loadingState, `is`(false))
    }

    @Test
    fun loadReminders_ifNoData_getEmptyList_checkShowNoData() {
        remindersViewModel.loadReminders()
        val value = remindersViewModel.remindersList.getOrAwaitValue()
        //assertThat(value, nullValue())
        assertThat(value, `is`(emptyList()))
        val showNoData = remindersViewModel.showNoData.getOrAwaitValue()
        assertThat(showNoData, `is`(true))

    }

    @Test
    fun loadReminders_setData_getNoEmptyList() {
        val reminder = ReminderDTO("Title4", "Description4", "location4", 2.5, 3.5)

        remindersDataSource.addReminders(reminder)
        remindersViewModel.loadReminders()
        val value = remindersViewModel.remindersList.getOrAwaitValue()

        assertThat(value, `is`(not(emptyList())))

    }

    @Test
    fun loadReminders_setError_toGetError() {
        remindersDataSource.setReturnError(true)
        remindersViewModel.loadReminders()
        val value = remindersViewModel.showSnackBar.getOrAwaitValue()

        assertThat(value, `is`("Test exception"))

    }

    @Test
    fun loadReminders_setData_getSameData() {
        val reminder1 = ReminderDTO("Title1", "Description1", "location1", 2.5, 3.5)
        val reminder2 = ReminderDTO("Title2", "Description2", "location1", 4.50003, 30.5)
        val reminder3 = ReminderDTO("Title3", "Description3", "location1", 25.5, 8.555)
        remindersDataSource.addReminders(reminder1, reminder2, reminder3)
        remindersViewModel.loadReminders()
        val value = remindersViewModel.remindersList.getOrAwaitValue()

        val reminderDataItem1 = ReminderDataItem(
            title = reminder1.title,
            description = reminder1.description,
            location = reminder1.location,
            latitude = reminder1.latitude,
            longitude = reminder1.longitude,
            id = reminder1.id
        )
        assertThat(value.first {
            it.id == reminder1.id
        }, `is`(reminderDataItem1))

    }

}
