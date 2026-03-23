package com.example.myapitest

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.content.IntentCompat
import androidx.lifecycle.lifecycleScope
import com.example.myapitest.databinding.ActivityFormBinding
import com.example.myapitest.model.Item
import com.example.myapitest.model.ItemLocation
import com.example.myapitest.model.ItemValue
import com.example.myapitest.service.Result
import com.example.myapitest.service.RetrofitClient
import com.example.myapitest.service.safeApiCall
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import org.osmdroid.config.Configuration
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.Marker
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class FormActivity : AppCompatActivity() {

    private lateinit var binding: ActivityFormBinding
    private var item: Item? = null
    private lateinit var imageUri: Uri
    private var photoFile: File? = null
    private var currentPhotoPath: String? = null
    private var marker: Marker? = null

    private val cameraLauncher: ActivityResultLauncher<Intent> = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        if (it.resultCode == RESULT_OK) {
            uploadImage()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Configuration.getInstance().load(this, getSharedPreferences("osmdroid", MODE_PRIVATE))
        binding = ActivityFormBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        savedInstanceState?.getString("photoPath")?.let {
            currentPhotoPath = it
            photoFile = File(it)
        }

        setupView()
        setupMap()
    }

    private fun setupMap() {
        binding.map.setTileSource(TileSourceFactory.MAPNIK)
        binding.map.setMultiTouchControls(true)
        binding.map.controller.setZoom(15.0)

        val car = item?.value
        if (car != null) {
            val geoPoint = GeoPoint(car.place.lat, car.place.long)
            updateMarker(geoPoint)
        } else {
            getCurrentLocation()
        }

        val mapEventsReceiver = object : MapEventsReceiver {
            override fun singleTapConfirmedHelper(p: GeoPoint?): Boolean {
                p?.let { updateMarker(it) }
                return true
            }

            override fun longPressHelper(p: GeoPoint?): Boolean {
                return false
            }
        }
        binding.map.overlays.add(MapEventsOverlay(mapEventsReceiver))
    }

    private fun updateMarker(geoPoint: GeoPoint) {
        if (marker == null) {
            marker = Marker(binding.map)
            marker?.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
            binding.map.overlays.add(marker)
        }
        marker?.position = geoPoint
        binding.map.controller.animateTo(geoPoint)
        binding.lat.setText(geoPoint.latitude.toString())
        binding.lng.setText(geoPoint.longitude.toString())
        binding.map.invalidate()
    }

    private fun getCurrentLocation() {
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                location?.let {
                    updateMarker(GeoPoint(it.latitude, it.longitude))
                }
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        currentPhotoPath?.let {
            outState.putString("photoPath", it)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    companion object {
        private const val EXTRA_ITEM = "item"
        private const val CAMERA_PERMISSION_REQUEST_CODE = 1002
        private const val IMGBB_API_KEY = "60981f6964dfc77f80db0f38690399cc"

        fun newIntent(context: Context, item: Item? = null): Intent {
            return Intent(context, FormActivity::class.java).apply {
                putExtra(EXTRA_ITEM, item)
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            CAMERA_PERMISSION_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    openCamera()
                } else {
                    Toast.makeText(this, getString(R.string.error_camera_denied), Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun setupView() {
        item = IntentCompat.getParcelableExtra(intent, EXTRA_ITEM, Item::class.java)
        val car = item?.value

        if (car != null) {
            supportActionBar?.title = getString(R.string.title_edit_car)
            binding.imageUrl.setText(car.imageUrl)
            binding.name.setText(car.name)
            binding.year.setText(car.year)
            binding.licence.setText(car.licence)
            binding.lat.setText(car.place.lat.toString())
            binding.lng.setText(car.place.long.toString())
        } else {
            supportActionBar?.title = getString(R.string.title_new_car)
        }

        binding.takePictureCta.setOnClickListener {
            takePicture()
        }

        binding.saveCTA.setOnClickListener {
            saveItem()
        }
    }

    fun takePicture() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            openCamera()
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.CAMERA),
                CAMERA_PERMISSION_REQUEST_CODE
            )
        }
    }

    private fun openCamera() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        val file = createImageFile()
        photoFile = file
        currentPhotoPath = file.absolutePath
        
        imageUri = FileProvider.getUriForFile(this, "com.example.myapitest.fileprovider", file)
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
        cameraLauncher.launch(intent)
    }

    private fun createImageFile(): File {
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir: File? = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile("JPEG_${timeStamp}_", ".jpg", storageDir)
    }

    private fun uploadImage() {
        val file = photoFile ?: currentPhotoPath?.let { File(it) }
        
        if (file == null || !file.exists()) {
            Toast.makeText(this, getString(R.string.error_image_not_found), Toast.LENGTH_SHORT).show()
            return
        }

        binding.imageUrl.setText(getString(R.string.msg_uploading))
        binding.takePictureCta.isEnabled = false
        binding.saveCTA.isEnabled = false

        val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
        val body = MultipartBody.Part.createFormData("image", file.name, requestFile)

        lifecycleScope.launch {
            val result = withContext(Dispatchers.IO) {
                safeApiCall { RetrofitClient.imgBBService.uploadImage(IMGBB_API_KEY, body) }
            }

            binding.takePictureCta.isEnabled = true
            binding.saveCTA.isEnabled = true
            when (result) {
                is Result.Success -> {
                    val response = result.data
                    if (response.isSuccessful && response.body() != null) {
                        binding.imageUrl.setText(response.body()!!.data.url)
                    } else {
                        binding.imageUrl.setText("")
                        Toast.makeText(this@FormActivity, getString(R.string.error_upload_failed, response.message()), Toast.LENGTH_SHORT).show()
                    }
                }
                is Result.Error -> {
                    binding.imageUrl.setText("")
                    Toast.makeText(this@FormActivity, getString(R.string.error_upload_failed, result.message), Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun validate(): Boolean {
        var isValid = true

        val imageUrl = binding.imageUrl.text.toString()
        if (imageUrl.isBlank()) {
            binding.imageUrlLayout.error = getString(R.string.error_required)
            isValid = false
        } else if (imageUrl == getString(R.string.msg_uploading)) {
            binding.imageUrlLayout.error = getString(R.string.error_wait_upload)
            isValid = false
        } else {
            binding.imageUrlLayout.error = null
        }

        if (binding.name.text.toString().isBlank()) {
            binding.nameLayout.error = getString(R.string.error_required)
            isValid = false
        } else {
            binding.nameLayout.error = null
        }

        if (binding.year.text.toString().isBlank()) {
            binding.yearLayout.error = getString(R.string.error_required)
            isValid = false
        } else {
            binding.yearLayout.error = null
        }

        if (binding.licence.text.toString().isBlank()) {
            binding.licenceLayout.error = getString(R.string.error_required)
            isValid = false
        } else {
            binding.licenceLayout.error = null
        }

        return isValid
    }

    private fun saveItem() {
        if (!validate()) return

        val name = binding.name.text.toString()
        val year = binding.year.text.toString()
        val licence = binding.licence.text.toString()
        val imageUrl = binding.imageUrl.text.toString()
        val lat = binding.lat.text.toString().toDoubleOrNull() ?: 0.0
        val long = binding.lng.text.toString().toDoubleOrNull() ?: 0.0

        val itemValue = ItemValue(
            id = item?.value?.id ?: System.currentTimeMillis().toString(),
            imageUrl = imageUrl,
            year = year,
            name = name,
            licence = licence,
            place = ItemLocation(lat, long)
        )

        lifecycleScope.launch {
            val result = withContext(Dispatchers.IO) {
                if (item == null) {
                    safeApiCall { RetrofitClient.itemApiService.addItem(itemValue) }
                } else {
                    safeApiCall { RetrofitClient.itemApiService.updateItem(item!!.id, itemValue) }
                }
            }

            when (result) {
                is Result.Success -> {
                    Toast.makeText(this@FormActivity, getString(R.string.msg_item_saved), Toast.LENGTH_LONG).show()
                    setResult(RESULT_OK)
                    finish()
                }
                is Result.Error -> {
                    Toast.makeText(this@FormActivity, getString(R.string.error_save_item, result.message), Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        binding.map.onResume()
    }

    override fun onPause() {
        super.onPause()
        binding.map.onPause()
    }
}
