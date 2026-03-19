package com.example.myapitest

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.example.myapitest.databinding.ActivityItemDetailBinding
import com.example.myapitest.model.Item
import com.example.myapitest.service.RetrofitClient
import com.example.myapitest.service.safeApiCall
import com.example.myapitest.service.Result
import com.example.myapitest.ui.loadUrl
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.overlay.Marker

class ItemDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityItemDetailBinding
    private lateinit var item: Item

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Configuração necessária para o OSMDroid antes de inflar o layout
        Configuration.getInstance().load(this, getSharedPreferences("osmdroid", MODE_PRIVATE))
        
        binding = ActivityItemDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupView()
        loadItem()
    }

    companion object {
        private const val ARG_ID = "arg_id"
        fun newIntent(context: Context, itemId: String): Intent {
            return Intent(context, ItemDetailActivity::class.java).apply {
                putExtra(ARG_ID, itemId)
            }
        }
    }

    private fun setupView() {
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Car Details"
        
        // Inicializa o mapa com algumas configurações básicas
        binding.map.setTileSource(TileSourceFactory.MAPNIK)
        binding.map.setMultiTouchControls(true)
        binding.map.controller.setZoom(15.0)

        binding.deleteCTA.setOnClickListener {
            deleteItem()
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    private fun deleteItem() {
        CoroutineScope(Dispatchers.IO).launch {
            val result = safeApiCall { RetrofitClient.itemApiService.deleteItem(item.id) }
            withContext(Dispatchers.Main) {
                when (result) {
                    is Result.Success -> handleSuccessDelete()
                    is Result.Error -> {
                        Toast.makeText(this@ItemDetailActivity, "Error deleting item", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    fun handleSuccessDelete() {
        Toast.makeText(this@ItemDetailActivity, "Item deleted successfully", Toast.LENGTH_LONG).show()
        finish()
    }

    private fun loadItem() {
        val itemId = intent.getStringExtra(ARG_ID) ?: ""
        CoroutineScope(Dispatchers.IO).launch {
            val result = safeApiCall { RetrofitClient.itemApiService.getItem(itemId) }

            withContext(Dispatchers.Main) {
                when (result) {
                    is Result.Success -> {
                        item = result.data
                        Log.d("API_TEST", "Item recebido: $item")
                        handleSuccessLoad()
                    }
                    is Result.Error -> {
                        Toast.makeText(this@ItemDetailActivity, "Error fetching details", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun handleSuccessLoad() {
        val car = item.value
        binding.image.loadUrl(car.imageUrl)
        binding.name.text = "Model: ${car.name}"
        binding.year.text = "Year: ${car.year}"
        binding.licence.text = "Licence: ${car.licence}"
        
        // Configura a localização no mapa
        val location = GeoPoint(car.place.lat, car.place.long)
        binding.map.controller.setCenter(location)
        
        // Adiciona um marcador (pin) no local
        val marker = Marker(binding.map)
        marker.position = location
        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
        marker.title = car.name
        binding.map.overlays.add(marker)
        
        binding.map.invalidate() // Atualiza o mapa
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
