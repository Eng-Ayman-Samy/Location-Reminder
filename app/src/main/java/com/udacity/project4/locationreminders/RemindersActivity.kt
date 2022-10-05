package com.udacity.project4.locationreminders

import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.google.android.material.color.DynamicColors
import com.udacity.project4.R
import com.udacity.project4.databinding.ActivityRemindersBinding


/**
 * The RemindersActivity that holds the reminders fragments
 */
class RemindersActivity : AppCompatActivity() {

    lateinit var binding: ActivityRemindersBinding
    private lateinit var navController: NavController
    //private val viewModel by viewModels<AuthenticationViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Apply dynamic color
        DynamicColors.applyToActivitiesIfAvailable(application)

        binding = ActivityRemindersBinding.inflate(layoutInflater)

        val navHostFragment by lazy { supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment }
        navController = navHostFragment.navController
//        viewModel.firebaseUser.observe(this) {
//            it?.let {
//                if (navController.currentDestination?.id != R.id.reminderListFragment) {
//                    //set reminder list as main screen
//                    navController.graph.setStartDestination(R.id.reminderListFragment)
//                    navController.navigate(
//                        AuthenticationFragmentDirections.actionAuthenticationFragmentToReminderListFragment()
//                    )
//                }
//                return@observe
//            }
//            navController.graph.setStartDestination(R.id.authenticationFragment)
//        }

        setContentView(binding.root)

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                navController.popBackStack()
                //(nav_host_fragment as NavHostFragment).navController.popBackStack()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

}
