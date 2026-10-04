package com.emma.proyects.gastour.ui.components

import android.graphics.Color as AndroidColor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.emma.proyects.gastour.R
import com.emma.proyects.gastour.data.models.RouteOption
import com.emma.proyects.gastour.ui.theme.RouteEcoColor
import com.emma.proyects.gastour.ui.theme.RouteShortColor
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline

@Composable
fun MapWebView(
    modifier: Modifier = Modifier,
    routes: List<RouteOption> = emptyList(),
    selectedRoute: RouteOption? = null,
    onPointSelected: (lat: Double, lng: Double, isOrigin: Boolean) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val originTitle = context.getString(R.string.marker_origin)
    val destTitle = context.getString(R.string.marker_dest)

    var isNextOrigin by rememberSaveable { mutableStateOf(true) }

    val mapView = remember {
        MapView(context).apply {
            setTileSource(TileSourceFactory.MAPNIK)
            setMultiTouchControls(true)
            controller.setZoom(13.5)
            controller.setCenter(GeoPoint(16.7516, -93.1159))
        }
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> mapView.onResume()
                Lifecycle.Event.ON_PAUSE -> mapView.onPause()
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            mapView.onDetach()
        }
    }

    AndroidView(
        modifier = modifier,
        factory = {
            val receiver = object : MapEventsReceiver {
                override fun singleTapConfirmedHelper(p: GeoPoint): Boolean {
                    val lat = p.latitude
                    val lng = p.longitude

                    if (isNextOrigin) {
                        mapView.overlays.removeAll { it is Marker || it is Polyline }

                        val markerA = Marker(mapView).apply {
                            position = p
                            title = originTitle
                            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                        }
                        mapView.overlays.add(markerA)
                        onPointSelected(lat, lng, true)
                        isNextOrigin = false
                    } else {
                        val markerB = Marker(mapView).apply {
                            position = p
                            title = destTitle
                            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                        }
                        mapView.overlays.add(markerB)
                        onPointSelected(lat, lng, false)
                        isNextOrigin = true
                    }

                    mapView.invalidate()
                    return true
                }

                override fun longPressHelper(p: GeoPoint): Boolean = false
            }

            mapView.overlays.add(MapEventsOverlay(receiver))
            mapView
        },
        update = { map ->
            if (routes.isNotEmpty()) {
                map.overlays.removeAll { it is Polyline }

                routes.forEachIndexed { index, route ->
                    val isSelected = route == selectedRoute

                    val baseColor = if (index == 0) RouteShortColor.toArgb() else RouteEcoColor.toArgb()
                    val finalColor = if (isSelected) baseColor else AndroidColor.argb(120, AndroidColor.red(baseColor), AndroidColor.green(baseColor), AndroidColor.blue(baseColor))

                    val line = Polyline().apply {
                        setPoints(route.pathPoints)
                        outlinePaint.color = finalColor
                        outlinePaint.strokeWidth = if (isSelected) 14f else 8f
                    }
                    map.overlays.add(line)
                }

                if (selectedRoute != null && selectedRoute.pathPoints.isNotEmpty()) {
                    val bounds = org.osmdroid.util.BoundingBox.fromGeoPoints(selectedRoute.pathPoints)
                    map.post { map.zoomToBoundingBox(bounds, true, 80) }
                }

                map.invalidate()
            }
        }
    )
}