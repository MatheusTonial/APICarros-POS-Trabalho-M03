package com.tonial.apicarros

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.tonial.apicarros.databinding.ActivityCarroDetailBinding
import com.tonial.apicarros.model.Carro
import com.tonial.apicarros.service.Result
import com.tonial.apicarros.service.RetrofitClient
import com.tonial.apicarros.service.safeApiCall
import com.tonial.apicarros.ui.loadUrl
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class CarroDetailActivity  : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var binding: ActivityCarroDetailBinding

    private lateinit var carro: Carro

    private lateinit var mMap: GoogleMap

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityCarroDetailBinding.inflate(layoutInflater)

        setContentView(binding.root)
        setupView()
        loadCarro()
        setupGoogleMap()
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.mapType = GoogleMap.MAP_TYPE_HYBRID
        if(::carro.isInitialized){
            //se o item ja estiver carregado carrega no mapa
            loadCarroInGoogleMap()
        }
    }

    private fun setupView() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }
        binding.deleteCTA.setOnClickListener {
            deleteCarro()
        }
        binding.editCTA.setOnClickListener {
            editCarro()
        }
    }

    private fun loadCarro() {
        val carroId = intent.getStringExtra(ARG_ID) ?: ""

        CoroutineScope(Dispatchers.IO).launch {
            val result = safeApiCall { RetrofitClient.apiService.getCarro(carroId) }

            withContext(Dispatchers.Main) {
                when (result) {
                    is Result.Success -> {
                        carro = result.data.value
                        handleSuccess()
                    }
                    is Result.Error -> {
                        Toast.makeText(this@CarroDetailActivity, "Erro ao buscar o item", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun setupGoogleMap(){
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    private fun handleSuccess(){
        binding.name.text = carro.name
        binding.year.text = carro.year
        binding.licence.setText(carro.licence)
        binding.image.loadUrl(carro.imageUrl)
        loadCarroInGoogleMap()
    }

    private fun loadCarroInGoogleMap(){
        if (!::mMap.isInitialized){
            return
        }

        carro.place?.let {
            binding.googleMapContent.visibility = View.VISIBLE
            val location = LatLng(it.lat, it.long)
            mMap.addMarker(MarkerOptions().position(location).title(carro.name))
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 15f))
        }

    }

    private fun deleteCarro(){
        CoroutineScope(Dispatchers.IO).launch {
            val result = safeApiCall { RetrofitClient.apiService.deleteCarro(carro.id) }
            withContext(Dispatchers.Main){
                when(result){
                    is Result.Success -> handleSuccessDelete()
                    is Result.Error -> {
                        Toast.makeText(this@CarroDetailActivity,
                            R.string.erro_ao_deletar, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun editCarro(){
        CoroutineScope(Dispatchers.IO).launch {
            val result = safeApiCall {
                RetrofitClient.apiService.updateCarro(
                    carro.id,
                    carro.copy(licence = binding.licence.text.toString())
                )
            }

            withContext(Dispatchers.Main){
                when(result){
                    is Result.Success<*> -> {
                        Toast.makeText(this@CarroDetailActivity,
                            R.string.editado_com_sucesso, Toast.LENGTH_SHORT).show()
                        finish()
                    }
                    is Result.Error -> {
                        Toast.makeText(this@CarroDetailActivity,
                            R.string.erro_ao_editar, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun handleSuccessDelete() {
        Toast.makeText(this, R.string.deletado_com_sucesso, Toast.LENGTH_SHORT).show()
        finish()
    }


    companion object {
        const val ARG_ID = "arg_id"

        fun newIntent(context: Context, itemId: String): Intent {
            return Intent(context, CarroDetailActivity::class.java).apply {
                putExtra(ARG_ID, itemId)
            }
        }
    }
}