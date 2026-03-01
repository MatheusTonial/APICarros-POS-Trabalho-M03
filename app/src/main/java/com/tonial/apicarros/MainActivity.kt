package com.tonial.apicarros

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.tonial.apicarros.adapter.CarroAdapter
import com.tonial.apicarros.databinding.ActivityMainBinding
import com.tonial.apicarros.model.Carro
import com.tonial.apicarros.service.RetrofitClient
import com.tonial.apicarros.service.safeApiCall
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.tonial.apicarros.service.Result

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupView()
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

        }
    }

    private fun fetchItems() {
        CoroutineScope(Dispatchers.IO).launch {
            val result = safeApiCall { RetrofitClient.apiService.getCars() }

            withContext(Dispatchers.Main) {
                binding.swipeRefreshLayout.isRefreshing = false
                when (result) {
                    is Result.Success -> handleOnSuccess(result.data)
                    is Result.Error -> {

                    }
                }
            }
        }
    }

    private fun handleOnSuccess(items: List<Carro>) {
        binding.recyclerView.adapter = CarroAdapter(items) { item ->
        }
    }
}