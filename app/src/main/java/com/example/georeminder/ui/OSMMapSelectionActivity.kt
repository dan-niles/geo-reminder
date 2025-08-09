package com.example.georeminder.ui

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.georeminder.ui.compose.OSMMapSelectionScreen
import com.example.georeminder.ui.theme.GeoReminderTheme

class OSMMapSelectionActivity : ComponentActivity() {
    
    companion object {
        const val EXTRA_LATITUDE = "extra_latitude"
        const val EXTRA_LONGITUDE = "extra_longitude"
        const val EXTRA_INITIAL_LATITUDE = "extra_initial_latitude"
        const val EXTRA_INITIAL_LONGITUDE = "extra_initial_longitude"
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val initialLatitude = intent.getDoubleExtra(EXTRA_INITIAL_LATITUDE, 37.7749)
        val initialLongitude = intent.getDoubleExtra(EXTRA_INITIAL_LONGITUDE, -122.4194)
        
        setContent {
            GeoReminderTheme {
                OSMMapSelectionScreen(
                    onBackClick = { finish() },
                    onLocationSelected = { latitude, longitude ->
                        val resultIntent = Intent().apply {
                            putExtra(EXTRA_LATITUDE, latitude)
                            putExtra(EXTRA_LONGITUDE, longitude)
                        }
                        setResult(Activity.RESULT_OK, resultIntent)
                        finish()
                    },
                    initialLatitude = initialLatitude,
                    initialLongitude = initialLongitude
                )
            }
        }
    }
}