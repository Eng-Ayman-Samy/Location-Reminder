package com.udacity.project4.locationreminders.savereminder.selectreminderlocation


import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.IntentSender
import android.content.pm.PackageManager
import android.content.res.Resources
import android.location.Geocoder
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.view.*
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.lifecycle.Lifecycle
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.location.Priority.PRIORITY_LOW_POWER
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.MarkerOptions
import com.udacity.project4.R
import com.udacity.project4.base.BaseFragment
import com.udacity.project4.base.NavigationCommand
import com.udacity.project4.base.SnakeBarData
import com.udacity.project4.databinding.FragmentSelectLocationBinding
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import com.udacity.project4.utils.setDisplayHomeAsUpEnabled
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import java.util.*

class SelectLocationFragment : BaseFragment(),
    OnMapReadyCallback {

    //Use Koin to get the view model of the SaveReminder
    override val baseViewModel: SaveReminderViewModel by sharedViewModel()  //inject()
    private lateinit var binding: FragmentSelectLocationBinding

    private lateinit var map: GoogleMap
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private var selectedLatLng: LatLng =
        LatLng(30.049740376291684, 31.233634391517125)//Egypt Museum
    private lateinit var title: String
    private lateinit var locationCallback: LocationCallback

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentSelectLocationBinding.inflate(layoutInflater)
        //DataBindingUtil.inflate(inflater, R.layout.fragment_select_location, container, false)

        binding.viewModel = baseViewModel
        binding.lifecycleOwner = this
        //set default title
        title = getString(R.string.default_title)

        // Construct a FusedLocationProviderClient.
        fusedLocationProviderClient =
            LocationServices.getFusedLocationProviderClient(requireActivity())

        //https://stackoverflow.com/questions/37784000/onmapreadycallback-not-triggered-when-using-fragment
        val supportMapFragment =
            childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        supportMapFragment.getMapAsync(this)

//        val mapFragment = requireActivity().supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
//        mapFragment.getMapAsync(this)

        val menuHost: MenuHost = requireActivity()
        menuHost.addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.map_options, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean = when (menuItem.itemId) {
                R.id.normal_map -> {
                    map.mapType = GoogleMap.MAP_TYPE_NORMAL
                    true
                }
                R.id.hybrid_map -> {
                    map.mapType = GoogleMap.MAP_TYPE_HYBRID
                    true
                }
                R.id.satellite_map -> {
                    map.mapType = GoogleMap.MAP_TYPE_SATELLITE
                    true
                }
                R.id.terrain_map -> {
                    map.mapType = GoogleMap.MAP_TYPE_TERRAIN
                    true
                }
                else -> false
            }

        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
        //setHasOptionsMenu(true)
        setDisplayHomeAsUpEnabled(true)

//        TODO: add the map setup implementation -> done
//        TODO: zoom to the user location after taking his permission -> done
//        TODO: add style to the map -> done
//        TODO: put a marker to location that the user selected -> done
//        TODO: call this function after the user confirms on the selected location -> done

        binding.selectButton.setOnClickListener {
            onLocationSelected()
        }

        //https://stackoverflow.com/questions/66207502/retrieve-the-current-location-in-fragment-with-mapview-in-kotlin
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                val location = locationResult.locations.last()
                val myLatLng = location.toLatLng()
                try {
                    title = myLatLng.getTitle()
                } catch (ex: Exception) {
                    baseViewModel.showSnackBarInt.value = R.string.network_error
                }
                map.addMarker(MarkerOptions().position(myLatLng).title(title))
                map.animateCamera(
                    CameraUpdateFactory.newLatLngZoom(
                        myLatLng, zoomLevel
                    )
                )
                selectedLatLng = myLatLng
                stopLocationUpdates()
            }
        }

        return binding.root
    }

    private fun onLocationSelected() {
        //        TODO: When the user confirms on the selected location,
        //         send back the selected location details to the view model
        //         and navigate back to the previous fragment to save the reminder and add the geofence -> done

        baseViewModel.longitude.value = selectedLatLng.longitude
        baseViewModel.latitude.value = selectedLatLng.latitude
//        baseViewModel.selectedPOI.value = selectedPOI
        baseViewModel.reminderSelectedLocationStr.value = title
        baseViewModel.navigationCommand.value = NavigationCommand.Back
        //findNavController().popBackStack() //or navigateUp()

    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        setPoiClick(map)
        setMapLongClick(map)
        setMapStyle(map)
        //default location
        map.addMarker(MarkerOptions().position(selectedLatLng).title(title))
        map.moveCamera(
            CameraUpdateFactory.newLatLngZoom(selectedLatLng, zoomLevel)
        )
        checkLocationPermission()
        map.uiSettings.isZoomControlsEnabled = true
        //map.setOnMyLocationButtonClickListener(this)
        map.setOnMyLocationButtonClickListener {
            checkDeviceLocationSettings()
            false
        }


//        val overlaySize = 100f
//        val androidOverlay = GroundOverlayOptions()
//            .image(BitmapDescriptorFactory.fromResource(R.drawable.android))
//            .position(workLatLng, overlaySize)
//        map.addGroundOverlay(androidOverlay)
    }

    private fun setMapLongClick(map: GoogleMap) {
        map.setOnMapLongClickListener {
            // A Snippet is Additional text that's displayed below the title.
            try {
                title = it.getTitle()
            } catch (ex: Exception) {
                baseViewModel.showSnackBarInt.value = R.string.network_error
                //baseViewModel.showSnackBar.value = ex.message
                title = getString(R.string.dropped_pin)
            }

            selectedLatLng = it
            //title = getString(R.string.dropped_pin)
            val snippet = String.format(
                Locale.getDefault(),
                getString(R.string.lat_long_snippet), it.latitude, it.longitude
            )
            map.addMarker(
                MarkerOptions()
                    .position(it)
                    .title(title)
                    .snippet(snippet)
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))
            )

        }
    }

    private fun LatLng.getTitle(): String {
        var mTitle = ""
        val geocoder = Geocoder(requireContext())
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            geocoder.getFromLocation(this.latitude, this.longitude, 1) { address ->
                mTitle = address[0]?.getAddressLine(0)
                    ?: getString(R.string.dropped_pin)//"Unknown address"
            }
        } else {
            @Suppress("DEPRECATION")
            mTitle = geocoder.getFromLocation(this.latitude, this.longitude, 1)?.get(0)
                ?.getAddressLine(0)
                ?: getString(R.string.dropped_pin)//"Unknown address"
        }
        return mTitle
    }

    private fun setPoiClick(map: GoogleMap) {
        map.setOnPoiClickListener { poi ->
            selectedLatLng = poi.latLng
            title = poi.name
            //baseViewModel.selectedPOI.value = poi //ToDo what is for
            val poiMarker = map.addMarker(
                MarkerOptions()
                    .position(poi.latLng)
                    .title(poi.name)
            )
            poiMarker?.showInfoWindow()
        }
    }

    private fun setMapStyle(map: GoogleMap) {
        try {
            // Customize the styling of the base map using a JSON object defined
            // in a raw resource file.
            val success = map.setMapStyle(
                MapStyleOptions.loadRawResourceStyle(
                    requireContext(),
                    R.raw.map_style
                )
            )

            if (!success) {
                Log.e(tag, "Style parsing failed.")
            }
        } catch (e: Resources.NotFoundException) {
            Log.e(tag, "Can't find style. Error: ", e)
        }
    }


    @SuppressLint("MissingPermission")
    private val locationPermissionRequest = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        when {
            permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                    permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true -> {

                map.isMyLocationEnabled = true

                //enableMyLocation()
                //checkDeviceLocationSettings() //moved to map my location button
            }
            //getOrDefault need android version Sdk 24+
//            permissions.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false) -> {
//                // Precise location access granted.
//            }
//            permissions.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false) -> {
//                // Only approximate location access granted.
//            }
            else -> {
                // No location access granted.
                //show message
                val requestPermissionRationale =
                    shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION) ||
                            shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_COARSE_LOCATION)

                //check shouldShowRequestPermissionRationale
                if (!requestPermissionRationale)
                    baseViewModel.showSnackBarInt.value = R.string.where_to_find_location_permission
                else {
                    baseViewModel.showSnackBarInt.value = R.string.permission_denied_explanation
                }

            }
        }
    }


    private fun stopLocationUpdates() {
        fusedLocationProviderClient.removeLocationUpdates(locationCallback)
    }

    @SuppressLint("MissingPermission")
    private fun startLocationUpdates() {
        fusedLocationProviderClient.requestLocationUpdates(
            LocationRequest.create(),
            locationCallback,
            Looper.getMainLooper()
        )
    }

    @SuppressLint("MissingPermission")
    private fun enableMyLocation() {
        //map.isMyLocationEnabled = true
        //map.myLocation is deprecated
        try {
            val locationResult = fusedLocationProviderClient.lastLocation
            locationResult.addOnCompleteListener(requireActivity()) { task ->
                if (task.isSuccessful) {
                    //title = getString(R.string.myLocation)
                    task.result.let {
                        if (it == null)
                            baseViewModel.showSnackBarInt.value =
                                R.string.try_click_location_button_again
                        else {
                            baseViewModel.showSnackBar.value =
                                " task 2 ${it.latitude}"
                            val myLatLng = it.toLatLng()
                            try {
                                title = myLatLng.getTitle()
                            } catch (ex: Exception) {
                                baseViewModel.showSnackBarInt.value = R.string.network_error
                            }
                            map.addMarker(MarkerOptions().position(myLatLng).title(title))
                            map.animateCamera(
                                CameraUpdateFactory.newLatLngZoom(
                                    myLatLng, zoomLevel
                                )
                            )
                            selectedLatLng = myLatLng
                        }
                    }
                } else {
                    baseViewModel.showSnackBar.value = "Exception: ${task.exception.toString()}"
                    Log.d(TAG, "Current location is null.")
                    Log.e(TAG, "Exception: %s", task.exception)
//                    map.uiSettings?.isMyLocationButtonEnabled = false
                }
            }

        } catch (e: SecurityException) {
            baseViewModel.showSnackBar.value = "security ${e.message} "
            Log.e("Exception: %s", e.message, e)
        }
    }
//    private val runningQOrLater =
//        Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q


    @SuppressLint("MissingPermission")
    private fun checkLocationPermission() {
        if (isLocationPermissionGranted()) {
            map.isMyLocationEnabled = true
            //checkDeviceLocationSettings() //moved to my location button
        } else {

            val permissionsList = arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )

            val requestPermissionRationale =
                shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION) ||
                        shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_COARSE_LOCATION)

            //check shouldShowRequestPermissionRationale
            if (!requestPermissionRationale)
                locationPermissionRequest.launch(permissionsList)
            else {
                //PermissionUtils.RationaleDialog.newInstance(
                //                LOCATION_PERMISSION_REQUEST_CODE, true
                //            ).show(supportFragmentManager, "dialog")
                //            return
                baseViewModel.showSnackBarWithAction.value = SnakeBarData(
                    getString(R.string.permission_denied_explanation),
                    getString(R.string.allow_permission)
                ) {
                    locationPermissionRequest.launch(permissionsList)
                }
            }
            //baseViewModel.showSnackBarInt.value = R.string.where_to_find_location_permission
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

    private val resolutionForResult =
        registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { activityResult ->
            if (activityResult.resultCode == Activity.RESULT_OK) {
                startLocationUpdates()
                //enableMyLocation()
            }
//            else {
//                baseViewModel.showSnackBarInt.value = R.string.location_required_error
//            }
        }

    private fun checkDeviceLocationSettings(resolve: Boolean = true) {
        val locationRequest = LocationRequest.create().apply {
            priority = PRIORITY_LOW_POWER
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
                Log.d(TAG, "Error getting location settings resolution: " + exception.message)
            }

        }
        locationSettingsResponseTask.addOnCompleteListener {
            if (it.isSuccessful) {
                startLocationUpdates()
                //enableMyLocation()
            }
        }
    }

}

private fun Location.toLatLng() = LatLng(latitude, longitude)
const val TAG = "Tag"
const val zoomLevel = 15f
//private const val REQUEST_TURN_DEVICE_LOCATION_ON = 68