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

class ItemDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityItemDetailBinding
    private lateinit var item: Item

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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
        // Usamos a ActionBar do sistema que o Tema já fornece
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Car Details"
    }

    // Metodo para tratar o clique no botão voltar da ActionBar do sistema
    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
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
                        handleSuccess()
                    }
                    is Result.Error -> {
                        Toast.makeText(this@ItemDetailActivity, "Error fetching details", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun handleSuccess() {
        val car = item.value
        binding.image.loadUrl(car.imageUrl)
        binding.name.text = "Model: ${car.name}"
        binding.year.text = "Year: ${car.year}"
        binding.licence.text = "Licence: ${car.licence}"
    }

}
