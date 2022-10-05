package com.udacity.project4

//import org.koin.test.AutoCloseKoinTest // Extended Koin Test - embed autoclose @after method to close Koin after every test

import android.app.Application
import android.view.View
import androidx.navigation.findNavController
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.core.app.launchActivity
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.RootMatchers.withDecorView
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.udacity.project4.locationreminders.RemindersActivity
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.local.LocalDB
import com.udacity.project4.locationreminders.data.local.RemindersDao
import com.udacity.project4.locationreminders.data.local.RemindersLocalRepository
import com.udacity.project4.locationreminders.reminderslist.RemindersListViewModel
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import com.udacity.project4.util.DataBindingIdlingResource
import com.udacity.project4.util.monitorActivity
import com.udacity.project4.utils.EspressoIdlingResource
import kotlinx.coroutines.runBlocking
import org.hamcrest.CoreMatchers.not
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.koin.test.KoinTest
import org.koin.test.get


@RunWith(AndroidJUnit4::class)
@LargeTest
//END TO END test to black box test the app
//https://insert-koin.io/docs/reference/koin-test/testing/
class RemindersActivityTest : KoinTest {

    private lateinit var repository: ReminderDataSource
    private lateinit var appContext: Application

    // An idling resource that waits for Data Binding to have no pending bindings.
    private val dataBindingIdlingResource = DataBindingIdlingResource()

    /**
     * As we use Koin as a Service Locator Library to develop our code, we'll also use Koin to test our code.
     * at this step we will initialize Koin related code to be able to use it in out testing.
     */
    @Before
    fun init() {
        stopKoin()//stop the original app koin
        appContext = getApplicationContext()
        val myModule = module {
            viewModel {
                RemindersListViewModel(
                    appContext,
                    get() as ReminderDataSource
                )
            }
//            single {
//                SaveReminderViewModel(
//                    appContext,
//                    get() as ReminderDataSource
//                )
//            }
            //sharedViewModel()
            viewModel { SaveReminderViewModel(appContext, get() as ReminderDataSource) }
            single { RemindersLocalRepository(get()) as ReminderDataSource }
            single { LocalDB.createRemindersDao(appContext) as RemindersDao }
        }
        //declare a new koin module
        startKoin {
            modules(listOf(myModule))
        }
        //Get our real repository
        repository = get()

        //clear the data to start fresh
        runBlocking {
            repository.deleteAllReminders()
        }
    }

    private var decorView: View? = null

    @After
    fun tearDown() {
        stopKoin()
    }

    /**
     * Idling resources tell Espresso that the app is idle or busy. This is needed when operations
     * are not scheduled in the main Looper (for example when executed on a different thread).
     */
    @Before
    fun registerIdlingResource() {
        IdlingRegistry.getInstance().register(EspressoIdlingResource.countingIdlingResource)
        IdlingRegistry.getInstance().register(dataBindingIdlingResource)
    }

    /**
     * Unregister your Idling Resource so it can be garbage collected and does not leak any memory.
     */
    @After
    fun unregisterIdlingResource() {
        IdlingRegistry.getInstance().unregister(EspressoIdlingResource.countingIdlingResource)
        IdlingRegistry.getInstance().unregister(dataBindingIdlingResource)
    }

//    TODO: add End to End testing to the app -> done

    @Test
    fun checkAddReminder() {

        val activityScenario = launchActivity<RemindersActivity>()
        dataBindingIdlingResource.monitorActivity(activityScenario)
        activityScenario.onActivity {
            decorView = it.window.decorView
            //skip authentication login screen
            val navController = it.findNavController(R.id.nav_host_fragment)
            if (navController.currentDestination?.id != R.id.reminderListFragment)
                navController.navigate(R.id.reminderListFragment)
        }

        onView(withId(R.id.addReminderFAB)).perform(click())

        //test no title message
        onView(withId(R.id.saveReminder)).perform(click())
        onView(withId(com.google.android.material.R.id.snackbar_text))
            .check(matches(withText(R.string.err_enter_title)))
        //onView(withText().check(matches(isDisplayed()))

        onView(withId(R.id.reminderTitleEditText))
            .perform(typeText("title"), closeSoftKeyboard())
        onView(withText("title")).check(matches(isDisplayed()))

        onView(withId(R.id.reminderDescriptionEditText))
            .perform(typeText("description"), closeSoftKeyboard())
        onView(withText("description")).check(matches(isDisplayed()))

        onView(withId(R.id.selectLocation)).perform(click())

        onView(withId(R.id.map)).perform(click())

        //for low connection use Thread.sleep(2000)
        //or use espresso-idling-resource


        onView(withId(R.id.select_button)).perform(click())

        onView(withId(R.id.rangeRadiusEditText))
            .perform(clearText())
            .perform(typeText("700"), closeSoftKeyboard())
        onView(withText("700")).check(matches(isDisplayed()))

        onView(withId(R.id.saveReminder)).perform(click())
        //check reminder saved toast message
        onView(withText(R.string.reminder_saved)).inRoot(withDecorView(not(decorView)))
            .check(matches(isDisplayed()))

        onView(withText("title")).check(matches(isDisplayed()))
        onView(withText("description")).check(matches(isDisplayed()))
        onView(withText(R.string.default_title)).check(matches(isDisplayed()))
        activityScenario.close()
    }
}

