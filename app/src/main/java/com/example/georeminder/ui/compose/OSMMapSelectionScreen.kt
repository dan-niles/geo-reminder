package com.example.georeminder.ui.compose

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.georeminder.ui.theme.GeoReminderTheme
import org.osmdroid.api.IMapController
import org.osmdroid.config.Configuration
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.Marker

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OSMMapSelectionScreen(
    onBackClick: () -> Unit,
    onLocationSelected: (latitude: Double, longitude: Double) -> Unit,
    modifier: Modifier = Modifier,
    initialLatitude: Double = 37.7749,
    initialLongitude: Double = -122.4194,
    useCurrentLocationAsDefault: Boolean = true
) {
    val context = LocalContext.current
    val keyboardController = LocalSoftwareKeyboardController.current
    var selectedLatitude by remember { mutableStateOf(initialLatitude) }
    var selectedLongitude by remember { mutableStateOf(initialLongitude) }
    var searchQuery by remember { mutableStateOf("") }
    var mapView: MapView? by remember { mutableStateOf(null) }

    DisposableEffect(Unit) {
        // Initialize osmdroid configuration
        Configuration.getInstance().load(context, context.getSharedPreferences("osmdroid", 0))
        
        onDispose {
            mapView?.onDetach()
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text("Select Location") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    onLocationSelected(selectedLatitude, selectedLongitude)
                },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Select Location"
                )
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // OpenStreetMap (full screen)
            AndroidView(
                factory = { context ->
                    MapView(context).apply {
                        mapView = this
                        setTileSource(TileSourceFactory.MAPNIK)
                        setMultiTouchControls(true)
                        
                        // Set initial position and zoom
                        val mapController: IMapController = controller
                        mapController.setZoom(15.0)
                        val startPoint = GeoPoint(initialLatitude, initialLongitude)
                        mapController.setCenter(startPoint)
                        
                        // Add initial marker
                        val marker = Marker(this).apply {
                            position = startPoint
                            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                            title = "Selected Location"
                        }
                        overlays.add(marker)
                        
                        // Add tap listener
                        val mapEventsReceiver = object : MapEventsReceiver {
                            override fun singleTapConfirmedHelper(geoPoint: GeoPoint): Boolean {
                                // Update selected coordinates
                                selectedLatitude = geoPoint.latitude
                                selectedLongitude = geoPoint.longitude
                                
                                // Clear existing overlays and add new marker
                                overlays.clear()
                                val newMarker = Marker(this@apply).apply {
                                    position = geoPoint
                                    setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                                    title = "Selected Location"
                                }
                                overlays.add(newMarker)
                                overlays.add(MapEventsOverlay(this))
                                
                                invalidate()
                                return true
                            }
                            
                            override fun longPressHelper(geoPoint: GeoPoint): Boolean {
                                return false
                            }
                        }
                        
                        overlays.add(MapEventsOverlay(mapEventsReceiver))
                    }
                },
                modifier = Modifier.fillMaxSize()
            ) { mapView ->
                // Update map view if needed
                mapView.onResume()
            }
            
            // Search and coordinates display at top (fixed overlay)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .align(Alignment.TopCenter),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.98f)
                )
            ) {
                Column(
                    modifier = Modifier.padding(12.dp)
                ) {
                    // Search field
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        label = { Text("Search places") },
                        placeholder = { Text("Try 'San Francisco', 'Tokyo', etc.") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Search"
                            )
                        },
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                        keyboardActions = KeyboardActions(
                            onSearch = {
                                searchLocation(searchQuery) { lat, lng ->
                                    selectedLatitude = lat
                                    selectedLongitude = lng
                                    // Update map center
                                    mapView?.let { map ->
                                        val mapController: IMapController = map.controller
                                        mapController.setCenter(GeoPoint(lat, lng))
                                        
                                        // Update marker
                                        map.overlays.clear()
                                        val newMarker = Marker(map).apply {
                                            position = GeoPoint(lat, lng)
                                            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                                            title = "Selected Location"
                                        }
                                        map.overlays.add(newMarker)
                                        map.overlays.add(MapEventsOverlay(object : MapEventsReceiver {
                                            override fun singleTapConfirmedHelper(geoPoint: GeoPoint): Boolean {
                                                selectedLatitude = geoPoint.latitude
                                                selectedLongitude = geoPoint.longitude
                                                map.overlays.clear()
                                                val marker = Marker(map).apply {
                                                    position = geoPoint
                                                    setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                                                    title = "Selected Location"
                                                }
                                                map.overlays.add(marker)
                                                map.overlays.add(MapEventsOverlay(this))
                                                map.invalidate()
                                                return true
                                            }
                                            override fun longPressHelper(geoPoint: GeoPoint): Boolean = false
                                        }))
                                        map.invalidate()
                                    }
                                }
                                keyboardController?.hide()
                            }
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    
                    // Coordinates display
                    Column(
                        modifier = Modifier.padding(top = 8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Selected Location",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Lat: ${"%.6f".format(selectedLatitude)}, Lng: ${"%.6f".format(selectedLongitude)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            
            // Instructions at bottom (fixed overlay)  
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .align(Alignment.BottomCenter),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.98f)
                )
            ) {
                Text(
                    text = "🗺️ Tap on the map to select your reminder location",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }
    }
}

// Simple offline geocoding for major cities
private fun searchLocation(query: String, onLocationFound: (Double, Double) -> Unit) {
    val locations = mapOf(
        // Major US Cities
        "san francisco" to Pair(37.7749, -122.4194),
        "new york" to Pair(40.7128, -74.0060),
        "los angeles" to Pair(34.0522, -118.2437),
        "chicago" to Pair(41.8781, -87.6298),
        "seattle" to Pair(47.6062, -122.3321),
        "miami" to Pair(25.7617, -80.1918),
        "boston" to Pair(42.3601, -71.0589),
        
        // International Cities
        "london" to Pair(51.5074, -0.1278),
        "paris" to Pair(48.8566, 2.3522),
        "tokyo" to Pair(35.6762, 139.6503),
        "berlin" to Pair(52.5200, 13.4050),
        "sydney" to Pair(-33.8688, 151.2093),
        "toronto" to Pair(43.6532, -79.3832),
        "mumbai" to Pair(19.0760, 72.8777),
        "bangkok" to Pair(13.7563, 100.5018),
        
        // US States (capitals)
        "california" to Pair(38.5767, -121.4934), // Sacramento
        "texas" to Pair(30.2672, -97.7431), // Austin
        "florida" to Pair(30.4518, -84.2807), // Tallahassee
        "new york state" to Pair(42.3584, -73.9781), // Albany
    )
    
    val searchKey = query.lowercase().trim()
    locations[searchKey]?.let { (lat, lng) ->
        onLocationFound(lat, lng)
    } ?: run {
        // Try partial matches
        locations.entries.find { it.key.contains(searchKey) || searchKey.contains(it.key) }?.let {
            onLocationFound(it.value.first, it.value.second)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun OSMMapSelectionScreenPreview() {
    GeoReminderTheme {
        OSMMapSelectionScreen(
            onBackClick = {},
            onLocationSelected = { _, _ -> },
            initialLatitude = 37.7749,
            initialLongitude = -122.4194
        )
    }
}