package com.example.myapitest

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
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
import java.util.UUID

class FormActivity : AppCompatActivity() {

    private lateinit var binding: ActivityFormBinding
    private var item: Item? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFormBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupView()
    }

    private fun setupView() {
        item = IntentCompat.getParcelableExtra(intent, "item", Item::class.java)
        val car = item?.value

        if (car != null) {
            binding.imagePreview.loadUrl(car.imageUrl)
            binding.image.setText(car.imageUrl)
            binding.name.setText(car.name)
            binding.year.setText(car.year)
            binding.licence.setText(car.licence)
            binding.lat.setText(car.place.lat.toString())
            binding.lng.setText(car.place.long.toString())
        }

        binding.saveCTA.setOnClickListener {
            saveItem()
        }

        binding.cancelCTA.setOnClickListener { finish() }
    }

    private fun saveItem() {
        val name = binding.name.text.toString()
        val year = binding.year.text.toString()
        val licence = binding.licence.text.toString()
        val imageUrl = binding.image.text.toString()
        val lat = binding.lat.text.toString().toDoubleOrNull() ?: 0.0
        val long = binding.lng.text.toString().toDoubleOrNull() ?: 0.0

        val itemValue = ItemValue(
            id = item?.value?.id ?: UUID.randomUUID().toString(),
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
