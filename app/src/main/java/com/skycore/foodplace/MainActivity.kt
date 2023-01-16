package com.skycore.foodplace

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.os.Looper
import android.view.View
import android.widget.SeekBar
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.paging.LoadState
import androidx.paging.PagingData
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.location.*
import com.skycore.foodplace.adapter.RestaurantPagingAdapter
import com.skycore.foodplace.apihelper.ApiService
import com.skycore.foodplace.apihelper.RetrofitHelper
import com.skycore.foodplace.databinding.ActivityMainBinding
import com.skycore.foodplace.dialogs.NoInternet
import com.skycore.foodplace.models.ApiRequest
import com.skycore.foodplace.paging.LoaderAdapter
import com.skycore.foodplace.utilities.Utility
import com.skycore.foodplace.viewmodel.MainViewModel
import com.skycore.foodplace.viewmodel.MainViewModelFactory
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    private lateinit var rvRestaurants: RecyclerView
    private lateinit var adapter: RestaurantPagingAdapter
    private lateinit var apiService: ApiService

    private lateinit var viewModel: MainViewModel

    private var radius = 0
    var latitude: String = ""
    var longitude: String = ""

    @SuppressLint("NotifyDataSetChanged")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val seek = binding.sbRadius
        val radiusText = binding.tvDistance
        val swipeRefresh = binding.swipeRefreshLayout

        rvRestaurants = binding.rvRestaurants
        adapter = RestaurantPagingAdapter(this)
        apiService = RetrofitHelper.getApiService()
        viewModel =
            ViewModelProvider(this, MainViewModelFactory(apiService))[MainViewModel::class.java]

        rvRestaurants.layoutManager = LinearLayoutManager(this)
        rvRestaurants.hasFixedSize()
        rvRestaurants.adapter = adapter.withLoadStateHeaderAndFooter(
            header = LoaderAdapter(), footer = LoaderAdapter(),
        )

        seek.progress = 5
        seek.progressTintList = ColorStateList.valueOf(Color.BLACK)
        seek.thumbTintList = ColorStateList.valueOf(Color.BLACK)
        radiusText.text = Utility.getRadiusText(seek.progress)

        getRestaurantData()

        swipeRefresh.setOnRefreshListener {
            swipeRefresh.isRefreshing = false
            getRestaurantData()
        }

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


        lifecycleScope.launchWhenCreated {
            viewModel.postList.observe(this@MainActivity) {
                adapter.submitData(lifecycle, PagingData.empty())
                adapter.refresh()
                adapter.submitData(lifecycle, it)
            }
        }

        adapter.addLoadStateListener { loadState ->
            if (loadState.source.refresh is LoadState.NotLoading && adapter.itemCount < 1) {
                //Utility.shortToast(this@MainActivity, getText(R.string.noData).toString())
                binding.apply {
                    if (tvNoData.visibility == View.GONE)
                        tvNoData.visibility = View.VISIBLE

                }
            } else {
                binding.apply {
                    tvNoData.visibility = View.GONE
                    rvRestaurants.visibility = View.VISIBLE
                }
            }
        }
    }

    /**
     * get data from Api
     */
    private fun getRestaurantData() {
        if (Utility.checkInternetConnection(this)) {
            radius = binding.sbRadius.progress * 100
            viewModel.setCurrentQuery(getApiRequest())

        } else {
            val dialog = NoInternet(
                this,
                R.style.RoundCornerAlertDialog,
                getString(R.string.alert),
                getString(R.string.no_internet)
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
        binding.apply {
            rvRestaurants.visibility = View.GONE
            svPagingLoader.visibility = View.VISIBLE
        }

        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 1000)
        locationRequest.setMinUpdateIntervalMillis(TimeUnit.SECONDS.toMillis(30))

        val locationProvider = LocationServices.getFusedLocationProviderClient(this)

        locationRequest.setMaxUpdateDelayMillis(TimeUnit.MINUTES.toMillis(1))
        locationRequest.setMinUpdateDistanceMeters(10f)
        locationRequest.setWaitForAccurateLocation(true)
        val request = locationRequest.build()

        /**
         * this method basically use to update location
         * need to clear call back one get location
         */
        val locationCallback = object : LocationCallback() {
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
                        //unregister call to avoid multiple calling in background
                        this.let { locationProvider.removeLocationUpdates(it) }
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

        locationProvider.requestLocationUpdates(
            request, locationCallback as LocationCallback, Looper.myLooper()
        )

        /*  //Provide last knows location
          locationProvider!!.lastLocation
              .addOnSuccessListener(OnSuccessListener<Location?> { location ->
                  if (location != null) {
                      val lat = location.latitude.toString()
                      val long = location.longitude.toString()

                      val isValidLocation: Boolean =
                          Utility.latLongValidation(latitude, longitude)
                      if (isValidLocation) {
                          latitude = lat
                          longitude = long
                          getRestaurantData()
                      }
                  }
              })*/
    }

    /**
     * generate query parameter
     */
    private fun getApiRequest() = ApiRequest(
        "restaurants", "15", radius.toString(), "distance", "New York City", longitude, latitude, ""
    )

}