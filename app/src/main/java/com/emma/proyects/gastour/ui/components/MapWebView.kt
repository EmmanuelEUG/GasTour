package com.emma.proyects.gastour.ui.components

import android.annotation.SuppressLint
import android.graphics.Color
import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.graphics.toColorInt
import com.emma.proyects.gastour.R
import com.emma.proyects.gastour.data.models.RouteOption
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.osmdroid.config.Configuration
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.BoundingBox
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun MapWebView(
    modifier: Modifier = Modifier,
    routes: List<RouteOption> = emptyList(),
    selectedRoute: RouteOption? = null,
    originPoint: GeoPoint? = null,
    destPoint: GeoPoint? = null,
    initialCenter: GeoPoint? = null,
    onPointSelected: ((Double, Double, Boolean) -> Unit)? = null,
    url: String? = null
) {
    if (url != null) {
        AndroidView(
            modifier = modifier.fillMaxSize(),
            factory = { context ->
                WebView(context).apply {
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                    settings.javaScriptEnabled = true
                    settings.domStorageEnabled = true
                    webViewClient = WebViewClient()
                    loadUrl(url)
                }
            },
            update = { webView ->
                if (webView.url != url) {
                    webView.loadUrl(url)
                }
            }
        )
    } else {
        val context = LocalContext.current
        val originTitle = stringResource(id = R.string.marker_origin)
        val destTitle = stringResource(id = R.string.marker_dest)

        Configuration.getInstance().userAgentValue = context.packageName

        val mapView = remember {
            MapView(context).apply {
                setTileSource(TileSourceFactory.MAPNIK)
                setMultiTouchControls(true)
                controller.setZoom(14.0)
            }
        }

        // Mover la cámara a la ubicación actual cuando se reciba la posición GPS
        LaunchedEffect(initialCenter) {
            initialCenter?.let { point ->
                mapView.controller.animateTo(point)
            }
        }

        DisposableEffect(mapView) {
            onDispose {
                mapView.onDetach()
            }
        }

        LaunchedEffect(routes, selectedRoute, originPoint, destPoint) {
            withContext(Dispatchers.Default) {
                val newOverlays = mutableListOf<org.osmdroid.views.overlay.Overlay>()

                if (onPointSelected != null) {
                    val eventsReceiver = object : MapEventsReceiver {
                        override fun singleTapConfirmedHelper(p: GeoPoint?): Boolean {
                            if (p != null) {
                                val isOrigin = originPoint == null || destPoint != null
                                onPointSelected(p.latitude, p.longitude, isOrigin)
                            }
                            return true
                        }

                        override fun longPressHelper(p: GeoPoint?): Boolean = false
                    }
                    newOverlays.add(MapEventsOverlay(eventsReceiver))
                }

                originPoint?.let { point ->
                    val startMarker = Marker(mapView).apply {
                        position = point
                        title = originTitle
                        setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                    }
                    newOverlays.add(startMarker)
                }

                destPoint?.let { point ->
                    val endMarker = Marker(mapView).apply {
                        position = point
                        title = destTitle
                        setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                    }
                    newOverlays.add(endMarker)
                }

                routes.filter { it != selectedRoute }.forEach { route ->
                    if (route.pathPoints.isNotEmpty()) {
                        val polyline = Polyline().apply {
                            setPoints(route.pathPoints)
                            outlinePaint.color = Color.GRAY
                            outlinePaint.strokeWidth = 8f
                        }
                        newOverlays.add(polyline)
                    }
                }

                selectedRoute?.let { route ->
                    if (route.pathPoints.isNotEmpty()) {
                        val polyline = Polyline().apply {
                            setPoints(route.pathPoints)
                            outlinePaint.color = "#2196F3".toColorInt()
                            outlinePaint.strokeWidth = 14f
                        }
                        newOverlays.add(polyline)
                    }
                }

                withContext(Dispatchers.Main) {
                    mapView.overlays.clear()
                    mapView.overlays.addAll(newOverlays)
                    mapView.invalidate()

                    selectedRoute?.let { route ->
                        if (route.pathPoints.isNotEmpty() && mapView.isLayoutOccurred) {
                            try {
                                val box = BoundingBox.fromGeoPoints(route.pathPoints)
                                mapView.zoomToBoundingBox(box, true, 100)
                            } catch (_: Exception) { }
                        }
                    }
                }
            }
        }

        AndroidView(
            factory = { mapView },
            modifier = modifier.fillMaxSize()
        )
    }
}