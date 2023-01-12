package com.example.dynamichealth_doctors

import android.Manifest
import android.content.*
import android.content.ContentValues.TAG
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.os.Looper
import android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS
import android.text.TextUtils
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.drawToBitmap
import com.example.dynamichealth_doctors.databinding.ActivityMainBinding
import com.google.android.gms.location.*
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.google.android.material.progressindicator.CircularProgressIndicator
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.*
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.collections.ArrayList


@Suppress("DEPRECATION")
class MainActivity : AppCompatActivity(), ActivityCompat.OnRequestPermissionsResultCallback {

    private lateinit var binding:ActivityMainBinding
    private lateinit var attendence_timing_ACT:AutoCompleteTextView
    private lateinit var progressbar: CircularProgressIndicator
    private lateinit var doc_name_TV:TextView
    private lateinit var time_list:ArrayList<String>
    private lateinit var submit_btn:ExtendedFloatingActionButton
    private lateinit var reset_btn:ImageButton
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var geocoder: Geocoder
    private lateinit var signatureView: SignatureView
    private lateinit var signaturePad : FrameLayout

    private var doc_name:String? = null
    private var doc_uid:String? = null
    private var doc_Timing:String? = null
    private var doc_signature_btimap:Bitmap? = null

    private lateinit var locationRequest: LocationRequest

    private lateinit var locationCallback: LocationCallback
    // This will store current location info
    private var currentLocation: Location? = null

    private lateinit var permissionLauncher:ActivityResultLauncher<Array<String>>
    private var isFineLocationRequestGranted = false
    private var isCoarseLocationRequestGranted = false
    private var isReadRequestGranted = false

    private val SHARED_PREFS = "sharedPrefs"
    private val tenant_phone = "tenantPhone"
    private val tenant_name = "tenantName"
    private val tenant_id = "tenantId"


    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        //doctor_name_search_ACT = binding.collegeAutoCompleteTV

        permissionLauncher = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()){
                permissions ->
            isReadRequestGranted = permissions[Manifest.permission.READ_EXTERNAL_STORAGE]?: isReadRequestGranted
            isFineLocationRequestGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION]?: isFineLocationRequestGranted
            isCoarseLocationRequestGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION]?: isCoarseLocationRequestGranted

        }
        RequestAllPermissions()

        Log.d("MainActivityStart", "Main activity Started")
        doc_name_TV = binding.docNameTV
        attendence_timing_ACT = binding.timingsAutoCompleteTV;
        progressbar = binding.locationFetchingProgressbar
        submit_btn = binding.submitBtn
        reset_btn = binding.resetBtn
        signaturePad = binding.signaturePadContainer

        val sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE)
        doc_name = sharedPreferences.getString(tenant_name, "")
        doc_uid = sharedPreferences.getString(tenant_phone, "")


        doc_name_TV.text = doc_name

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this@MainActivity)

        geocoder = Geocoder(this@MainActivity, Locale.getDefault())

        signatureView = SignatureView(this@MainActivity).apply {
            signaturePad.addView(this)
        }

        // Signature pad view
        time_list = arrayListOf("IN", "OUT")

        val attendance_time_adapter: ArrayAdapter<String> = ArrayAdapter<String>(
            this@MainActivity, R.layout.doctor_item, time_list)

        attendence_timing_ACT.setAdapter(attendance_time_adapter)

        attendence_timing_ACT.setOnItemClickListener { adapterView, view, position,id ->
            doc_Timing = adapterView.getItemAtPosition(position).toString()
            attendence_timing_ACT.isCursorVisible = false
            //hideKeyboard(view)
            val inputMethodManager = this.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            inputMethodManager.hideSoftInputFromWindow(adapterView.windowToken, 0)
        }
        // buttons click handling
        submit_btn.setOnClickListener {
            progressbar.visibility = View.VISIBLE
            Log.d("First", "First")
            getSummary()

        }

        reset_btn.setOnClickListener {
            //doctor_name_search_ACT.setText(null)
            doc_name = ""
            doc_Timing = ""
            attendence_timing_ACT.setText(null)
            doc_signature_btimap = null
            signatureView.clear()
        }
    }

    private fun RequestAllPermissions() {

        isReadRequestGranted = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.READ_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED


        isFineLocationRequestGranted = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        isCoarseLocationRequestGranted = ContextCompat.checkSelfPermission(
            this, Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED


        val permissionRequest :MutableList<String> = ArrayList()

        if(!isReadRequestGranted){
            permissionRequest.add(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
        if(!isFineLocationRequestGranted){
            permissionRequest.add(Manifest.permission.ACCESS_FINE_LOCATION)
        }
        if(!isCoarseLocationRequestGranted){
            permissionRequest.add(Manifest.permission.ACCESS_COARSE_LOCATION)
        }

        if(permissionRequest.isNotEmpty()){
            permissionLauncher.launch(permissionRequest.toTypedArray() )
        }
    }

    @RequiresApi(Build.VERSION_CODES.N)
    private fun getSummary() {
        doc_signature_btimap = signatureView.drawToBitmap(Bitmap.Config.ARGB_8888)
        if(!TextUtils.isEmpty(doc_Timing)){
            progressbar.visibility = View.VISIBLE
            val fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                RequestAllPermissions()
                return
            }
            fusedLocationClient.lastLocation
                .addOnSuccessListener { location : Location? ->
                    Log.d(TAG, "Latitude = ${location?.latitude}, Longitude = ${location?.longitude}")
                    // Got last known location. In some rare situations this can be null.
                    progressbar.visibility = View.GONE
                    Log.d("location_found", "latitude: ${location?.latitude}, longitude: ${location?.longitude}")
                    val geocoder= Geocoder(this@MainActivity, Locale.getDefault())
                    val address = geocoder.getFromLocation(location!!.latitude, location.longitude, 1)
                    Log.d("address", address?.get(0)?.getAddressLine(0)?:"null")

                    val confirmationDialog = ConfirmationDialog()
                    val bundle = Bundle()
                    bundle.putString("address", address?.get(0)?.getAddressLine(0)?:"null")
                    bundle.putString("name", doc_name)
                    bundle.putString("id", doc_uid)
                    bundle.putString("timing", doc_Timing)
                    bundle.putParcelable("doc_sign_bitmap",doc_signature_btimap)
                    confirmationDialog.setArguments(bundle)
                    confirmationDialog.show(supportFragmentManager,"confirmationDialog" )
                    //doctor_name_search_ACT.setText(null)
                    doc_Timing = ""
                    attendence_timing_ACT.setText(null)
                    doc_signature_btimap = null
                    signatureView.clear()

                }
        }
        else{
            Toast.makeText(this@MainActivity, "Enter all fields", Toast.LENGTH_SHORT).show()
        }
    }


    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        if (currentFocus != null) {
            val imm = this@MainActivity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(this@MainActivity.currentFocus!!.windowToken, 0)
        }
        return super.dispatchTouchEvent(ev)
    }

    override fun onDestroy() {
        super.onDestroy()
        val removeTask = fusedLocationProviderClient.removeLocationUpdates(locationCallback)
        removeTask.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Log.d(TAG, "Location Callback removed.")
            } else {
                Log.d(TAG, "Failed to remove Location Callback.")
            }
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        val removeTask = fusedLocationProviderClient.removeLocationUpdates(locationCallback)
        removeTask.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Log.d(TAG, "Location Callback removed.")
            } else {
                Log.d(TAG, "Failed to remove Location Callback.")
            }
        }
    }

}






