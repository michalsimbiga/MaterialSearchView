package com.example.materialsearchbar

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.AttributeSet
import android.util.Log
import android.view.View

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.AutocompleteSessionToken
import com.google.android.libraries.places.api.model.TypeFilter
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import kotlinx.android.synthetic.main.activity_maps.*

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap

    private val adapter = SuggestionsRecyclerViewAdapter().apply {
        setOnSuggestionCallback { Log.i("TESTING", "Suggestion $it") }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        material_search_view.setSuggestionsAdapter(adapter)
        material_search_view.setOnTextChangedCallbacks { query ->
            Log.i("TESTING", "setOnTextChangedCallback $query")
            getSuggestions(query)
        }
    }

    private fun getSuggestions(newText: String) {

        val apiKey = "YourApiKeyGoesHere"
        Places.initialize(applicationContext, apiKey)

        val client = Places.createClient(applicationContext)

        val token = AutocompleteSessionToken.newInstance()

        val request = FindAutocompletePredictionsRequest.builder()
            .setCountry("PL")
            .setTypeFilter(TypeFilter.ADDRESS)
            .setSessionToken(token)
            .setQuery(newText)
            .build()

        client.findAutocompletePredictions(request)
            .addOnSuccessListener { response ->
                val list = response.autocompletePredictions
                Log.i("TESTING", "SUGGESTION LIST $list")
                adapter.setSuggestions(list)
            }

            .addOnFailureListener {
                Log.i("TESTING", "SUGGESTION exteption $it")
            }

    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        // Add a marker in Sydney and move the camera
        val sydney = LatLng(-34.0, 151.0)
        mMap.addMarker(MarkerOptions().position(sydney).title("Marker in Sydney"))
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney))
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == MaterialSearchView.VOICE_RECOGNITION_CODE)
            data?.let { material_search_view.handleVoiceData(it) }
    }
}
