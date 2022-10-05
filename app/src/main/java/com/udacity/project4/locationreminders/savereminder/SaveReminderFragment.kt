package com.udacity.project4.locationreminders.savereminder

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity.RESULT_OK
import android.app.PendingIntent
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.udacity.project4.R
import com.udacity.project4.base.BaseFragment
import com.udacity.project4.base.NavigationCommand
import com.udacity.project4.databinding.FragmentSaveReminderBinding
import com.udacity.project4.locationreminders.geofence.GeofenceBroadcastReceiver
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import com.udacity.project4.locationreminders.savereminder.selectreminderlocation.TAG
import com.udacity.project4.utils.setDisplayHomeAsUpEnabled
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import java.util.concurrent.TimeUnit


class SaveReminderFragment : BaseFragment() {
    //Get the view model this time as a single to be shared with the another fragment
    override val baseViewModel: SaveReminderViewModel by sharedViewModel() //inject()
    private lateinit var binding: FragmentSaveReminderBinding
    private lateinit var reminderDataItem: ReminderDataItem

    private lateinit var geofencingClient: GeofencingClient
    private val geofencePendingIntent: PendingIntent by lazy {
        val intent = Intent(requireContext(), GeofenceBroadcastReceiver::class.java)
        intent.action = ACTION_GEOFENCE_EVENT

        @SuppressLint("UnspecifiedImmutableFlag")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            PendingIntent.getBroadcast(
                requireContext(),
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
            )
        } else {
            PendingIntent.getBroadcast(
                requireContext(),
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT
            )
        }
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSaveReminderBinding.inflate(layoutInflater)
        //DataBindingUtil.inflate(inflater, R.layout.fragment_save_reminder, container, false)
        // geofence
        geofencingClient = LocationServices.getGeofencingClient(requireActivity())

        setDisplayHomeAsUpEnabled(true)

        binding.viewModel = baseViewModel

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.lifecycleOwner = this
        binding.selectLocation.setOnClickListener {
            //Navigate to another fragment to get the user location
            baseViewModel.navigationCommand.value =
                NavigationCommand.To(SaveReminderFragmentDirections.actionSaveReminderFragmentToSelectLocationFragment())
        }

        binding.saveReminder.setOnClickListener {
            val title = baseViewModel.reminderTitle.value
            val description = baseViewModel.reminderDescription.value
            val location = baseViewModel.reminderSelectedLocationStr.value
            val latitude = baseViewModel.latitude.value
            val longitude = baseViewModel.longitude.value
            val rangeRadius = baseViewModel.rangeRadius.value.toString()

            reminderDataItem =
                ReminderDataItem(title, description, location, latitude, longitude)

//            TODO: use the user entered reminder details to:
//             1) add a geofencing request -> done
//             2) save the reminder to the local db -> done
//            addGeofence(reminderDataItem)
//            baseViewModel.validateAndSaveReminder(reminderDataItem)
            if (baseViewModel.validateEnteredData(reminderDataItem, rangeRadius))
                checkLocationPermissionAndSaveReminder()

//            if (baseViewModel.checkValidationAndSaveReminder(reminderDataItem, rangeRadius)) {
//                addGeofence(reminderDataItem)
//            }


        }
    }

    override fun onDestroy() {
        super.onDestroy()
        //make sure to clear the view model after destroy, as it's a single view model.
        baseViewModel.onClear()
    }

    //https://stackoverflow.com/questions/40110823/start-resolution-for-result-in-a-fragment
    private val resolutionForResult =
        registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { activityResult ->
            if (activityResult.resultCode == RESULT_OK) {
                baseViewModel.saveReminder(reminderDataItem)
                addGeofence(reminderDataItem)
            }
            //startLocationUpdates() or do whatever you want
            else {
                baseViewModel.showSnackBarInt.value = R.string.location_required_error
            }
        }

    @SuppressLint("MissingPermission")
    private fun addGeofence(reminderDataItem: ReminderDataItem) {
        val geofence = Geofence.Builder()
            .setRequestId(reminderDataItem.id)
            .setCircularRegion(
                reminderDataItem.latitude!!,
                reminderDataItem.longitude!!,
                baseViewModel.rangeRadius.value?.toFloat() ?: GEOFENCE_RADIUS_IN_METERS
            )
            .setExpirationDuration(GEOFENCE_EXPIRATION_IN_MILLISECONDS)
            .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER)
            .build()

        val geofencingRequest = GeofencingRequest.Builder()
            .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
            .addGeofence(geofence)
            .build()

//        geofencingClient.removeGeofences(geofencePendingIntent).run {
//            addOnCompleteListener {
        geofencingClient.addGeofences(geofencingRequest, geofencePendingIntent).run {
            addOnSuccessListener {
                Log.i(TAG, "Geofences added")
                //baseViewModel.showToast.value = getString(R.string.reminder_saved)
            }
            addOnFailureListener {
                baseViewModel.showErrorMessage.value = it.message
            }
        }
//            }
//        }
    }

    private fun checkDeviceLocationSettings(resolve: Boolean = true) {
        val locationRequest = LocationRequest.create().apply {
            priority = Priority.PRIORITY_LOW_POWER
        }
        val requestBuilder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest)
        val settingsClient = LocationServices.getSettingsClient(requireActivity())
        val locationSettingsResponseTask =
            settingsClient.checkLocationSettings(requestBuilder.build())
        locationSettingsResponseTask.addOnFailureListener { exception ->
            if (exception is ResolvableApiException && resolve) {
                try {
                    val intentSenderRequest =
                        IntentSenderRequest.Builder(exception.resolution).build()
                    resolutionForResult.launch(intentSenderRequest)
//                    exception.startResolutionForResult(
//                        requireActivity(),
//                        REQUEST_TURN_DEVICE_LOCATION_ON
//                    )
                } catch (sendEx: IntentSender.SendIntentException) {
                    Log.d(TAG, "Error getting location settings resolution: " + sendEx.message)
                }
            } else {

//                baseViewModel.showSnackBarWithAction.value = SnakeBarData(
//                    getString(R.string.location_required_error),
//                    getString(android.R.string.ok)
//                ) { checkDeviceLocationSettings() }
            }

        }

        locationSettingsResponseTask.addOnCompleteListener {
            if (it.isSuccessful) {
                //save reminder then add geofence
                baseViewModel.saveReminder(reminderDataItem)
                addGeofence(reminderDataItem)
//                if (baseViewModel.checkValidationAndSaveReminder(reminderDataItem, rangeRadius)) {
//                    addGeofence(reminderDataItem)
//                }
            }
        }
    }

    private val locationPermissionRequest = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        when {
            (permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                    permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true) -> {
                //request background location
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    backgroundLocationPermissionRequest.launch(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
                } else checkDeviceLocationSettings()
            }

            else -> {
                // No location access granted.
                //show message
                baseViewModel.showSnackBarInt.value = R.string.permission_denied_explanation
            }
        }
    }

    private val backgroundLocationPermissionRequest = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            checkDeviceLocationSettings()
        } else {
            baseViewModel.showSnackBarInt.value = R.string.permission_denied_explanation
        }
    }

    private fun checkLocationPermissionAndSaveReminder() {
        if (isLocationPermissionGranted()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                if (isBackgroundPermissionGranted()) {
                    checkDeviceLocationSettings()
                } else {
                    backgroundLocationPermissionRequest.launch(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
                }
            } else checkDeviceLocationSettings()
        } else {
            //if request foreground and background location the location permission dialog not display
            //test on API 31 so separate them
            val permissionsList = arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
            locationPermissionRequest.launch(
                permissionsList
            )
        }
    }

    private fun isLocationPermissionGranted(): Boolean {
        return ActivityCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun isBackgroundPermissionGranted(): Boolean {
        return ActivityCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.ACCESS_BACKGROUND_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }
}

val GEOFENCE_EXPIRATION_IN_MILLISECONDS: Long = TimeUnit.HOURS.toMillis(1)
const val ACTION_GEOFENCE_EVENT = "LocationReminders.action.ACTION_GEOFENCE_EVENT"
const val GEOFENCE_RADIUS_IN_METERS = 200f
//private const val REQUEST_TURN_DEVICE_LOCATION_ON = 68
