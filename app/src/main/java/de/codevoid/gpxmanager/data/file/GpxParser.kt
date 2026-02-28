package de.codevoid.gpxmanager.data.file

import org.xml.sax.Attributes
import org.xml.sax.helpers.DefaultHandler
import java.io.InputStream
import javax.xml.parsers.SAXParserFactory
import kotlin.math.asin
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Parsed GPX metadata extracted from a GPX file.
 */
data class GpxMetadata(
    val name: String?,
    val date: Long?,
    val routeCount: Int,
    val trackCount: Int,
    val waypointCount: Int,
    val totalLengthKm: Double
)

/**
 * Streaming GPX parser using Java's built-in SAX parser.
 * Works in both Android runtime and JVM unit tests.
 * Extracts metadata without loading the entire file structure into memory.
 */
class GpxParser {

    companion object {
        private const val EARTH_RADIUS_KM = 6371.0
    }

    fun parse(inputStream: InputStream): GpxMetadata {
        val handler = GpxHandler()
        val factory = SAXParserFactory.newInstance()
        factory.isNamespaceAware = false
        val parser = factory.newSAXParser()
        parser.parse(inputStream, handler)
        return handler.toMetadata()
    }

    private class GpxHandler : DefaultHandler() {
        var name: String? = null
        var date: Long? = null
        var routeCount = 0
        var trackCount = 0
        var waypointCount = 0

        private val allTrackPoints = mutableListOf<MutableList<DoubleArray>>()
        private val allRoutePoints = mutableListOf<MutableList<DoubleArray>>()
        private var currentSegmentPoints: MutableList<DoubleArray>? = null
        private var currentRoutePoints: MutableList<DoubleArray>? = null

        private var inMetadata = false
        private var currentTag = ""
        private val textBuffer = StringBuilder()

        override fun startElement(uri: String?, localName: String?, qName: String?, attributes: Attributes?) {
            val tag = localName?.takeIf { it.isNotEmpty() } ?: qName ?: return
            currentTag = tag
            textBuffer.clear()

            when (tag) {
                "metadata" -> inMetadata = true
                "trk" -> trackCount++
                "trkseg" -> currentSegmentPoints = mutableListOf()
                "trkpt" -> {
                    val lat = attributes?.getValue("lat")?.toDoubleOrNull()
                    val lon = attributes?.getValue("lon")?.toDoubleOrNull()
                    if (lat != null && lon != null) {
                        currentSegmentPoints?.add(doubleArrayOf(lat, lon))
                    }
                }
                "rte" -> {
                    routeCount++
                    currentRoutePoints = mutableListOf()
                }
                "rtept" -> {
                    val lat = attributes?.getValue("lat")?.toDoubleOrNull()
                    val lon = attributes?.getValue("lon")?.toDoubleOrNull()
                    if (lat != null && lon != null) {
                        currentRoutePoints?.add(doubleArrayOf(lat, lon))
                    }
                }
                "wpt" -> waypointCount++
            }
        }

        override fun characters(ch: CharArray?, start: Int, length: Int) {
            if (ch != null) textBuffer.append(ch, start, length)
        }

        override fun endElement(uri: String?, localName: String?, qName: String?) {
            val tag = localName?.takeIf { it.isNotEmpty() } ?: qName ?: return
            val text = textBuffer.toString().trim()

            when (tag) {
                "metadata" -> inMetadata = false
                "name" -> {
                    if (inMetadata && name == null && text.isNotEmpty()) {
                        name = text
                    }
                }
                "time" -> {
                    if (inMetadata && date == null && text.isNotEmpty()) {
                        date = parseIso8601(text)
                    }
                }
                "trkseg" -> {
                    currentSegmentPoints?.let { allTrackPoints.add(it) }
                    currentSegmentPoints = null
                }
                "rte" -> {
                    currentRoutePoints?.let { allRoutePoints.add(it) }
                    currentRoutePoints = null
                }
            }
            currentTag = ""
        }

        fun toMetadata(): GpxMetadata {
            val totalLength = calculateTotalLength(allTrackPoints) + calculateTotalLength(allRoutePoints)
            return GpxMetadata(
                name = name,
                date = date,
                routeCount = routeCount,
                trackCount = trackCount,
                waypointCount = waypointCount,
                totalLengthKm = Math.round(totalLength * 100.0) / 100.0
            )
        }

        private fun calculateTotalLength(segments: List<List<DoubleArray>>): Double {
            return segments.sumOf { points ->
                if (points.size < 2) return@sumOf 0.0
                points.zipWithNext().sumOf { (a, b) -> haversine(a[0], a[1], b[0], b[1]) }
            }
        }

        private fun haversine(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
            val dLat = Math.toRadians(lat2 - lat1)
            val dLon = Math.toRadians(lon2 - lon1)
            val sinLat = sin(dLat / 2)
            val sinLon = sin(dLon / 2)
            val h = sinLat.pow(2) +
                    cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) * sinLon.pow(2)
            return 2 * EARTH_RADIUS_KM * asin(sqrt(h))
        }

        private fun parseIso8601(text: String): Long? {
            return try {
                java.time.Instant.parse(text).toEpochMilli()
            } catch (_: Exception) {
                null
            }
        }
    }
}
