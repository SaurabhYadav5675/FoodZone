package com.skycore.foodplace

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.SeekBar
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.location.*
import com.skycore.foodplace.adapter.ApiResponse
import com.skycore.foodplace.adapter.RestaurantAdapter
import com.skycore.foodplace.apihelper.RetrofitHelper
import com.skycore.foodplace.databinding.ActivityMainBinding
import com.skycore.foodplace.dialogs.NoInternet
import com.skycore.foodplace.models.ApiRequest
import com.skycore.foodplace.models.Businesse
import com.skycore.foodplace.repository.RestaurantRepository
import com.skycore.foodplace.utilities.Utility
import com.skycore.foodplace.viewmodel.MainViewModel
import com.skycore.foodplace.viewmodel.MainViewModelFactory
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var mainViewModel: MainViewModel
    private lateinit var rvRestaurants: RecyclerView

    private lateinit var restaurantList: MutableList<Businesse>
    private lateinit var adapter: RestaurantAdapter
    private lateinit var repository: RestaurantRepository

    private var radius = 0
    var locationProvider: FusedLocationProviderClient? = null
    var locationCallback: LocationCallback? = null
    var locationRequest: LocationRequest.Builder? = null
    var latitude: String = ""
    var longitude: String = ""

    @SuppressLint("NotifyDataSetChanged")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        rvRestaurants = binding.rvRestaurants

        restaurantList = mutableListOf()
        adapter = RestaurantAdapter(this, restaurantList)
        rvRestaurants.adapter = adapter
        rvRestaurants.layoutManager = LinearLayoutManager(this)

        val apiService = RetrofitHelper.getApiService()
        repository = RestaurantRepository(apiService)
        mainViewModel =
            ViewModelProvider(this, MainViewModelFactory(repository))[MainViewModel::class.java]

        val seek = binding.sbRadius
        val radiusText = binding.tvDistance

        seek.progress = 5
        seek.progressTintList = ColorStateList.valueOf(Color.BLACK)
        seek.thumbTintList = ColorStateList.valueOf(Color.BLACK)

        radiusText.text = Utility.getRadiusText(seek.progress)
        getRestaurantData()

        seek.setOnSeekBarChangeListener(
            object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
                    // TODO("Not yet implemented")
                }

                override fun onStartTrackingTouch(p0: SeekBar?) {
                    // TODO("Not yet implemented")
                }

                override fun onStopTrackingTouch(p0: SeekBar?) {
                    Utility.getRadiusText(seek.progress).also { radiusText.text = it }
                    getRestaurantData()
                }
            },
        )

        binding.ivLocation.setOnClickListener {
            checkLocationPermission()
        }

        mainViewModel.restaurants.observe(this) {
            when (it) {
                is ApiResponse.Loading -> {
                    binding.svPagingLoader.visibility = View.VISIBLE
                }
                is ApiResponse.Success -> {
                    binding.svPagingLoader.visibility = View.GONE
                    if (it.successData?.businesses?.isEmpty() == true) {
                        Utility.shortToast(this, getText(R.string.noData).toString())
                    }
                    it.successData?.let { it1 -> restaurantList.addAll(it1.businesses) }
                    adapter.notifyDataSetChanged()
                }
                is ApiResponse.Error -> {
                    binding.svPagingLoader.visibility = View.GONE
                    Log.e("Api error", it.errorData.toString())
                }
            }
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    /**
     * Api calling
     */
    private fun getRestaurantData() {
        restaurantList.clear()
        if (Utility.checkInternetConnection(this)) {
            radius = binding.sbRadius.progress * 100
            GlobalScope.launch {
                val params = ApiRequest(
                    "restaurants",
                    "15",
                    radius.toString(),
                    "distance",
                    "New York City",
                    longitude,
                    latitude
                )
                val mapParams = params.toMap() as Map<String, String>
                repository.getRestaurants(mapParams)
            }
        } else {
            val dialog = NoInternet(
                this, R.style.RoundCornerAlertDialog, "Alert", getString(R.string.no_internet)
            )
            dialog.show()
            dialog.onCloseClicked { dialog.dismiss() }
        }
    }

    /**
     * check location permission before access
     */
    private fun checkLocationPermission() {
        try {
            if (ActivityCompat.checkSelfPermission(
                    this, Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(
                    this, Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this, arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    ), 1
                )
            } else {
                getLocation()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    @SuppressLint("MissingPermission")
    fun getLocation() {
        Utility.shortToast(this, getText(R.string.locationLoading).toString())
        binding.svPagingLoader.visibility = View.VISIBLE
        locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 1000)
        locationRequest!!.setMinUpdateIntervalMillis(TimeUnit.SECONDS.toMillis(30))

        locationRequest!!.setMaxUpdateDelayMillis(TimeUnit.MINUTES.toMillis(1))
        locationRequest!!.setMinUpdateDistanceMeters(10f)
        locationRequest!!.setWaitForAccurateLocation(true)
        val request = locationRequest!!.build()

        /**
         * this method basically use to update location
         * need to clear call back one get location
         */
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                super.onLocationResult(locationResult)
                binding.svPagingLoader.visibility = View.GONE
                val locationList = locationResult.locations
                if (locationList.size > 0) {
                    val location = locationList[locationList.size - 1]
                    val lat = location.latitude.toString()
                    val long = location.longitude.toString()
                    val isValidLocation: Boolean = Utility.latLongValidation(lat, long)
                    if (isValidLocation) {
                        latitude = lat
                        longitude = long
                        getRestaurantData()
                        //unregister call to avoid multiple calling
                        locationCallback?.let { locationProvider?.removeLocationUpdates(it) }
                    } else {
                        Utility.shortToast(
                            this@MainActivity, getText(R.string.invalidLocation).toString()
                        )
                    }
                } else {
                    Utility.shortToast(
                        this@MainActivity, getText(R.string.noLocation).toString()
                    )
                }
            }
        }

        locationProvider = LocationServices.getFusedLocationProviderClient(this)

        locationProvider!!.requestLocationUpdates(
            request, locationCallback as LocationCallback, Looper.myLooper()
        )

        /*
        //Provide last knows location
        locationProvider!!.lastLocation
        .addOnSuccessListener(OnSuccessListener<Location?> { location ->
            if (location != null) {
                latitude = location.latitude.toString()
                longitude = location.longitude.toString()
            }
            val isValidLocation: Boolean =
                Utility.latLongValidation(latitude, longitude)
            if (isValidLocation) retryNetworkAtt()
        })*/
    }
}