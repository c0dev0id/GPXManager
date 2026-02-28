package de.codevoid.gpxmanager.data.file

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class GpxParserTest {

    private val parser = GpxParser()

    @Test
    fun `parse extracts metadata name from GPX file`() {
        val input = javaClass.classLoader!!.getResourceAsStream("sample.gpx")!!
        val metadata = parser.parse(input)

        println("=== Test: parse extracts metadata name ===")
        println("Expected name: Test Route")
        println("Actual name: ${metadata.name}")

        assertEquals("Test Route", metadata.name)
    }

    @Test
    fun `parse extracts correct date from metadata`() {
        val input = javaClass.classLoader!!.getResourceAsStream("sample.gpx")!!
        val metadata = parser.parse(input)

        println("=== Test: parse extracts correct date ===")
        println("Expected date (epoch): 1718447400000 (2024-06-15T10:30:00Z)")
        println("Actual date: ${metadata.date}")

        assertNotNull(metadata.date)
        assertEquals(1718447400000L, metadata.date)
    }

    @Test
    fun `parse counts waypoints correctly`() {
        val input = javaClass.classLoader!!.getResourceAsStream("sample.gpx")!!
        val metadata = parser.parse(input)

        println("=== Test: parse counts waypoints ===")
        println("Expected waypoint count: 2")
        println("Actual waypoint count: ${metadata.waypointCount}")

        assertEquals(2, metadata.waypointCount)
    }

    @Test
    fun `parse counts routes correctly`() {
        val input = javaClass.classLoader!!.getResourceAsStream("sample.gpx")!!
        val metadata = parser.parse(input)

        println("=== Test: parse counts routes ===")
        println("Expected route count: 1")
        println("Actual route count: ${metadata.routeCount}")

        assertEquals(1, metadata.routeCount)
    }

    @Test
    fun `parse counts tracks correctly`() {
        val input = javaClass.classLoader!!.getResourceAsStream("sample.gpx")!!
        val metadata = parser.parse(input)

        println("=== Test: parse counts tracks ===")
        println("Expected track count: 2")
        println("Actual track count: ${metadata.trackCount}")

        assertEquals(2, metadata.trackCount)
    }

    @Test
    fun `parse calculates positive total length for tracks and routes`() {
        val input = javaClass.classLoader!!.getResourceAsStream("sample.gpx")!!
        val metadata = parser.parse(input)

        println("=== Test: parse calculates total length ===")
        println("Total length (km): ${metadata.totalLengthKm}")

        assertTrue("Total length should be positive", metadata.totalLengthKm > 0.0)
        // Combined route + track distance should be reasonable (Zurich-Bern area is ~100km)
        assertTrue("Total length should be less than 500 km", metadata.totalLengthKm < 500.0)
    }

    @Test
    fun `parse handles empty GPX file`() {
        val input = javaClass.classLoader!!.getResourceAsStream("empty.gpx")!!
        val metadata = parser.parse(input)

        println("=== Test: parse handles empty GPX ===")
        println("Route count: ${metadata.routeCount}")
        println("Track count: ${metadata.trackCount}")
        println("Waypoint count: ${metadata.waypointCount}")
        println("Total length: ${metadata.totalLengthKm}")

        assertNull(metadata.name)
        assertNull(metadata.date)
        assertEquals(0, metadata.routeCount)
        assertEquals(0, metadata.trackCount)
        assertEquals(0, metadata.waypointCount)
        assertEquals(0.0, metadata.totalLengthKm, 0.001)
    }

    @Test
    fun `parse handles GPX with only waypoints`() {
        val gpxContent = """
            <?xml version="1.0" encoding="UTF-8"?>
            <gpx version="1.1">
              <wpt lat="47.3769" lon="8.5417"><name>Point A</name></wpt>
              <wpt lat="46.9480" lon="7.4474"><name>Point B</name></wpt>
              <wpt lat="46.2044" lon="6.1432"><name>Point C</name></wpt>
            </gpx>
        """.trimIndent()

        val metadata = parser.parse(gpxContent.byteInputStream())

        println("=== Test: parse handles GPX with only waypoints ===")
        println("Waypoint count: ${metadata.waypointCount}")
        println("Route count: ${metadata.routeCount}")
        println("Track count: ${metadata.trackCount}")

        assertEquals(3, metadata.waypointCount)
        assertEquals(0, metadata.routeCount)
        assertEquals(0, metadata.trackCount)
        assertEquals(0.0, metadata.totalLengthKm, 0.001)
    }

    @Test
    fun `parse calculates haversine distance correctly for known points`() {
        // Zurich (47.3769, 8.5417) to Bern (46.9480, 7.4474) is approximately 95 km
        val gpxContent = """
            <?xml version="1.0" encoding="UTF-8"?>
            <gpx version="1.1">
              <trk>
                <trkseg>
                  <trkpt lat="47.3769" lon="8.5417" />
                  <trkpt lat="46.9480" lon="7.4474" />
                </trkseg>
              </trk>
            </gpx>
        """.trimIndent()

        val metadata = parser.parse(gpxContent.byteInputStream())

        println("=== Test: haversine distance Zurich-Bern ===")
        println("Expected: ~95 km")
        println("Actual: ${metadata.totalLengthKm} km")

        // Zurich to Bern straight line is approximately 95 km
        assertTrue("Distance should be around 95 km", metadata.totalLengthKm > 85.0)
        assertTrue("Distance should be around 95 km", metadata.totalLengthKm < 105.0)
    }
}
