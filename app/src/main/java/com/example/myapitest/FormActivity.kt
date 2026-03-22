package com.example.myapitest

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.content.IntentCompat
import com.example.myapitest.databinding.ActivityFormBinding
import com.example.myapitest.model.Item
import com.example.myapitest.model.ItemLocation
import com.example.myapitest.model.ItemValue
import com.example.myapitest.service.Result
import com.example.myapitest.service.RetrofitClient
import com.example.myapitest.service.safeApiCall
import com.example.myapitest.ui.loadUrl
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class FormActivity : AppCompatActivity() {

    private lateinit var binding: ActivityFormBinding
    private var item: Item? = null
    private lateinit var imageUri: Uri

    private val cameraLauncher: ActivityResultLauncher<Intent> = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        if (it.resultCode == RESULT_OK) {
            binding.imageUrl.setText("IMAGE OBTAINED!")
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFormBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupView()
    }

    companion object {
        private const val EXTRA_ITEM = "item"
        private const val CAMERA_PERMISSION_REQUEST_CODE = 1002

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
                    Toast.makeText(this, "Camera permission denied", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun setupView() {
        item = IntentCompat.getParcelableExtra(intent, EXTRA_ITEM, Item::class.java)
        val car = item?.value

        if (car != null) {
            supportActionBar?.title = "Edit car"
            binding.imageUrl.setText(car.imageUrl)
            binding.name.setText(car.name)
            binding.year.setText(car.year)
            binding.licence.setText(car.licence)
            binding.lat.setText(car.place.lat.toString())
            binding.lng.setText(car.place.long.toString())
        } else {
            supportActionBar?.title = "New car"
        }

        binding.takePictureCta.setOnClickListener {
            takePicture()
        }

        binding.saveCTA.setOnClickListener {
            saveItem()
        }

        binding.cancelCTA.setOnClickListener { finish() }
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
        imageUri = createImageUri()
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
        cameraLauncher.launch(intent)
    }

    private fun createImageUri(): Uri {
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val imageFileName = "JPEG_" + timeStamp + "_"

        val storageDir: File? = getExternalFilesDir(Environment.DIRECTORY_PICTURES)

        val imageFile = File.createTempFile(imageFileName, ".jpg", storageDir)

        return FileProvider.getUriForFile(this, "com.example.myapitest.fileprovider", imageFile)
    }

    private fun validate(): Boolean {
        var isValid = true

        if (binding.imageUrl.text.toString().isBlank()) {
            binding.imageUrlLayout.error = "Required"
            isValid = false
        } else {
            binding.imageUrlLayout.error = null
        }

        if (binding.name.text.toString().isBlank()) {
            binding.nameLayout.error = "Required"
            isValid = false
        } else {
            binding.nameLayout.error = null
        }

        if (binding.year.text.toString().isBlank()) {
            binding.yearLayout.error = "Required"
            isValid = false
        } else {
            binding.yearLayout.error = null
        }

        if (binding.licence.text.toString().isBlank()) {
            binding.licenceLayout.error = "Required"
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

        CoroutineScope(Dispatchers.IO).launch {
            val result = if (item == null) {
                safeApiCall { RetrofitClient.itemApiService.addItem(itemValue) }
            } else {
                safeApiCall { RetrofitClient.itemApiService.updateItem(item!!.id, itemValue) }
            }

            withContext(Dispatchers.Main) {
                when (result) {
                    is Result.Success -> {
                        Toast.makeText(this@FormActivity, "Item saved successfully", Toast.LENGTH_LONG).show()
                        setResult(RESULT_OK)
                        finish()
                    }
                    is Result.Error -> {
                        Toast.makeText(this@FormActivity, "Error saving item: ${result.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }
}
