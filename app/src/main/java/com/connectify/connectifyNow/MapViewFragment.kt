package com.connectify.connectifyNow

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.connectify.connectifyNow.databinding.FragmentMapBinding
import com.connectify.connectifyNow.helpers.navigate
import com.connectify.connectifyNow.viewModel.OrganizationViewModel
import com.connectify.connectifyNow.models.Organization
import com.connectify.connectifyNow.services.AddressApiCall
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.ItemizedIconOverlay
import org.osmdroid.views.overlay.OverlayItem
import java.util.Timer
import java.util.TimerTask

class MapFragment : BaseFragment(), LocationListener {

    private var chosenLocation: GeoPoint? = null
    private lateinit var aMap: MapView
    private lateinit var locationManager: LocationManager
    private lateinit var view: View
    private lateinit var viewModel: OrganizationViewModel
    private var organizationList: MutableList<Organization> = mutableListOf()

    private var loadingOverlay: LinearLayout? = null
    private var addressApiCall: AddressApiCall = AddressApiCall()

    private val timer = Timer()
    private var binding: FragmentMapBinding? = null

    private var locationUpdatesRequested = false

    private val requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
        if (isGranted) {
            initializeMap()
            requestLocationUpdates()
        }
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMapBinding.inflate(layoutInflater, container, false)
        view = binding?.root as View

        viewModel = ViewModelProvider(this)[OrganizationViewModel::class.java]

        loadingOverlay = view.findViewById(R.id.map_loading_overlay)
        loadingOverlay?.visibility = View.VISIBLE
        requestPermissionLauncher

        if (isLocationPermissionGranted()) {
            initializeMap()
            requestLocationUpdates()
        } else {
            requestLocationPermissions()
        }
        return view
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setEventListeners() {
        val args = arguments
        val isEditMode = args?.getBoolean("editMode", true) ?: true

        if (isEditMode) {
            binding?.backPageButton?.visibility = View.GONE
        } else {
            binding?.backPageButton?.text = getString(R.string.back_to_profile)
            binding?.backButton?.visibility = View.GONE
            binding?.doneButton?.visibility = View.GONE
        }

        binding?.backPageButton?.setOnClickListener {
            clearMapOverlays()
            if (isEditMode) {
                view.navigate(R.id.action_mapViewFragment_to_feedFragment)
            }
            else findNavController().navigateUp()
        }

        if(isEditMode){
            aMap.setOnTouchListener { view, event ->
                if (event.action == android.view.MotionEvent.ACTION_UP) {
                    val geoPoint = aMap.projection.fromPixels(event.x.toInt(), event.y.toInt()) as? GeoPoint
                    geoPoint?.let { handleMapTouch(it, view) }
                }
                false
            }
        }


        binding?.backButton?.setOnClickListener {
            clearMapOverlays()
            findNavController().navigateUp()
        }

        binding?.doneButton?.setOnClickListener {
            clearMapOverlays()

            chosenLocation?.let { location ->
                val latitude = location.latitude
                val longitude = location.longitude

                addressApiCall.getAddressByLocation(requireContext(), latitude, longitude) { address ->
                    if (address != null) {
                        // Create a bundle to hold the address data
                        val result = Bundle().apply {
                            putDouble("latitude", latitude)
                            putDouble("longitude", longitude)
                            putString("address", address)
                        }

                        val navController = findNavController()

                        // Set the fragment result to be received by the previous fragment
                        navController.previousBackStackEntry?.savedStateHandle?.set("location_data", result)

                        // Navigate back to the previous fragment
                        navController.popBackStack()
                     } else {
                        Log.d("MapFragment", "Failed to fetch address")
                    }
                }
            } ?: Log.d("MapFragment", "No location chosen")
        }
    }


    private fun handleMapTouch(geoPoint: GeoPoint, view: View) {
        chosenLocation = geoPoint
        clearMapOverlays()

        addMarker(
            geoPoint,
            hashMapOf("name" to "", "bio" to "", "address" to ""),
            "",
            false
        )

        view.performClick()
    }

    private fun scheduleAutomaticRefresh() {
        timer.schedule(object : TimerTask() {
            override fun run() {
                reloadData()
            }
        }, 0,  5 * 60 * 1000) // 5 minutes in milliseconds
    }

    private fun updateMapMarkers(organizations: MutableList<Organization>) {
        organizationList.clear()
        organizationList.addAll(organizations)

        for (organization in organizations) {
            val (address, location) = organization.location
            val latitude = location.latitude
            val longitude = location.longitude
            val organizationData = hashMapOf(
                "name" to organization.name,
                "bio" to organization.bio,
                "address" to address
            )
            addMarker(
                GeoPoint(latitude, longitude),
                organizationData,
                organizationData["name"].toString()
            )
            addMarker(GeoPoint(latitude, longitude), organizationData, "Organization Marker")
        }
    }

    private fun reloadData() {
        viewModel.setOrganizationsOnMap { organization ->
            val (address, location) = organization.location
            val latitude = location.latitude
            val longitude = location.longitude
            val organizationData = hashMapOf(
                "name" to organization.name,
                "bio" to organization.bio,
                "address" to address
            )
            addMarker(
                GeoPoint(latitude, longitude),
                organizationData,
                organizationData["name"].toString()
            )
        }

        viewModel.refreshOrganizations()
    }

    private fun initializeMap() {
        val context = context ?: return
        aMap = binding?.map as MapView
        aMap.setTileSource(TileSourceFactory.MAPNIK)
        aMap.controller.setZoom(17.0)

        Configuration.getInstance().load(
            context.applicationContext,
            context.getSharedPreferences("locations", AppCompatActivity.MODE_PRIVATE)
        )
    }

    private fun isLocationPermissionGranted(): Boolean {
        return (ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED)
    }

    private fun requestLocationPermissions() {
        if (!isLocationPermissionGranted()) {
            try {
                requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            } catch (e: Exception) {
                Log.e("MapFragment", "Error launching permission request: ${e.message}")
                e.printStackTrace()
            }
        }
    }

    private fun addMarker(geoPoint: GeoPoint, data: HashMap<String, String>, snippet: String, showOrganizationDialog: Boolean = true) {
        val markerIcon: Drawable? = ContextCompat.getDrawable(requireContext(), R.drawable.custom_marker_icon)

        val overlayItem = OverlayItem(data["name"], snippet, geoPoint)
        overlayItem.setMarker(markerIcon)

        val itemizedIconOverlay = ItemizedIconOverlay(
            context?.applicationContext,
            listOf(overlayItem),
            object : ItemizedIconOverlay.OnItemGestureListener<OverlayItem> {
                override fun onItemSingleTapUp(index: Int, item: OverlayItem): Boolean {
                    Log.d("Map Click", "Marker clicked: ${item.title} ${data["bio"]} ${data["address"]}")
                    val organizationData = hashMapOf(
                        "name" to data["name"],
                        "address" to data["address"],
                        "bio" to data["bio"]
                    )
                    if (showOrganizationDialog) {
                        showOrganizationInfoDialog(organizationData)
                    }
                    return true
                }

                override fun onItemLongPress(index: Int, item: OverlayItem): Boolean {
                    return false
                }
            }
        )

        aMap.overlays.add(itemizedIconOverlay)
        aMap.invalidate()
    }

    private fun showOrganizationInfoDialog(organizationData: HashMap<String, String?>) {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.card_location, null)
        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .create()

        dialogView.findViewById<TextView>(R.id.textViewTitle).text = organizationData["name"]
        dialogView.findViewById<TextView>(R.id.textViewBio).text = organizationData["bio"]
        dialogView.findViewById<TextView>(R.id.textViewAddress).text = organizationData["address"]

        dialog.show()
    }
    private fun removeLocationUpdates() {
        if (locationUpdatesRequested) {
            locationManager.removeUpdates(this)
            locationUpdatesRequested = false
        }
    }

    override fun onStop() {
        super.onStop()
        clearMapOverlays()
        removeLocationUpdates()
    }
    override fun onDestroyView() {
        super.onDestroyView()
        clearMapOverlays()
        removeLocationUpdates()
        binding = null
    }

    private fun clearMapOverlays() {
        aMap.overlays.clear()
        aMap.invalidate()
    }

    private fun requestLocationUpdates() {
        locationManager = context?.getSystemService(Context.LOCATION_SERVICE) as LocationManager

        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestLocationPermissions()
        }
        locationManager.requestLocationUpdates(
            LocationManager.GPS_PROVIDER,
            1000, // Minimum time interval between updates (in milliseconds)
            1f,   // Minimum distance between updates (in meters)
            this
        )
        locationUpdatesRequested = true
    }

    override fun onLocationChanged(location: Location) {
        val geoPoint = GeoPoint(location.latitude, location.longitude)
        aMap.controller.setCenter(geoPoint)
        addMarker(geoPoint, hashMapOf("name" to "this is my location", "bio" to "", "address" to ""), "OSMDroid Marker")
        loadingOverlay?.visibility = View.INVISIBLE
    }

    override fun onResume() {
        super.onResume()
        if (isLocationPermissionGranted()) {
            initializeMap()
            requestLocationUpdates()

            viewModel.organizations?.observe(viewLifecycleOwner) { organizations ->
                updateMapMarkers(organizations)
            }
            scheduleAutomaticRefresh()
            setEventListeners()

            reloadData()

        } else requestLocationPermissions()

    }

    override fun onDestroy() {
        super.onDestroy()
        timer.cancel()
        binding = null
    }
}
