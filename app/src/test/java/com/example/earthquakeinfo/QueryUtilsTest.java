package com.example.earthquakeinfo;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.io.IOException;
import java.util.List;

/*
    Unit tests for parsing the USGS JSON response.
    No internet is used: we give the parser a small, hand-written JSON
    that looks exactly like a real USGS answer.
 */
public class QueryUtilsTest {

    // Three earthquakes: a normal one, one without magnitude and one without place
    // Three earthquakes: a normal one (with coordinates and a tsunami flag),
    // one without magnitude, and one without place and without geometry
    private static final String USGS_JSON = """
            {
              "features": [
                { "properties": { "mag": 6.5, "place": "73 km WSW of Rumoi, Japan",
                                  "time": 1700000000000, "url": "https://earthquake.usgs.gov/a",
                                  "tsunami": 1 },
                  "geometry": { "type": "Point", "coordinates": [141.18, 43.61, 10.5] } },
                { "properties": { "mag": null, "place": "Somewhere",
                                  "time": 1700000000001, "url": "https://earthquake.usgs.gov/b" } },
                { "properties": { "mag": 4.2, "place": null,
                                  "time": 1700000000002, "url": "https://earthquake.usgs.gov/c",
                                  "tsunami": 0 } }
              ]
            }
            """;

    @Test
    public void extract_validJson_readsAllFields() throws IOException {
        List<Earthquake> earthquakes = QueryUtils.extractEarthquakesFromJson(USGS_JSON);

        Earthquake first = earthquakes.get(0);
        // The third argument (0.0001) is the allowed difference when comparing decimal numbers
        assertEquals(6.5, first.getMagnitude(), 0.0001);
        assertEquals("73 km WSW of Rumoi, Japan", first.getLocation());
        assertEquals(1700000000000L, first.getTime());
        assertEquals("https://earthquake.usgs.gov/a", first.getUrl());
    }

    @Test
    public void extract_earthquakeWithoutMagnitude_isSkipped() throws IOException {
        List<Earthquake> earthquakes = QueryUtils.extractEarthquakesFromJson(USGS_JSON);

        // 3 in the JSON, but the one with "mag": null is skipped
        assertEquals(2, earthquakes.size());
        assertEquals(4.2, earthquakes.get(1).getMagnitude(), 0.0001);
    }

    @Test
    public void extract_earthquakeWithoutPlace_getsEmptyText() throws IOException {
        List<Earthquake> earthquakes = QueryUtils.extractEarthquakesFromJson(USGS_JSON);

        assertEquals("", earthquakes.get(1).getLocation());
    }

    @Test
    public void extract_emptyOrNullText_returnsEmptyList() throws IOException {
        assertTrue(QueryUtils.extractEarthquakesFromJson("").isEmpty());
        assertTrue(QueryUtils.extractEarthquakesFromJson(null).isEmpty());
    }

    @Test
    public void extract_noFeatures_returnsEmptyList() throws IOException {
        assertTrue(QueryUtils.extractEarthquakesFromJson("{ \"features\": [] }").isEmpty());
    }

    @Test
    public void extract_invalidJson_throwsIOException() {
        assertThrows(IOException.class,
                () -> QueryUtils.extractEarthquakesFromJson("this is not JSON"));
    }

    @Test
    public void extract_jsonWithoutFeatures_throwsIOException() {
        assertThrows(IOException.class,
                () -> QueryUtils.extractEarthquakesFromJson("{ \"type\": \"FeatureCollection\" }"));
    }
    @Test
    public void extract_coordinates_longitudeFirstThenLatitudeThenDepth() throws IOException {
        Earthquake first = QueryUtils.extractEarthquakesFromJson(USGS_JSON).get(0);

        // GeoJSON order is [longitude, latitude, depth]
        assertEquals(43.61, first.getLatitude(), 0.0001);
        assertEquals(141.18, first.getLongitude(), 0.0001);
        assertEquals(10.5, first.getDepth(), 0.0001);
        assertTrue(first.hasCoordinates());
        assertTrue(first.hasDepth());
    }

    @Test
    public void extract_tsunamiFlag_isRead() throws IOException {
        List<Earthquake> earthquakes = QueryUtils.extractEarthquakesFromJson(USGS_JSON);

        assertTrue(earthquakes.get(0).isTsunami());
        assertFalse(earthquakes.get(1).isTsunami());
    }

    @Test
    public void extract_noGeometry_hasNoCoordinates() throws IOException {
        Earthquake withoutGeometry = QueryUtils.extractEarthquakesFromJson(USGS_JSON).get(1);

        assertFalse(withoutGeometry.hasCoordinates());
        assertFalse(withoutGeometry.hasDepth());
    }
}
