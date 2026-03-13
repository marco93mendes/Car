package com.example.myapitest

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myapitest.adapater.ItemAdapter
import com.example.myapitest.databinding.ActivityMainBinding
import com.example.myapitest.model.Item
import com.example.myapitest.service.RetrofitClient
import com.example.myapitest.service.safeApiCall
import com.example.myapitest.service.Result
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        //requestLocationPermission() //TODO
        setupView()


        //////////////////////////////////////////////////////////
        //1- post, delete e patch
        //2- pagina de login firebase
        //3- imagens do firebase
        //4- google maps
        //////////////////////////////////////////////////////////


        // 1- Criar tela de Login com algum provedor do Firebase (Telefone, Google)
        //      Cadastrar o Seguinte celular para login de test: +5511912345678
        //      Código de verificação: 101010

        // 2- Criar Opção de Logout no aplicativo

        // 3- Integrar API REST /car no aplicativo
        //      API será disponibilida no Github
        //      JSON Necessário para salvar e exibir no aplicativo
        //      O Image Url deve ser uma foto armazenada no Firebase Storage
        //      { "id": "001", "imageUrl":"https://image", "year":"2020/2020", "name":"Gaspar", "licence":"ABC-1234", "place": {"lat": 0, "long": 0} }

        // Opcionalmente trabalhar com o Google Maps ara enviar o place
    }

    override fun onResume() {
        super.onResume()
        fetchItems()
    }

    private fun setupView() {
        binding.swipeRefreshLayout.setOnRefreshListener {
            fetchItems()
        }

        binding.recyclerView.layoutManager = LinearLayoutManager(this)

        binding.addCta.setOnClickListener {
            //TODO
        }
    }

    private fun requestLocationPermission() {
        // TODO
    }

    private fun fetchItems() {
        CoroutineScope(Dispatchers.IO).launch {
            val result = safeApiCall { RetrofitClient.itemApiService.getItems() }

            withContext(Dispatchers.Main) {
                binding.swipeRefreshLayout.isRefreshing = false
                when (result) {
                    is Result.Success -> {
                        handleOnSuccess(result.data)
                    }

                    //is Result.Error -> { }
                    else -> Toast.makeText(this@MainActivity, "Error | API may be OFF", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun handleOnSuccess(items: List<Item>) {
        binding.recyclerView.adapter = ItemAdapter(items) { item ->
            val intent = ItemDetailActivity.newIntent(this, item.id)
            startActivity(intent)
        }

    }


}
