package com.emma.proyects.gastour.UI.components

import android.graphics.Color
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.emma.proyects.gastour.data.models.RouteOption
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
    var isNextOrigin = true
    var markerA: Marker? = null
    var markerB: Marker? = null

    AndroidView(
        modifier = modifier,
        factory = { context ->
            MapView(context).apply {
                setTileSource(TileSourceFactory.MAPNIK)
                setMultiTouchControls(true)

                controller.setZoom(13.5)
                controller.setCenter(GeoPoint(16.7516, -93.1159))

                val receiver = object : MapEventsReceiver {
                    override fun singleTapConfirmedHelper(p: GeoPoint): Boolean {
                        val lat = p.latitude
                        val lng = p.longitude

                        if (isNextOrigin) {
                            overlays.clear()
                            overlays.add(MapEventsOverlay(this))

                            markerB = null

                            markerA = Marker(this@apply).apply {
                                position = p
                                title = "Origen (Punto A)"
                                setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                            }
                            overlays.add(markerA)
                            onPointSelected(lat, lng, true)
                            isNextOrigin = false
                        } else {
                            markerB = Marker(this@apply).apply {
                                position = p
                                title = "Destino (Punto B)"
                                setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                            }
                            overlays.add(markerB)

                            onPointSelected(lat, lng, false)
                            isNextOrigin = true
                        }

                        invalidate()
                        return true
                    }

                    override fun longPressHelper(p: GeoPoint): Boolean = false
                }

                overlays.add(MapEventsOverlay(receiver))
            }
        },
        update = { mapView ->
            // Cuando cambia el estado de las rutas, redibujamos las polílineas sobre las calles
            if (routes.isNotEmpty()) {
                // Conservar solo marcadores y el receiver, remover polilíneas viejas
                mapView.overlays.removeAll { it is Polyline }

                routes.forEachIndexed { index, route ->
                    val isSelected = route == selectedRoute

                    // Asignar colores distintos: Azul para la corta, Verde para la eficiente
                    val routeColor = if (index == 0) {
                        if (isSelected) Color.BLUE else Color.parseColor("#800000FF") // Azul traslúcido
                    } else {
                        if (isSelected) Color.parseColor("#008000") else Color.parseColor("#80008000") // Verde traslúcido
                    }

                    val line = Polyline().apply {
                        setPoints(route.pathPoints)
                        outlinePaint.color = routeColor
                        outlinePaint.strokeWidth = if (isSelected) 14f else 8f // Más gruesa la seleccionada
                    }
                    mapView.overlays.add(line)
                }

                // Ajustar el encuadre del mapa para ver ambas rutas completas
                if (selectedRoute != null && selectedRoute.pathPoints.isNotEmpty()) {
                    val bounds = org.osmdroid.util.BoundingBox.fromGeoPoints(selectedRoute.pathPoints)
                    mapView.post { mapView.zoomToBoundingBox(bounds, true, 80) }
                }

                mapView.invalidate()
            }
        }
    )
}