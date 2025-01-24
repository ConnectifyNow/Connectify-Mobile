package com.connectify.connectifyNow

import android.Manifest
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
import com.connectify.connectifyNow.Navigations.navigate
import com.connectify.connectifyNow.viewModules.OrganizationViewModel
import com.connectify.connectifyNow.databinding.FragmentMapViewBinding
import com.connectify.connectifyNow.models.Organization
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.ItemizedIconOverlay
import org.osmdroid.views.overlay.OverlayItem
import java.util.Timer
import java.util.TimerTask

class MapViewFragment : BaseFragment(), LocationListener {

    private lateinit var aMapView: MapView
    private lateinit var locationManager: LocationManager
    private lateinit var view: View
    private lateinit var viewModel: OrganizationViewModel
    private var organizationList: MutableList<Organization> = mutableListOf()

    private var loadingOverlay: LinearLayout? = null;

    private val timer = Timer()
    private val binding get() = _binding!!

    private var _binding: FragmentMapViewBinding? = null
    private var locationUpdatesRequested = false
    val LOCATIONS_PERMISSIONS_CODE = 2

    private val requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
        if (isGranted) {

            initializeMapView()
            requestLocationUpdates()
        }
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMapViewBinding.inflate(layoutInflater, container, false)
        view = binding.root

        val args = arguments
        val showFeed = args?.getString("showFeed") ?: true

        viewModel = ViewModelProvider(this)[OrganizationViewModel::class.java]

        loadingOverlay = view.findViewById(R.id.map_loading_overlay);
        loadingOverlay?.visibility = View.VISIBLE;
        // Initialize the permission launcher
        requestPermissionLauncher

        if (isLocationPermissionGranted()) {
            initializeMapView()
            requestLocationUpdates()
        } else {
            requestLocationPermissions()
        }
        return view
    }

    private fun setEventListeners() {
        val args = arguments
        val showFeed = args?.getBoolean("showFeed", true) ?: true

        if (showFeed) {
            binding.feedBtn.text = "Feed"
        } else {
            binding.feedBtn.text = "Back to Profile"
        }

        binding.feedBtn.setOnClickListener {
            clearMapOverlays()
            if (showFeed) view.navigate(R.id.action_mapViewFragment_to_feedFragment)
            else findNavController().navigateUp()

        }
    }

    private fun scheduleAutomaticRefresh() {
        // Schedule a task to run every 5 minutes
        timer.scheduleAtFixedRate(object : TimerTask() {
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
        viewModel.setOrganizationssOnMap { organization ->
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

    private fun initializeMapView() {
        val context = context ?: return
        aMapView = binding.map
        aMapView.setTileSource(TileSourceFactory.MAPNIK)
        aMapView.controller.setZoom(17.0)

        // Load map configuration
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
                Log.e("MapViewFragment", "Error launching permission request: ${e.message}")
                e.printStackTrace()
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATIONS_PERMISSIONS_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, proceed with your logic
                initializeMapView()
                requestLocationUpdates()
            } else {
                Log.d("MapViewFragment", "Location permission denied")
            }
        }
    }

    private fun addMarker(geoPoint: GeoPoint, data: HashMap<String, String>, snippet: String) {
        val markerIcon: Drawable? = ContextCompat.getDrawable(requireContext(), R.drawable.custom_marker_icon)

        val overlayItem = OverlayItem(data["name"], snippet, geoPoint)
        overlayItem.setMarker(markerIcon)

        val itemizedIconOverlay = ItemizedIconOverlay(
            context?.applicationContext,
            listOf(overlayItem),
            object : ItemizedIconOverlay.OnItemGestureListener<OverlayItem> {
                override fun onItemSingleTapUp(index: Int, item: OverlayItem): Boolean {
                    // Log information when a pin is clicked
                    Log.d("Map Click", "Marker clicked: ${item.title} ${data["bio"]} ${data["address"]}")
                    val organizationData = hashMapOf(
                        "name" to data["name"],
                        "address" to data["address"],
                        "bio" to data["bio"]
                    )
                    showOrganizationInfoDialog(organizationData)
                    return true
                }

                override fun onItemLongPress(index: Int, item: OverlayItem): Boolean {
                    // Handle long press if needed
                    return false
                }
            }
        )

        aMapView.overlays.add(itemizedIconOverlay)
        aMapView.invalidate()
    }

    private fun showOrganizationInfoDialog(organizationData: HashMap<String, String?>) {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.card_view_location, null)
        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .create()

        // Set data to views
        dialogView.findViewById<TextView>(R.id.textViewTitle).text = organizationData["name"]
        dialogView.findViewById<TextView>(R.id.textViewBio).text = organizationData["bio"]
        dialogView.findViewById<TextView>(R.id.textViewAddress).text = organizationData["address"]

        dialog.show()
    }
    private fun removeLocationUpdates() {
        if (locationUpdatesRequested) {
            locationManager.removeUpdates(this)
            locationUpdatesRequested = false // Reset the flag
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
        _binding = null
    }

    private fun clearMapOverlays() {
        aMapView.overlays.clear()
        aMapView.invalidate()
    }

    private fun requestLocationUpdates() {
        locationManager = context?.getSystemService(Context.LOCATION_SERVICE) as LocationManager

        // Check if GPS permission is granted
        // Request location updates
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
            LocationManager.NETWORK_PROVIDER,
            1000, // Minimum time interval between updates (in milliseconds)
            1f,   // Minimum distance between updates (in meters)
            this
        )
        locationUpdatesRequested = true
    }

    override fun onLocationChanged(location: Location) {
        val geoPoint = GeoPoint(location.latitude, location.longitude)
        aMapView.controller.setCenter(geoPoint)
        addMarker(geoPoint, hashMapOf("name" to "this is my location", "bio" to "", "address" to ""), "OSMDroid Marker")
        loadingOverlay?.visibility = View.INVISIBLE;
    }

    override fun onResume() {
        super.onResume()
        if (isLocationPermissionGranted()) {
            // Location permission is granted, proceed with initialization
            initializeMapView()
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
        _binding = null
    }
}
