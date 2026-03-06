package com.tonial.apicarros

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.icu.text.SimpleDateFormat
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.storage.FirebaseStorage
import com.tonial.apicarros.databinding.ActivityNewCarroBinding
import com.tonial.apicarros.model.Carro
import com.tonial.apicarros.model.Location
import com.tonial.apicarros.service.RetrofitClient
import com.tonial.apicarros.service.safeApiCall
import com.tonial.apicarros.service.Result
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
import java.security.SecureRandom
import java.util.Date
import java.util.Locale
import java.util.UUID

class NewCarroActivity : AppCompatActivity(),OnMapReadyCallback {

    private lateinit var binding: ActivityNewCarroBinding
    private lateinit var mMap : GoogleMap
    private var selectedMarker: Marker? = null

    private lateinit var imageUri: Uri
    private var imageFile: File? = null

    private val cameraLauncher: ActivityResultLauncher<Intent> = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ){
        if(it.resultCode == RESULT_OK){
            saveCarro()
        }
        else{

        }
    }

    private lateinit var fusedLocationClient: FusedLocationProviderClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityNewCarroBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupView()
        requestLocationPermission()
        setupGoogleMap()
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        binding.mapContent.visibility = View.VISIBLE
        getDeviceLocation()
        mMap.setOnMapClickListener { latLong  ->
            selectedMarker?.remove()
            selectedMarker = mMap.addMarker(
                MarkerOptions().position(latLong)
                    .draggable(true)
                    .title("lat: ${latLong.latitude}, long: ${latLong.longitude}")
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        when(requestCode){
            LOCATION_PERMISSION_REQUEST_CODE -> {
                if(grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    loadCurrentLocation()
                }
                else{
                    Toast.makeText(this, R.string.permissao_de_localizacao_negada, Toast.LENGTH_SHORT).show()
                }
            }
            CAMERA_PERMISSION_REQUEST_CODE -> {
                if(grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    openCamera()
                }

            }
        }
    }

    private fun setupView(){
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }
        binding.saveCta.setOnClickListener { saveCarro() }
        binding.takePictureCta.setOnClickListener { takePicture() }
    }

    private fun saveCarro(){
        if(!validateForm()){
            return
        }
        uploadImageToFirebase()
    }

    private fun saveData() {
        val itemPosition = selectedMarker?.position?.let {
            Location(
                it.latitude,
                it.longitude
            )
        }
        CoroutineScope(Dispatchers.IO).launch {
            val id = SecureRandom().nextInt().toString()

            val infoCarro = Carro(
                id = id,
                name = binding.name.text.toString(),
                year = binding.year.text.toString(),
                licence = binding.licence.text.toString(),
                imageUrl = binding.imageUrl.text.toString(),
                place = itemPosition
            )

            val result = safeApiCall { RetrofitClient.apiService.addCarro(infoCarro) }
            withContext(Dispatchers.Main) {
                when (result) {
                    is Result.Success -> handleOnSuccess()
                    is Result.Error -> handleOnError()
                }
            }
        }
    }

    private fun handleOnError() {
        Toast.makeText(this, R.string.erro_add, Toast.LENGTH_SHORT).show()
    }

    private fun handleOnSuccess() {
        Toast.makeText(this, R.string.sucesso_add, Toast.LENGTH_SHORT).show()
        finish()
    }

    private fun validateForm(): Boolean {
        var hasError = false
        if(binding.name.text.isNullOrBlank()){
            binding.name.error = getString(R.string.campo_obrigatorio)
            hasError = true
        }
        if(binding.licence.text.isNullOrBlank()) {
            binding.licence.error = getString(R.string.campo_obrigatorio)
            hasError = true
        }
        if(binding.year.text.isNullOrBlank()) {
            binding.year.error = getString(R.string.campo_obrigatorio)
            hasError = true
        }

        return !hasError
    }

    //region camera

    private fun takePicture(){
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED){
            openCamera()
        }
        else {
            ActivityCompat.requestPermissions(this,
                arrayOf(android.Manifest.permission.CAMERA),
                CAMERA_PERMISSION_REQUEST_CODE)
        }
    }

    private fun openCamera(){
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        imageUri = createImageUri()
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
        cameraLauncher.launch(intent)
    }

    private fun createImageUri(): Uri{
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val imageFileName = "JPEG_" + timeStamp + "_"

        val storageDir: File? = getExternalFilesDir(android.os.Environment.DIRECTORY_PICTURES)
        imageFile = File.createTempFile(imageFileName, ".jpg", storageDir)

        return FileProvider.getUriForFile(this, "com.tonial.apicarros.fileprovider",
            imageFile!!
        )
    }

    private fun uploadImageToFirebase(){

        imageFile?.let {
            //inicializa firebase storage
            val storageRef = FirebaseStorage.getInstance().reference

            //cria referencia imagem
            val imageRef = storageRef.child("images/${UUID.randomUUID()}.jpg")

            //converter o bitmap para byteArray
            val baos = ByteArrayOutputStream()
            val imageBitmap = BitmapFactory.decodeFile(it.path)
            imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)

            val data = baos.toByteArray()

            onLoadingImage(true)

            imageRef.putBytes(data)
                .addOnFailureListener {
                    onLoadingImage(false)
                    Toast.makeText(this, "Falha ao fazer upload da imagem", Toast.LENGTH_SHORT).show()
                }
                .addOnSuccessListener {
                    onLoadingImage(false)
                    imageRef.downloadUrl.addOnSuccessListener { uri ->
                        binding.imageUrl.setText(uri.toString())
                        saveData()
                    }
                    Toast.makeText(this, "Sucesso ao fazer upload da imagem", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun onLoadingImage(isLoading: Boolean) {
        binding.loadImageProgress.visibility = if(isLoading) View.VISIBLE else View.GONE
        binding.takePictureCta.isEnabled = !isLoading
        binding.saveCta.isEnabled = !isLoading
    }

    //endregion camera

    //region Maps

    @SuppressLint("MissingPermission")
    private fun requestLocationPermission(){
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        //se o user permitiu localizacao, pega a ultima localizacao
        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            val currentLocation = location?.let {
                val latLong = LatLng(it.latitude, it.longitude)
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLong, 15f))
            }
        }
    }

    private fun setupGoogleMap(){
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    private fun getDeviceLocation(){
        //verifica permissao de localizacao
        if(ContextCompat.checkSelfPermission(
                this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            loadCurrentLocation()
        }
        else{
            ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE)
        }
    }

    @SuppressLint("MissingPermission")
    private fun loadCurrentLocation() {
        if (!::mMap.isInitialized) {
            return
        }
        mMap.isMyLocationEnabled = true
        mMap.uiSettings.isMyLocationButtonEnabled = true
        mMap.uiSettings.isZoomControlsEnabled = true
        fusedLocationClient
    }


    //endregion Maps

    companion object {

        private const val LOCATION_PERMISSION_REQUEST_CODE = 1001
        private const val CAMERA_PERMISSION_REQUEST_CODE = 1002

        fun newIntent(context: Context): Intent {
            return Intent(context, NewCarroActivity::class.java)
        }

    }
}